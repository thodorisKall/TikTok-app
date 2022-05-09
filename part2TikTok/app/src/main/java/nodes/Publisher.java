package nodes;

import communication.ConnectionMetadata;
import communication.Message;
import config.BrokerConfiguration;
import hashing.HashCalculator;
import structures.Catalog;
import structures.VideoFile;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLOutput;

public class Publisher extends Node {
    public final String username;
    private Catalog catalog;
    private String initial_publisher_ip;
    private int initial_publisher_port;
    private ServerSocket providerSocket = null;
    private ServiceThread t = null;
    private boolean registered = false;


    public Publisher(String username, Catalog catalog, String initial_publisher_ip, int initial_publisher_port) {
        this.username = username;
        this.catalog = catalog;
        this.initial_publisher_ip = initial_publisher_ip;
        this.initial_publisher_port = initial_publisher_port;
    }

    protected void handshake(ConnectionMetadata connectionMetadata) throws IOException, ClassNotFoundException {
        Message m = new Message("REGISTER AS PUBLISHER");

        super.sendMessage(m, connectionMetadata);

        // send my socket:
        Message m0 = new Message(username);
        Message m1 = new Message(initial_publisher_ip);
        Message m2 = new Message(initial_publisher_port + "");

        sendMessage(m0, connectionMetadata);
        sendMessage(m1, connectionMetadata);
        sendMessage(m2, connectionMetadata);

        Message r= super.receiveMessage(connectionMetadata);

        System.out.println("Response received:" + r);
    }

    protected void sendCatalogToBrokers(Publisher publisher, Catalog catalog) throws IOException {
        catalog.sendToBrokers(publisher, connectionMetadataForEachBroker);
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

        sendCatalogToBrokers(this, catalog);

        registered = true;

        return "SUCCESS";
    }


    public String getInitial_publisher_ip() {
        return initial_publisher_ip;
    }

    public void setInitial_publisher_ip(String initial_publisher_ip) {
        this.initial_publisher_ip = initial_publisher_ip;
    }

    public int getInitial_publisher_port() {
        return initial_publisher_port;
    }

    public void setInitial_publisher_port(int initial_publisher_port) {
        this.initial_publisher_port = initial_publisher_port;
    }

    public void push(String channelName , String Value) {
    }

    public void notifyFailure(Broker b) {
    }

    public void notifyBrokersForHashTags(String s) {
    }

    public void closeServer() {
        System.out.println("CLosing server down");
        if (providerSocket != null) {
            try {
                providerSocket.close();
            } catch (IOException e) {
            }
        }

        if (t != null) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Server closed");
    }

    public void addHashTagToChannel(String channel, String tag) {
        if (catalog.addHashTagToChannel(channel, tag)) {
            BigInteger hash = HashCalculator.hash(channel);
            int broker_no = HashCalculator.decideWhereToSend(hash) - 1;

            try {
                ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);
                Message m1 = new Message("addHashTagToChannel");
                super.sendMessage(m1, metadata);

                Message m2 = new Message(channel);
                super.sendMessage(m2, metadata);

                Message m3 = new Message(tag);
                super.sendMessage(m3, metadata);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Command failed\n");
        }
    }

    public void addHashTagToAVideoChannel(String channel, String video, String tag) {
        if (catalog.addHashTagToAVideoChannel(channel, video, tag)) {
            BigInteger hash = HashCalculator.hash(channel);
            int broker_no = HashCalculator.decideWhereToSend(hash) - 1;

            try {
                ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);
                Message m1 = new Message("addHashTagToAVideoChannel");
                super.sendMessage(m1, metadata);

                Message m2 = new Message(channel);
                super.sendMessage(m2, metadata);

                Message m3 = new Message(video);
                super.sendMessage(m3, metadata);

                Message m4 = new Message(tag);
                super.sendMessage(m4, metadata);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Command failed\n");
        }
    }

    public void removeHashTagFromChannel(String channel, String tag) {
        if (catalog.removeHashTagFromChannel(channel, tag)) {
            BigInteger hash = HashCalculator.hash(channel);
            int broker_no = HashCalculator.decideWhereToSend(hash) - 1;
            try {
                ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);
                Message m1 = new Message("removeHashTagFromChannel");
                super.sendMessage(m1, metadata);

                Message m2 = new Message(channel);
                super.sendMessage(m2, metadata);

                Message m3 = new Message(tag);
                super.sendMessage(m3, metadata);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            System.out.println("Command failed\n");
        }
    }

    public void removeHashTagFromVideo(String channel, String video, String tag) {
        if (catalog.removeHashTagFromVideo(channel, video, tag)) {
            BigInteger hash = HashCalculator.hash(channel);
            int broker_no = HashCalculator.decideWhereToSend(hash) - 1;

            try {
                ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);
                Message m1 = new Message("removeHashTagFromVideo");
                super.sendMessage(m1, metadata);

                Message m2 = new Message(channel);
                super.sendMessage(m2, metadata);

                Message m3 = new Message(video);
                super.sendMessage(m3, metadata);

                Message m4 = new Message(tag);
                super.sendMessage(m4, metadata);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Command failed\n");
        }
    }

    public void addVideo(String channel, String video) {
        if (catalog.addVideo(channel, video)) {
            BigInteger hash = HashCalculator.hash(channel);
            int broker_no = HashCalculator.decideWhereToSend(hash) - 1;
            try {
                ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);
                Message m1 = new Message("addVideo");
                super.sendMessage(m1, metadata);

                Message m2 = new Message(channel);
                super.sendMessage(m2, metadata);

                Message m3 = new Message(video);
                super.sendMessage(m3, metadata);


            } catch (Exception ex) {

            }
        } else {
            System.out.println("Command failed\n");
        }
    }

    public void removeVideo(String channel, String video) {
        if (catalog.removeVideo(channel, video)) {
            BigInteger hash = HashCalculator.hash(channel);
            int broker_no = HashCalculator.decideWhereToSend(hash) - 1;
            try {
                ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);
                Message m1 = new Message("removeVideo");
                super.sendMessage(m1, metadata);

                Message m2 = new Message(channel);
                super.sendMessage(m2, metadata);

                Message m3 = new Message(video);
                super.sendMessage(m3, metadata);

            } catch (Exception ex) {

            }
        } else {
            System.out.println("Command failed\n");
        }
    }

    public void createChannel(String channel) {
        if (catalog.createChannel(channel)) {
            BigInteger hash = HashCalculator.hash(channel);
            int broker_no = HashCalculator.decideWhereToSend(hash) - 1;
            try {
                ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);
                Message m1 = new Message("createChannel");
                super.sendMessage(m1, metadata);

                Message m2 = new Message(channel);
                super.sendMessage(m2, metadata);
            } catch (Exception ex) {

            }
        } else {
            System.out.println("Command failed\n");
        }
    }

    public void deleteChannel(String channel) {
        if (catalog.deleteChannel(channel)) {
            BigInteger hash = HashCalculator.hash(channel);
            int broker_no = HashCalculator.decideWhereToSend(hash) - 1;
            try {
                ConnectionMetadata metadata = super.getConnectionMetadataForEachBroker().get(broker_no);
                Message m1 = new Message("deleteChannel");
                super.sendMessage(m1, metadata);

                Message m2 = new Message(channel);
                super.sendMessage(m2, metadata);
            } catch (Exception ex) {

            }
        } else {
            System.out.println("Command failed\n");
        }
    }

    public boolean isServerActive() {
        return providerSocket != null;
    }

    class ActionsForBroker extends Thread {
        private ConnectionMetadata connectionMetadata;

        public ActionsForBroker(ConnectionMetadata connectionMetadata) {
            this.connectionMetadata = connectionMetadata;
        }

        public void run() {
            try {
                Message m = receiveMessage(connectionMetadata);

                if (m.getData().equals("ask video of a channel")) { // 5
                    Message m2 = receiveMessage(connectionMetadata);
                    String channel = m2.getData();

                    Message m3 = receiveMessage(connectionMetadata);
                    String videoName = m3.getData();

                    System.out.println("Request received for: " + channel + " " + videoName);

                    VideoFile vf = catalog.find(channel, videoName);

                    System.out.println("Video file found: " + vf);

                    long chunk = 512;

                    long fileSize = vf.getBytes();;

                    long completeChunks = fileSize / chunk;
                    long remaining = fileSize % chunk;

                    Message prefix = new Message("" + fileSize);
                    sendMessage(prefix, connectionMetadata);


                    try (InputStream inputStream = new FileInputStream(vf.getFilepath());) {
                        byte[] buffer = new byte[(int) chunk];

                        for (int i = 0; i < completeChunks; i++) {
                            long bytes_read = 0;

                            while (bytes_read < chunk) {
                                long missing = chunk - bytes_read;
                                int k = inputStream.read(buffer, (int) bytes_read, (int) missing);
                                bytes_read = bytes_read + k;
                            }
                            connectionMetadata.getObjectOutputStream().write(buffer);
                            connectionMetadata.getObjectOutputStream().flush();
                        }

                        if (remaining > 0) {
                            byte[] bufferrem = new byte[(int) remaining];

                            long bytes_read = 0;

                            while (bytes_read < remaining) {
                                long missing = remaining - bytes_read;
                                int k = inputStream.read(bufferrem, (int) bytes_read, (int) missing);
                                bytes_read = bytes_read + k;
                            }

                            connectionMetadata.getObjectOutputStream().write(bufferrem);
                            connectionMetadata.getObjectOutputStream().flush();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                } else if (m.getData().equals("ask video of a tag")) {
                    Message m2 = receiveMessage(connectionMetadata);
                    String tag = m2.getData();

                    Message m3 = receiveMessage(connectionMetadata);
                    String videoName = m3.getData();

                    System.out.println("Request received for: " + tag + " " + videoName);

                    VideoFile vf = catalog.findByTag(tag, videoName);

                    System.out.println("Video file found: " + vf);

                    long chunk = 512;

                    long fileSize = vf.getBytes();

                    long completeChunks = fileSize / chunk;
                    long remaining = fileSize % chunk;

                    Message prefix = new Message("" + fileSize);
                    sendMessage(prefix, connectionMetadata);


                    try (InputStream inputStream = new FileInputStream(vf.getFilepath());) {
                        byte[] buffer = new byte[(int) chunk];

                        for (int i = 0; i < completeChunks; i++) {
                            long bytes_read = 0;

                            while (bytes_read < chunk) {
                                long missing = chunk - bytes_read;
                                int k = inputStream.read(buffer, (int) bytes_read, (int) missing);
                                bytes_read = bytes_read + k;
                            }
                            connectionMetadata.getObjectOutputStream().write(buffer);
                            connectionMetadata.getObjectOutputStream().flush();
                        }

                        if (remaining > 0) {
                            byte[] bufferrem = new byte[(int) remaining];

                            long bytes_read = 0;

                            while (bytes_read < remaining) {
                                long missing = remaining - bytes_read;
                                int k = inputStream.read(bufferrem, (int) bytes_read, (int) missing);
                                bytes_read = bytes_read + k;
                            }

                            connectionMetadata.getObjectOutputStream().write(bufferrem);
                            connectionMetadata.getObjectOutputStream().flush();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception ex ) {
                ex.printStackTrace();
            }
        }
    }

    class ServiceThread extends Thread {

        public ConnectionMetadata accept(ServerSocket providerSocket) throws IOException {
            Socket communicationSocket = providerSocket.accept();

            System.out.println("Client connected ");

            ObjectInputStream objectInputStream;
            ObjectOutputStream objectOutputStream;

            objectOutputStream = new ObjectOutputStream(communicationSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(communicationSocket.getInputStream());

            ConnectionMetadata metadata = new ConnectionMetadata(communicationSocket, objectOutputStream, objectInputStream);
            return metadata;
        }

        public void run() {

            try {
                System.out.println("Opening incoming socket at:" + initial_publisher_port);
                providerSocket = new ServerSocket(initial_publisher_port, 50);

                System.out.println("Socket Opened at:" + initial_publisher_port);

                while (true) {
                    //Αναμονή για σύνδεση με broker
                    System.out.println("Waiting for a connection from broker on port:" + initial_publisher_port + " ...");

                    ConnectionMetadata connectionMetadata = accept(providerSocket);

                    ActionsForBroker t = new ActionsForBroker(connectionMetadata);
                    t.start();
                }
            } catch (SocketException se) {
                System.out.println("Provider socket closed.");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    providerSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                providerSocket= null;
            }
        }
    }

    public boolean isRegistered() {
        return registered;
    }

    public void openServer() {
        t = new ServiceThread();
        t.start();
    }
}
