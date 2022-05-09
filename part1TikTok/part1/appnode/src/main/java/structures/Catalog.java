package structures;

import communication.ConnectionMetadata;
import communication.Message;
import config.BrokerConfiguration;
import hashing.HashCalculator;
import nodes.Publisher;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Catalog {

    ChannelList channels = new ChannelList();
    MultimediaList multimedias = new MultimediaList(); // multimedia list
    TagsHashtable tagTable = new TagsHashtable(); // tag -> multimedia
    ChannelHashtable channelTable = new ChannelHashtable(); // channel -> multimedia
    String profileDirectory;

    // ******************************************************
    //          Read video metadata from disk
    // ******************************************************
    public void load(String profileDirectory) throws IOException, TikaException, SAXException {
        this.profileDirectory = profileDirectory;
        File dir = new File(profileDirectory);

        for (File file : dir.listFiles()) { // for each channel
            if (file.isDirectory()) {
                if (file.getName().equals("downloads") || file.getName().equals("tags")) {
                    continue;
                }

                System.out.println("EXAMINING DIR : " + file.getAbsolutePath() + " name:" + file.getName());

                String channelName = file.getName();
                Channel channel= new Channel(channelName, file.getCanonicalPath());

                channels.add(channel);

                if (!channelTable.containsKey(channel.getName())) {
                    channelTable.put(channel.getName(), new MultimediaList());
                }


                for (File video : file.listFiles()) { // for each video in that channel
                    System.out.println("\tMULTIMEDIA : " + video.getAbsolutePath() + " name:" + video.getName());

                    VideoFile media = new VideoFile(video.getName(), channelName , video.getCanonicalPath());

                    multimedias.add(media);

                    channelTable.get(channel.getName()).add(media);
                }
            } else {
                System.out.println("FILE: " + file.getAbsolutePath()  + " (ignored) ") ;
            }
        }
    }

    public void loadTags(String tagsDirectory) throws IOException {
        File dir = new File(tagsDirectory);

        for (File file : dir.listFiles()) { // for each tag file in tags ..
            if (file.isDirectory()) {
                continue;
            }

            System.out.println("Tag File : " + file.getAbsolutePath() + " name:" + file.getName());

            if (file.getName().endsWith(".mp4")) {
                String videoName = file.getName();

                VideoFile media = multimedias.findByTitle(videoName);

                if (media == null) {
                    System.out.println("Skipped: " + file.getName());
                    continue;
                }

                List<String> allLines = Files.readAllLines(Paths.get(file.getCanonicalPath()));

                for (String tag : allLines) {
                    if (!tagTable.containsKey(tag)) {
                        tagTable.put(tag, new MultimediaList());
                    }

                    media.getAssociatedTags().add(tag);

                    tagTable.get(tag).add(media);


                    System.out.println("Tag loaded: " + tag);
                }
            } else {
                String channelName =  file.getName();

                Channel c = channels.findByTitle(channelName);

                if (c == null) {
                    System.out.println("Skipped: " + file.getName());
                    continue;
                }

                List<String> allLines = Files.readAllLines(Paths.get(file.getCanonicalPath()));

                for (String tag : allLines) {
                    if (!tagTable.containsKey(tag)) {
                        tagTable.put(tag, new MultimediaList());
                    }

                    c.getAssociatedTags().add(tag);

                    System.out.println("Tag loaded: " + tag);
                }
            }
        }
    }

    // ******************************************************
    //          Print video metadata to display
    // ******************************************************
    public void print() {
        System.out.println("-----------------------------------------------");
        System.out.println("               Discovered data ");
        System.out.println("-----------------------------------------------");

        System.out.println("+ All Channels: ");
        int i = 1;
        for (Channel c : channels) {
            System.out.print("\t" + i++ + " ");
            c.print();
        }

         System.out.println("+ All Multimedia: ");

        int j = 1;
         for (VideoFile m : multimedias) {
             System.out.print("\t" + j++ + " ");
             m.print();
         }

         System.out.println("Channel hashtable: ");

         i= 1;
         for (Map.Entry<String, MultimediaList> e : channelTable.entrySet()) {
             System.out.println("CHANNEL KEY: " +  e.getKey() );

             for (VideoFile m : e.getValue()) {
                 System.out.print("\t" + i++ + " ");
                 m.print();
             }
         }


        System.out.println("Tag hashtable: ");

         i = 1;
        for (Map.Entry<String, MultimediaList> e : tagTable.entrySet()) {
            System.out.println("TAG KEY: " +  e.getKey() );

            for (VideoFile m : e.getValue()) {
                System.out.print("\t" + i++ + " ");
                m.print();
            }
        }

     }

    // ******************************************************
    //          Send video metadata to brokers
    // ******************************************************
    public void sendToBrokers(Publisher publisher, ArrayList<ConnectionMetadata> connectionMetadata1) throws IOException {
         System.out.println("Sending Channels to brokers: ");

         // for each channel ...
         for (Channel c : channels) {
             BigInteger hash = HashCalculator.hash(c.getName());
             int broker_no = HashCalculator.decideWhereToSend(hash);

             ConnectionMetadata metadata = connectionMetadata1.get(broker_no-1);

             Message m1 = new Message(c.getName());
             Message m2 = new Message(c.getFilepath());

             // send it's name and the local path
             publisher.sendMessage(m1, metadata);
             publisher.sendMessage(m2, metadata);

             // send it's tags
             for (String tag : c.getAssociatedTags()) {
                 Message m = new Message(tag);
                 publisher.sendMessage(m, metadata);
             }

             Message m = new Message("END OF TAGS");
             publisher.sendMessage(m, metadata);

             // send it's videos
            MultimediaList channelMultimedia = channelTable.get(c.getName());

            for (VideoFile v : channelMultimedia) {
                Message v_m1 = new Message(v.getVideoName());
                Message v_m2 = new Message(v.getFilepath());
                Message v_m3 = new Message(String.valueOf(v.getBytes()));
                Message v_m4 = new Message(String.valueOf(v.getDuration()));
                Message v_m5 = new Message(String.valueOf(v.getFramerate()));
                Message v_m6 = new Message(String.valueOf(v.getFrameWidth()));
                Message v_m7 = new Message(String.valueOf(v.getFrameHeight()));
                Message v_m8 = new Message(String.valueOf(v.getDateCreated().toString()));

                publisher.sendMessage(v_m1, metadata);
                publisher.sendMessage(v_m2, metadata);
                publisher.sendMessage(v_m3, metadata);
                publisher.sendMessage(v_m4, metadata);
                publisher.sendMessage(v_m5, metadata);
                publisher.sendMessage(v_m6, metadata);
                publisher.sendMessage(v_m7, metadata);
                publisher.sendMessage(v_m8, metadata);

                // send video tags
                for (String tag : v.getAssociatedTags()) {
                    Message vm = new Message(tag);
                    publisher.sendMessage(vm, metadata);
                }

                Message eot = new Message("END OF TAGS");
                publisher.sendMessage(eot, metadata);
            }

             Message eov = new Message("END OF VIDEOS");
             publisher.sendMessage(eov, metadata);

         }

         Message m = new Message("END OF CHANNELS");

         if (BrokerConfiguration.CONNECT_ONLY_TO_ONE_BROKER == true) {
             ConnectionMetadata metadata = connectionMetadata1.get(0);
             publisher.sendMessage(m, metadata);
         } else {
             for (int i = 0; i < 3; i++) {
                 ConnectionMetadata metadata = connectionMetadata1.get(i);
                 publisher.sendMessage(m, metadata);
             }
         }
     }




    // helper functions

    public ChannelList getChannels() {
        return channels;
    }

    public void setChannels(ChannelList channels) {
        this.channels = channels;
    }

    public MultimediaList getMultimedias() {
        return multimedias;
    }

    public void setMultimedias(MultimediaList multimedias) {
        this.multimedias = multimedias;
    }

    public TagsHashtable getTagTable() {
        return tagTable;
    }

    public void setTagTable(TagsHashtable tagTable) {
        this.tagTable = tagTable;
    }

    public ChannelHashtable getChannelTable() {
        return channelTable;
    }

    public void setChannelTable(ChannelHashtable channelTable) {
        this.channelTable = channelTable;
    }

    public VideoFile find(String channel, String videoName) {
        MultimediaList videoFiles = channelTable.get(channel);

        for (VideoFile vf : videoFiles) {
            if (vf.getVideoName().equals(videoName)) {
                return vf;
            }
        }

        return null;
    }

    public String getProfileDirectory() {
        return profileDirectory;
    }

    public VideoFile findByTag(String tag, String videoName) {
        MultimediaList videoFiles = tagTable.get(tag);

        for (VideoFile vf : videoFiles) {
            if (vf.getVideoName().equals(videoName)) {
                return vf;
            }
        }

        return null;
    }
}
