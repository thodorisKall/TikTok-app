package structures;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Catalog {

    ChannelList channels = new ChannelList();
    MultimediaList multimedias = new MultimediaList(); // multimedia list
    TagsHashtable tagTable = new TagsHashtable(); // tag -> multimedia
    ChannelHashtable channelTable = new ChannelHashtable(); // channel -> multimedia

    public void load(String profileDirectory) throws IOException, TikaException, SAXException {
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

    public Set<String> getTagList() {
        Set<String> set = new HashSet<>();

        for (String tag : tagTable.keySet()) {
            set.add(tag);
        }

        return set;
    }
}
