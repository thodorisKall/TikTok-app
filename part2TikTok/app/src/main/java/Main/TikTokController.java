package Main;

import org.xml.sax.SAXException;

import config.StorageConfiguration;
import nodes.Consumer;
import nodes.Publisher;
import structures.Catalog;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class TikTokController {
    public static final Catalog catalog = new Catalog();

    private static boolean loadComplete = false;
    private static boolean registerComplete = false;
    private static Publisher publisher = null;
    private static Consumer consumer = null;

    public static Consumer getConsumer() {
        return consumer;
    }

    public static boolean isLoaded() {
        return loadComplete == true;
    }

    public static boolean isRegisteredAsPublisher() {
        return publisher != null &&  publisher.isRegistered();
    }

    public static boolean isRegisteredAsConsumer() {
        return consumer != null &&  consumer.isRegistered();
    }

    public static void loadCatalog() throws IOException, SAXException {
        if (!loadComplete) {
            StorageConfiguration.setupDirectories(publisher.username);
            catalog.load(StorageConfiguration.resourceDirectory);
            catalog.loadTags(StorageConfiguration.tagsDirectory);
            loadComplete = true;
        }
    }

    public static void openServer(String username, String INITIAL_PUBLISHER_IP, int INITIAL_PUBLISHER_PORT) {
        if (publisher == null) {
            publisher = new Publisher(username, catalog, INITIAL_PUBLISHER_IP, INITIAL_PUBLISHER_PORT);
            publisher.openServer();
        } else {
            // already connected
        }
    }

    public static String registerAsPublisher(String INITIAL_BROKER_IP, int INITIAL_BROKER_PORT) throws IOException, ClassNotFoundException {
        return publisher.register(INITIAL_BROKER_IP, INITIAL_BROKER_PORT);
    }

    public static String registerAsConsumer(String INITIAL_BROKER_IP, int INITIAL_BROKER_PORT) throws IOException, ClassNotFoundException {
        if (consumer == null) {
            consumer = new Consumer(publisher.username, StorageConfiguration.resourceDirectory);
        }

        return consumer.register(INITIAL_BROKER_IP, INITIAL_BROKER_PORT);
    }

    public static boolean videoIsDownloaded(String topic, String key) {
        String path = StorageConfiguration.downloadsDirectory;
        String keypath = path + File.separator + topic;

        if (!new File(keypath).exists()) {
            return false;
        }

        String videopath = keypath + File.separator + key;

        if (!new File(videopath).exists()) {
            return false;
        }

        return true;
    }

    public static boolean downloadVideo(String topic, String key) throws IOException, ClassNotFoundException {
        String path = StorageConfiguration.downloadsDirectory;
        String keypath = path + File.separator + topic;

        File f1 = new File(keypath);

        if (!f1.exists()) {
            f1.mkdir();
        }

        String videopath = keypath + File.separator + key;

        File f2 = new File(videopath);

        if (f2.exists()) {
            return false;
        }

        if (topic.startsWith("#")) {
            topic = topic.substring(1); // remove hashtag
            consumer.askForVideoOfTag(topic, key, videopath);
        } else {
            consumer.askForVideoOfChannel(topic, key, videopath);
        }

        return true;
    }


    public static void main_qeqwe(String [] args) {

//            Consumer c = new Consumer(username, resourceDirectory);
//
//            c.register(INITIAL_BROKER_IP, INITIAL_BROKER_PORT);
//
//            menu(p, c, catalog);
//
//            p.logout();
//
//            c.logout();
//
//            p.closeServer();

    }

    public static void menuPublisher(Publisher p,Catalog catalog) {
        System.out.println("---------------------------------------------");
        System.out.println("                 Menu publisher ");
        System.out.println("---------------------------------------------");

        do {
            System.out.println("Press 1 to add a hashtag to a channel");
            System.out.println("Press 2 to add a hashtag to a video");
            System.out.println("Press 3 to remove a hashtag from a channel");
            System.out.println("Press 4 to remove a hashtag from a video");
            System.out.println("Press 5 to add a new video ");  // ??
            System.out.println("Press 6 to remove a new video ");  // ??
            System.out.println("Press 7 to create a channel ");  // ??
            System.out.println("Press 8 to remove a channel ");  // ??
            System.out.println("Press 9 print cache ");  // ??
            System.out.println("Press 0 to exit.\n");

            Scanner s = new Scanner(System.in);
            String line = s.nextLine();

            if (line.equals("1")) { // add hashtag to a channel
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which tag? ");
                String tag = s.nextLine();
                p.addHashTagToChannel(channel, tag);
            } else if (line.equals("2")){ // add hashtag to a video
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which video? ");
                String video = s.nextLine();
                System.out.print("Which tag? ");
                String tag = s.nextLine();
                p.addHashTagToAVideoChannel(channel, video, tag);
            } else if (line.equals("3")) { // remove hashtag from a channel
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which tag? ");
                String tag = s.nextLine();
                p.removeHashTagFromChannel(channel, tag);
            } else if (line.equals("4")) { // remove hashtag from a video
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which video? ");
                String video = s.nextLine();
                System.out.print("Which tag? ");
                String tag = s.nextLine();
                p.removeHashTagFromVideo(channel, video, tag);
            } else if (line.equals("5")) { // add video
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which video? ");
                String video = s.nextLine();
                p.addVideo(channel, video);
            } else if (line.equals("6")) { // remove video
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which video? ");
                String video = s.nextLine();
                p.removeVideo(channel, video);
            } else if (line.equals("7")) { // remove video
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                p.createChannel(channel);
            } else if (line.equals("8")) { // remove video
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                p.deleteChannel(channel);
            } else  if (line.equals("9")) {
                catalog.print();
            } else  if (line.equals("0")) {
                break;
            }
        } while (true);
    }

    public static void menuConsumer(Consumer consumer, Catalog catalog) throws IOException, ClassNotFoundException {
        do {
            System.out.println("---------------------------------------------");
            System.out.println("                 Menu consumer ");
            System.out.println("---------------------------------------------\n");

            System.out.println("Press 1 to ask for channels"); // ask each broker for its channels
            System.out.println("Press 2 to ask for tags"); // ask each broker for its tags
            System.out.println("Press 3 to ask for video list of a channel");
            System.out.println("Press 4 to ask for video list of a tag");
            System.out.println("Press 5 to ask for a video of a channel");
            System.out.println("Press 6 to ask for a video of a tag ???");
            System.out.println("Press 0 to exit.\n");

            Scanner s = new Scanner(System.in);
            String line = s.nextLine();

            if (line.equals("1")) { // ask for channels
                consumer.askForChannels();
            } else if (line.equals("2")){ // ask for tags
                consumer.askForTags();
            } else if (line.equals("3")){ // ask for videos of a channel
                System.out.print("Which channel?");
                String channel = s.nextLine();
                consumer.askForVideosOfChannel(channel);
            } else if (line.equals("4")){ // ask for videos of a tag
                System.out.print("Which tag?");
                String tag = s.nextLine();
                consumer.askForVideosOfTag(tag);
            } else  if (line.equals("5")) { // ask to download a specific video of a specific channel
                System.out.print("Which channel?");
                String channel = s.nextLine();

                System.out.print("Which video?");
                String video = s.nextLine();

//                consumer.askForVideoOfChannel(channel, video, videopath);
            } else  if (line.equals("6")) {// ask to download a specific video of a specific tag
                System.out.print("Which Tag?");
                String tag = s.nextLine();

                System.out.print("Which video?");
                String video = s.nextLine();

//                consumer.askForVideoOfTag(tag, video, videopath);
            } else  if (line.equals("0")) {
                break;
            }
        } while (true);
    }


    public static void shutdown() {
        if (publisher!=null) {
            publisher.closeServer();
            publisher = null;
        }
    }

    public static boolean serverActive() {
        return publisher!= null && publisher.isServerActive();
    }

    public static String getVideoPath(String topic, String key) {
        String path = StorageConfiguration.downloadsDirectory;
        String keypath = path + File.separator + topic;

        if (!new File(keypath).exists()) {
            return null;
        }

        String videopath = keypath + File.separator + key;

        if (!new File(videopath).exists()) {
            return null;
        }

        return videopath;
    }

    public static Publisher getPublisher() {
        return publisher;
    }
}
