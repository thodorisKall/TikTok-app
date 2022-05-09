package nodes;

import communication.ConnectionMetadata;
import communication.Message;
import config.BrokerConfiguration;
import hashing.HashCalculator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;

public class Consumer extends Node {

    private String resourceDirectory;
    private String username;
    private boolean registered = false;

    public Consumer(String username, String resourceDirectory) {
        super();
        this.username = username;
        this.resourceDirectory = resourceDirectory;
    }

    public void playData() {

    }

    protected void handshake(ConnectionMetadata connectionMetadata) throws IOException, ClassNotFoundException {
        Message m = new Message("REGISTER AS CONSUMER");

        Message m0 = new Message(username);

        super.sendMessage(m, connectionMetadata);
        super.sendMessage(m0, connectionMetadata);

        Message r= super.receiveMessage(connectionMetadata);

        System.out.println("Response received:" + r);
    }

    public String register(String IP, int port) throws IOException, ClassNotFoundException {
        ConnectionMetadata connectionMetadata1 = super.connect(IP, port);
        handshake(connectionMetadata1);
        getSocketMetadataFromFirstBroker(connectionMetadata1); // register and get receive IP and PORT from each broker
        connectionMetadataForEachBroker.add(connectionMetadata1);

        if (BrokerConfiguration.CONNECT_ONLY_TO_ONE_BROKER == false ) {
            ConnectionMetadata connectionMetadata2 = super.connect(BrokerConfiguration.BROKER_IP_2, BrokerConfiguration.BROKER_PORT_2);
            handshake(connectionMetadata2);
            getSocketMetadataFromOtherBrokers(connectionMetadata2);
            connectionMetadataForEachBroker.add(connectionMetadata2);

            ConnectionMetadata connectionMetadata3 = super.connect(BrokerConfiguration.BROKER_IP_3, BrokerConfiguration.BROKER_PORT_3);
            handshake(connectionMetadata3);
            getSocketMetadataFromOtherBrokers(connectionMetadata3); //
            connectionMetadataForEachBroker.add(connectionMetadata3);
        }

        registered = true;

        return "SUCCESS";
    }

    public String [] askForChannels() throws IOException, ClassNotFoundException {
        ArrayList<String> data =new ArrayList<String>();

        for (ConnectionMetadata metadata : super.getConnectionMetadataForEachBroker()) {
            Message m = new Message("ask channels");
            super.sendMessage(m, metadata);

            while (true) {
                m = super.receiveMessage(metadata);
                if (m.getData().equals("OK")) {
                    break;
                } else {
                    System.out.println("Channel available: " + m.getData());
                    data.add(m.getData());
                }
            }
        }

        String [] result = new String[data.size()];

        data.toArray(result);

        return result;
    }

    public String [] askForTags() throws IOException, ClassNotFoundException {
        ArrayList<String> data =new ArrayList<String>();

        for (ConnectionMetadata metadata : super.getConnectionMetadataForEachBroker()) {
            Message m = new Message("ask tags");
            super.sendMessage(m, metadata);

            while (true) {
                m = super.receiveMessage(metadata);
                if (m.getData().equals("OK")) {
                    break;
                } else {
                    System.out.println("tag available: " + m.getData());
                    data.add("#" + m.getData());
                }
            }
        }

        String [] result = new String[data.size()];

        data.toArray(result);

        return result;
    }

    public String[]  askForVideosOfChannel(String channel) throws IOException, ClassNotFoundException {
        BigInteger hash = HashCalculator.hash(channel);
        int broker_no = HashCalculator.decideWhereToSend(hash) - 1;

        ArrayList<String> data =new ArrayList<String>();

        ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);

        Message m1 = new Message("ask videos of a channel");
        super.sendMessage(m1, metadata);

        Message m2 = new Message(channel);
        super.sendMessage(m2, metadata);

        int hits = 0;
        while (true) {
            Message  m = super.receiveMessage(metadata);
            if (m.getData().equals("OK")) {
                break;
            } else {
                System.out.println("video available: " + m.getData());
                data.add(m.getData());
                hits++;
            }
        }
        if (hits == 0) {
            System.out.println("Nothing found");
        }

        String [] result = new String[data.size()];

        data.toArray(result);

        return result;
    }

    public String[]  askForVideosOfTag(String tag) throws IOException, ClassNotFoundException {
        BigInteger hash = HashCalculator.hash(tag);
        int broker_no = HashCalculator.decideWhereToSend(hash) - 1;

        ArrayList<String> data =new ArrayList<String>();

        ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);

        Message m1 = new Message("ask videos of a tag");
        super.sendMessage(m1, metadata);

        Message m2 = new Message(tag);
        super.sendMessage(m2, metadata);

        int hits = 0;

        while (true) {
            Message  m = super.receiveMessage(metadata);
            if (m.getData().equals("OK")) {
                break;
            } else {
                System.out.println("video available: " + m.getData());
                data.add(m.getData());
                hits++;
            }
        }

        if (hits == 0) {
            System.out.println("Nothing found");
        }

        String [] result = new String[data.size()];

        data.toArray(result);

        return result;
    }

    public void askForVideoOfChannel(String channel, String video, String videopath) throws IOException, ClassNotFoundException { // pull
        BigInteger hash = HashCalculator.hash(channel);
        int broker_no = HashCalculator.decideWhereToSend(hash) - 1;

        ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);

        Message m1 = new Message("ask video of a channel");
        super.sendMessage(m1, metadata);

        Message m2 = new Message(channel);
        super.sendMessage(m2, metadata);

        Message m3 = new Message(video);
        super.sendMessage(m3, metadata);

        Message res = super.receiveMessage(metadata);

        if (res.getData().equals("FOUND")) {
            System.out.println("video found, waiting for data ...");

            File file = new File(videopath);

            Message prefix = super.receiveMessage(metadata);

            System.out.println("Bytes to be received: " + prefix);

            long chunk = 512;

            long fileSize = Long.parseLong(prefix.toString());

            long completeChunks = fileSize / chunk;
            long remaining = fileSize % chunk;

            try (OutputStream outputStream = new FileOutputStream(file);) {
                byte[] buffer = new byte[(int) chunk];

                for (int i=0;i<completeChunks;i++) {
                    long bytes_read = 0;

                    while (bytes_read < chunk) {
                        long missing = chunk - bytes_read;
                        int k = metadata.getObjectInputStream().read(buffer, (int)bytes_read, (int) missing);
                        bytes_read = bytes_read + k;
                    }

                    outputStream.write(buffer); // publisher disk
                    outputStream.flush();
                }

                if (remaining >0) {
                    byte[] bufferrem = new byte[(int) remaining];

                    long bytes_read = 0;

                    while (bytes_read < remaining) {
                        long missing = remaining - bytes_read;
                        int k = metadata.getObjectInputStream().read(bufferrem, (int)bytes_read, (int) missing);
                        bytes_read = bytes_read + k;
                    }
                    outputStream.write(bufferrem);
                    outputStream.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("Video saved to : " + videopath);
        } else {
            System.out.println("video not found ...");
        }
    }

    public void askForVideoOfTag(String tag, String video, String videopath) throws IOException, ClassNotFoundException { // pull
        BigInteger hash = HashCalculator.hash(tag);
        int broker_no = HashCalculator.decideWhereToSend(hash) - 1;

        ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);
        Message m1 = new Message("ask video of a tag");
        super.sendMessage(m1, metadata);

        Message m2 = new Message(tag);
        super.sendMessage(m2, metadata);

        Message m3 = new Message(video);
        super.sendMessage(m3, metadata);

        Message res = super.receiveMessage(metadata);

        if (res.getData().equals("FOUND")) {
            System.out.println("video found, waiting for data ...");

            File file = new File(videopath);

            Message prefix = super.receiveMessage(metadata);

            System.out.println("Bytes to be received: " + prefix);

            long chunk = 512;

            long fileSize = Long.parseLong(prefix.toString());

            long completeChunks = fileSize / chunk;
            long remaining = fileSize % chunk;

            try (OutputStream outputStream = new FileOutputStream(file);) {
                byte[] buffer = new byte[(int) chunk];

                for (int i=0;i<completeChunks;i++) {
                    long bytes_read = 0;

                    while (bytes_read < chunk) {
                        long missing = chunk - bytes_read;
                        int k = metadata.getObjectInputStream().read(buffer, (int)bytes_read, (int) missing);
                        bytes_read = bytes_read + k;
                    }

                    outputStream.write(buffer); // publisher disk
                    outputStream.flush();
                }

                if (remaining >0) {
                    byte[] bufferrem = new byte[(int) remaining];

                    long bytes_read = 0;

                    while (bytes_read < remaining) {
                        long missing = remaining - bytes_read;
                        int k = metadata.getObjectInputStream().read(bufferrem, (int)bytes_read, (int) missing);
                        bytes_read = bytes_read + k;
                    }
                    outputStream.write(bufferrem);
                    outputStream.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("Video saved to : " + videopath);
        } else {
            System.out.println("video not found ...");
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isRegistered() {
        return registered;
    }
}


