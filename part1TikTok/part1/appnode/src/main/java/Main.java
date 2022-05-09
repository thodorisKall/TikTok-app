import communication.ConnectionMetadata;
import communication.Message;
import hashing.HashCalculator;
import nodes.Consumer;
import nodes.Publisher;
import structures.Catalog;
import structures.Channel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static final  String username ="user2";

    public static void main(String [] args) {
        final String currentDirectory = System.getProperty("user.dir");
        final String profileDirectory = username;
        final String resourceDirectory = currentDirectory + "\\" + profileDirectory;
        final String downloadsDirectory = currentDirectory + "\\" + profileDirectory + "\\" + "downloads";
        final String tagsDirectory = currentDirectory + "\\" + profileDirectory + "\\" + "tags";
        final String INITIAL_BROKER_IP = "192.168.2.17";
        final int INITIAL_BROKER_PORT = 14321;

        final String INITIAL_PUBLISHER_IP = "192.168.2.17";
        final int seed = Math.abs((new Random().nextInt())%30000);
        final int INITIAL_PUBLISHER_PORT = 20000 + seed;

        System.out.println("Starting Appnode");
        System.out.println("Username: " + username);
        System.out.println("seed            : " + seed);
        System.out.println("Incoming IP:PORT: " + INITIAL_PUBLISHER_IP + ":" + INITIAL_PUBLISHER_PORT);
        System.out.println("Working directory  : " + profileDirectory);
        System.out.println("Resource directory : " + resourceDirectory);
        System.out.println("Downloads directory: " + downloadsDirectory);
        System.out.println("Tags directory     : " + tagsDirectory);

        System.out.println("Loading data from disk ... ");

        try {
            Catalog catalog = new Catalog();

            catalog.load(resourceDirectory);

            catalog.loadTags(tagsDirectory);

            catalog.print();

            Publisher p = new Publisher(username, catalog, INITIAL_PUBLISHER_IP, INITIAL_PUBLISHER_PORT);

            p.openServer();

            p.register(INITIAL_BROKER_IP, INITIAL_BROKER_PORT);

            Consumer c = new Consumer(username, resourceDirectory);

            c.register(INITIAL_BROKER_IP, INITIAL_BROKER_PORT);

            menu(p, c, catalog);

            p.logout();

            c.logout();

            p.closeServer();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        System.out.println("Exit successful");
    }

    public static void menuPublisher(Publisher p,Catalog catalog) {
        System.out.println("---------------------------------------------");
        System.out.println("                 Menu publisher ");
        System.out.println("---------------------------------------------");

        System.out.println("Press 1 to add a hashtag to a channel");
        System.out.println("Press 2 to add a hashtag to a video");
        System.out.println("Press 3 to remove a hashtag from a channel");
        System.out.println("Press 4 to remove a hashtag from a video");
        System.out.println("Press 5 to add a new video ");  // ??
        System.out.println("Press 6 to remove a new video ");  // ??
        System.out.println("Press 0 to exit");

        do {
            System.out.print("Type your choice: ");

            Scanner s = new Scanner(System.in);
            String line = s.nextLine();

            if (line.equals("1")) {
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which tag? ");
                String tag = s.nextLine();
            } else if (line.equals("2")){
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which video? ");
                String video = s.nextLine();
                System.out.print("Which tag? ");
                String tag = s.nextLine();
            } else if (line.equals("3")) {
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which tag? ");
                String tag = s.nextLine();
            } else if (line.equals("4")) {
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which video? ");
                String video = s.nextLine();
                System.out.print("Which tag? ");
                String tag = s.nextLine();
            } else if (line.equals("5")) {
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which video? ");
                String video = s.nextLine();
                System.out.print("Which tag? ");
                String tag = s.nextLine();
            } else if (line.equals("6")) {
                System.out.print("Which channel? ");
                String channel = s.nextLine();
                System.out.print("Which video? ");
                String video = s.nextLine();
            } else  if (line.equals("0")) {
                break;
            }
        } while (true);
    }

    public static void menuConsumer(Consumer consumer, Catalog catalog) throws IOException, ClassNotFoundException {
        System.out.println("---------------------------------------------");
        System.out.println("                 Menu consumer ");
        System.out.println("---------------------------------------------");

        System.out.println("Press 1 to ask for channels"); // ask each broker for its channels
        System.out.println("Press 2 to ask for tags"); // ask each broker for its tags
        System.out.println("Press 3 to ask for video list of a channel");
        System.out.println("Press 4 to ask for video list of a tag");
        System.out.println("Press 5 to ask for a video of a channel");
        System.out.println("Press 6 to ask for a video of a tag ???");
        System.out.println("Press 0 to exit");

        do {
            System.out.print("Type your choice: ");

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

                consumer.askForVideoOfChannel(channel, video);
            } else  if (line.equals("6")) {// ask to download a specific video of a specific tag
                System.out.print("Which Tag?");
                String tag = s.nextLine();

                System.out.print("Which video?");
                String video = s.nextLine();

                consumer.askForVideoOfTag(tag, video);
            } else  if (line.equals("0")) {
                break;
            }
        } while (true);
    }

    public static void menu(Publisher p, Consumer c, Catalog catalog) throws IOException, ClassNotFoundException {
        System.out.println("---------------------------------------------");
        System.out.println("                   Main menu ");
        System.out.println("---------------------------------------------");

        do {
            System.out.println("Press 1 for publisher menu");
            System.out.println("Press 2 for consumer menu");
            System.out.println("Press 3 to test hashfunction");
            System.out.println("Press 0 to exit");


            System.out.print("Type your choice: ");

            Scanner s = new Scanner(System.in);
            String line = s.nextLine();

            if (line.equals("1")) {
                menuPublisher(p, catalog);
            } else if (line.equals("2")){
                menuConsumer(c, catalog);
            } else if (line.equals("3")){
                testHashFunction(catalog);
            } else  if (line.equals("0")) {
                break;
            }
        } while (true);

    }

    private static void testHashFunction(Catalog catalog) {
        Publisher p = new Publisher("Bob", catalog, "", 0);

        for (Channel c : catalog.getChannels()) {
//            String hash = p.hashTopic(c.getName());
//            BigInteger i = p.hashToInteger(hash);
            BigInteger hash = p.hashTopic(c.getName());

            System.out.println("---------------------------------------------");
            System.out.println("                   Hashing ");
            System.out.println("---------------------------------------------");
            System.out.println("Hashing: " + hash);

            System.out.println("Channel " + c.getName() + " has hash: " +  hash);
        }
    }
}
