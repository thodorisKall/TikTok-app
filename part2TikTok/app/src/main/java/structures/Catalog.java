package structures;

import communication.ConnectionMetadata;
import communication.Message;
import config.BrokerConfiguration;
import hashing.HashCalculator;
import nodes.Publisher;
//import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public int load(String profileDirectory) throws IOException, SAXException /*TikaException*/ {
        this.profileDirectory = profileDirectory;
        File dir = new File(profileDirectory);


        int counter = 0;
        for (File file : dir.listFiles()) { // for each channel
            if (file.isDirectory()) {
                if (file.getName().equals("downloads") || file.getName().equals("tags")) {
                    continue;
                }

                String channelName = file.getName();
                Channel channel= new Channel(channelName, file.getCanonicalPath());

                channels.add(channel);

                if (!channelTable.containsKey(channel.getName())) {
                    channelTable.put(channel.getName(), new MultimediaList());
                }

                for (File video : file.listFiles()) { // for each video in that channel
                    VideoFile media = new VideoFile(video.getName(), channelName , video.getCanonicalPath());
                    multimedias.add(media);
                    channelTable.get(channel.getName()).add(media);
                    counter++;
                }
            } else {
                System.out.println("There is not such directory!") ;
            }
        }

        Collections.sort(channels);

        for (Channel cha : channels) {
            MultimediaList videoFiles = channelTable.get(cha.getName());
//            List<VideoFile> lvf = Arrays.asList();
//            Collections.sort(lvf);
            Collections.sort(videoFiles);
        }

        return counter;
    }

    public void loadTags(String tagsDirectory) throws IOException {
        File dir = new File(tagsDirectory);

        for (File file : dir.listFiles()) { // for each tag file in tags ..
            if (file.isDirectory()) {
                continue;
            }

            if (file.getName().endsWith(".mp4")) {
                String videoName = file.getName();

                VideoFile media = multimedias.findByTitle(videoName);

                if (media == null) {
                    //System.out.println("Skipped: " + file.getName());
                    continue;
                }

                List<String> allLines = Files.readAllLines(Paths.get(file.getCanonicalPath()));

                for (String tag : allLines) {
                    if (!tagTable.containsKey(tag)) {
                        tagTable.put(tag, new MultimediaList());
                    }

                    media.getAssociatedTags().add(tag);

                    tagTable.get(tag).add(media);

                }
            } else {
                String channelName =  file.getName();

                Channel c = channels.findByTitle(channelName);

                if (c == null) {
                    //System.out.println("Skipped: " + file.getName());
                    continue;
                }

                List<String> allLines = Files.readAllLines(Paths.get(file.getCanonicalPath()));

                for (String tag : allLines) {
                    if (!tagTable.containsKey(tag)) {
                        tagTable.put(tag, new MultimediaList());
                    }

                    c.getAssociatedTags().add(tag);
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

                // send video tags  - bug - videos are sent to the broker of the channel instead of the broker of the tag
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


    public String [] getChannelArray() {
        String [] array = new String[channels.size()];

        for (int i=0;i<array.length;i++) {
            array[i] = channels.get(i).getName();
        }


        return array;
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

    public boolean addHashTagToChannel(String channel, String tag) {
        for (Channel c : channels) {
            if (c.getName().equals(channel)) {
                if (c.getAssociatedTags().contains(tag) == false) {
                    c.getAssociatedTags().add(tag);
                    return true;
                } else {
                    System.out.println("The tag already exists!");
                    return false;
                }
            }
        }
        return false;
    }

    public  boolean removeHashTagFromChannel(String channel, String tag) {
        for (Channel c : channels) {
            if (c.getName().equals(channel)) {
                if (c.getAssociatedTags().contains(tag)) {
                    c.getAssociatedTags().remove(tag);
                    return true;
                }else{
                    System.out.println("No such tag exists!");
                }
            }
        }
        return false;
    }

    public  boolean addHashTagToAVideoChannel(String channel, String video, String tag) {
        MultimediaList videoFiles = this.channelTable.get(channel);

        if (videoFiles == null) {
            return false;
        }
        for (VideoFile v : videoFiles) {
            if (v.getVideoName().equals(video)) {
                if(!v.getAssociatedTags().contains(tag)) {
                    v.getAssociatedTags().add(tag);
                    return true;
                }else{
                    System.out.println("The tag already exists!");
                }
            }
        }
        return false;
    }

    public  boolean removeHashTagFromVideo(String channel, String video, String tag) {
        MultimediaList videoFiles = this.channelTable.get(channel);

        if (videoFiles == null) {
            return false;
        }

        for (VideoFile v : videoFiles) {
            if (v.getVideoName().equals(video)) {
                if(v.getAssociatedTags().contains(tag)) {
                    v.getAssociatedTags().remove(tag);
                    return true;
                } else{
                    System.out.println("No such tag exists!");
                }
            }
        }
        return false;
    }

    public  boolean addVideo(String channelName, String videoName) {
        try {
            String videoPath = profileDirectory + File.separator + channelName + File.separator + videoName;

            File path = new File(videoPath);

            if (path.exists()) {
                VideoFile media = new VideoFile(videoName, channelName, path.getCanonicalPath());

                multimedias.add(media);

                channelTable.get(channelName).add(media);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public  boolean removeVideo(String channel, String video) {
        MultimediaList videoFiles = this.channelTable.get(channel);

        if (videoFiles == null) {
            return false;
        }
        for (VideoFile v : videoFiles) {
            if (v.getVideoName().equals(video)) {
                videoFiles.remove(v);
                return true;
            }
        }
        return false;
    }

    public boolean createChannel(String channelName) {
        for (Channel c : channels) {
            if (c.getName().equals(channelName)) {
                return false;
            }
        }

        channels.add(new Channel(channelName, profileDirectory + "\\" + channelName));
        return true;
    }

    public boolean deleteChannel(String channelName) {
        for (Channel c : channels) {
            if (c.getName().equals(channelName)) {
                channels.remove(c);
                return true;
            }
        }
        return false;
    }

    public VideoFile[] getVideoArray(String channel) {
        MultimediaList videoFiles = channelTable.get(channel);

        VideoFile[] array = new VideoFile[videoFiles.size()];

        for (int i=0;i<videoFiles.size();i++) {
            array[i] = videoFiles.get(i);
        }

        return array;
    }
}
