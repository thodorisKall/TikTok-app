package nodes;

import communication.ConnectionMetadata;
import communication.Message;
import config.BrokerConfiguration;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;
import server.ConsumerData;
import server.PublisherData;
import structures.Catalog;
import structures.Channel;
import structures.MultimediaList;
import structures.VideoFile;

import java.io.*;
import java.util.*;

public class Broker extends Node {
    protected final Message responseOK = new Message("OK");
    protected final Message responseFound = new Message("FOUND");
    protected final Message responseMissing = new Message("MISSING");

    public List<ConsumerData> consumers = new ArrayList<>();
    public List<PublisherData> publishers = new ArrayList<PublisherData>();


    public String handShake(ConnectionMetadata connectionMetadata) throws IOException, ClassNotFoundException {
        System.out.println("Client connected (publisher or subscriber) ");

        Message m = super.receiveMessage(connectionMetadata);

        System.out.println("Message received: " + m.toString());

        System.out.println("Sending OK response ");

        super.sendMessage(responseOK, connectionMetadata);

        if (m.toString().equals("REGISTER AS PUBLISHER")) {

            Message m0 = super.receiveMessage(connectionMetadata);
            Message m1 = super.receiveMessage(connectionMetadata);
            Message m2 = super.receiveMessage(connectionMetadata);

            String username = m0.getData();
            String ip = m1.getData();
            int port = Integer.parseInt(m2.getData());

            connectionMetadata.setUsername(username);
            connectionMetadata.setIp(ip);
            connectionMetadata.setPort(port);

            return "PUBLISHER";
        } else {
            Message m0 = super.receiveMessage(connectionMetadata);
            String username = m0.getData();
            connectionMetadata.setUsername(username);

            return "CONSUMER";
        }
    }

    public void sendSocketMetadata(ConnectionMetadata connectionMetadata) throws IOException {
        Message m1a = new Message(BrokerConfiguration.BROKER_IP_1);
        Message m1b = new Message(String.valueOf(BrokerConfiguration.BROKER_PORT_1));
        Message m2a = new Message(BrokerConfiguration.BROKER_IP_2);
        Message m2b = new Message(String.valueOf(BrokerConfiguration.BROKER_PORT_2));
        Message m3a = new Message(BrokerConfiguration.BROKER_IP_3);
        Message m3b = new Message(String.valueOf(BrokerConfiguration.BROKER_PORT_3));

        super.sendMessage(m1a, connectionMetadata);
        super.sendMessage(m1b, connectionMetadata);
        super.sendMessage(m2a, connectionMetadata);
        super.sendMessage(m2b, connectionMetadata);
        super.sendMessage(m3a, connectionMetadata);
        super.sendMessage(m3b, connectionMetadata);
    }

    public void getCatalogFromPublisher(PublisherData publisher) throws IOException, ClassNotFoundException, TikaException, SAXException {
        ConnectionMetadata metadata = publisher.getConnectionMetadata();

        publisher.setCatalog(new Catalog());
        // receive channels
        while (true) {
            Message m1 = receiveMessage(metadata);
            if (m1.getData().equals("END OF CHANNELS")) {
                break;
            }

            Message m2 = receiveMessage(metadata);

            String channel = m1.getData();
            String path = m2.getData();

            Channel c = new Channel(channel, path);

            publisher.getCatalog().getChannels().add(c);

            System.out.println("Receiving data for channel: " + c.getName());

            if (!publisher.getCatalog().getChannelTable().containsKey(c.getName())) {
                publisher.getCatalog().getChannelTable().put(c.getName(), new MultimediaList());
            }

            // receive channel tags
            System.out.println("Receiving tags for channel: " + c.getName());

            while (true) {
                Message m3 = receiveMessage(metadata);
                if (m3.getData().equals("END OF TAGS")) {
                    break;
                }

                String tag = m3.getData();

                c.getAssociatedTags().add(tag);

                System.out.println("- " + tag);
            }

            // receive its videos
            while (true) {
                Message v_m1 = receiveMessage(metadata);
                if (v_m1.getData().equals("END OF VIDEOS")) {
                    break;
                }

                Message v_m2 = receiveMessage(metadata);
                Message v_m3 = receiveMessage(metadata);
                Message v_m4 = receiveMessage(metadata);
                Message v_m5 = receiveMessage(metadata);
                Message v_m6 = receiveMessage(metadata);
                Message v_m7 = receiveMessage(metadata);
                Message v_m8 = receiveMessage(metadata);

                String videoName = v_m1.getData();
                String filepath = v_m2.getData();
                String bytes = v_m3.getData();
                String duration = v_m4.getData();
                String framerate = v_m5.getData();
                String framewidth= v_m6.getData();
                String frameheight = v_m7.getData();
                String datecreated = v_m8.getData();

                VideoFile v = new VideoFile(videoName, c.getName(), filepath);

                v.setBytes(Long.parseLong(bytes));
               // v.setLength(Double.parseDouble(duration));
                v.setDuration(duration);
                v.setFramerate(framerate);
                v.setFrameHeight(frameheight);
                v.setFrameWidth(framewidth);
//                v.setFramerate(Double.parseDouble(framerate));
//                v.setFrameWidth(Integer.parseInt(framewidth));
//                v.setFrameHeight(Integer.parseInt(frameheight));
                v.setDateCreated(datecreated);
//                v.setDateCreated(new Date()); // TODO: fix later ...

                // receive video tags
                while (true) {
                    Message vt = receiveMessage(metadata);
                    if (vt.getData().equals("END OF TAGS")) {
                        break;
                    }

                    String tag = vt.getData();

                    v.getAssociatedTags().add(tag);

                    if (!publisher.getCatalog().getTagTable().containsKey(tag)) {
                        publisher.getCatalog().getTagTable().put(tag, new MultimediaList());
                    }

                    publisher.getCatalog().getTagTable().get(tag).add(v);
                }

                publisher.getCatalog().getMultimedias().add(v);
                publisher.getCatalog().getChannelTable().get(c.getName()).add(v);
            }

        }

        System.out.println("-----------------------------------------------------------");
        System.out.println("                        REceived:");
        System.out.println("-----------------------------------------------------------");

        publisher.getCatalog().print();


    }

    public void calculateKeys() {

    }

    public void acceptConnection(PublisherData newdata ) throws IOException, ClassNotFoundException, TikaException, SAXException {
        boolean duplicate = false;

        synchronized (publishers) {
            for (int i = 0; i < publishers.size(); i++) {
                PublisherData olddata = publishers.get(i);
                if (olddata.getConnectionMetadata().getUsername().equals(newdata.getConnectionMetadata().getUsername())) {
                    // already exists
                    duplicate = true;
                    publishers.set(i, newdata);
                }
            }

            if (!duplicate) {
                publishers.add(newdata);
            }
        }

        getCatalogFromPublisher(newdata);
    }

    public void acceptConnection(ConsumerData consumerData) throws IOException, ClassNotFoundException {
        boolean duplicate = false;

        synchronized (consumers) {
            for (int i = 0; i < consumers.size(); i++) {
                ConsumerData olddata = consumers.get(i);
                if (olddata.getConnectionMetadata().getUsername().equals(consumerData.getConnectionMetadata().getUsername())) {
                    // already exists
                    duplicate = true;
                    consumers.set(i, consumerData);
                }
            }

            if (!duplicate) {
                consumers.add(consumerData);
            }
        }

        ConnectionMetadata clientConnectionMetadata = consumerData.getConnectionMetadata();

        while (true) {
            System.out.println("Waiting for a command from a consumer ");

            Message m = super.receiveMessage(clientConnectionMetadata);


            if (m.getData().equals("ask channels")) { // 1
                System.out.println("Command received: " + m);

                synchronized (publishers) {
                    for (PublisherData p : publishers) {
                        if (!p.getConnectionMetadata().getUsername().equals(consumerData.getConnectionMetadata().getUsername())) {
                            for (Channel c : p.getCatalog().getChannels()) {
                                Message mi = new Message(c.getName());
                                super.sendMessage(mi, clientConnectionMetadata);
                            }
                        }
                    }
                }

                super.sendMessage(responseOK, clientConnectionMetadata);
            }

            if (m.getData().equals("ask tags")) { // 2
                System.out.println("Command received: " + m);

                synchronized (publishers) {
                    for (PublisherData p : publishers) {
                        if (!p.getConnectionMetadata().getUsername().equals(consumerData.getConnectionMetadata().getUsername())) {
                            for (String tag : p.getCatalog().getTagList()) {
                                Message mi = new Message(tag);
                                super.sendMessage(mi, clientConnectionMetadata);
                            }
                        }
                    }
                }

                super.sendMessage(responseOK, clientConnectionMetadata);
            }

            if (m.getData().equals("ask videos of a channel")) { // 3
                Message m2 = super.receiveMessage(clientConnectionMetadata);
                String channel = m2.getData();

                synchronized (publishers) {
                    for (PublisherData p : publishers) {
                        if (!p.getConnectionMetadata().getUsername().equals(consumerData.getConnectionMetadata().getUsername())) {
                            MultimediaList videoFiles = p.getCatalog().getChannelTable().get(channel);
                            if (videoFiles != null) {
                                for (VideoFile video : videoFiles) {
                                    Message mi = new Message(video.getVideoName());
                                    super.sendMessage(mi, clientConnectionMetadata);
                                }
                            }
                        }
                    }
                }
                super.sendMessage(responseOK, clientConnectionMetadata);
            }

            if (m.getData().equals("ask videos of a tag")) { // 4
                Message m2 = super.receiveMessage(clientConnectionMetadata);
                String channel = m2.getData();

                synchronized (publishers) {
                    for (PublisherData p : publishers) {
                        if (!p.getConnectionMetadata().getUsername().equals(consumerData.getConnectionMetadata().getUsername())) {
                            MultimediaList videoFiles = p.getCatalog().getTagTable().get(channel);
                            if (videoFiles != null) {
                                for (VideoFile video : videoFiles) {
                                    Message mi = new Message(video.getVideoName());
                                    super.sendMessage(mi, clientConnectionMetadata);
                                }
                            }
                        }
                    }
                }

                super.sendMessage(responseOK, clientConnectionMetadata);
            }

            if (m.getData().equals("ask video of a channel")) { // 5
                Message m2 = super.receiveMessage(clientConnectionMetadata);
                String channel = m2.getData();

                Message m3 = super.receiveMessage(clientConnectionMetadata);
                String videoName = m3.getData();

                boolean found = false;
                PublisherData owner = null;

                synchronized (publishers) {
                    for (PublisherData p : publishers) {
                        if (!p.getConnectionMetadata().getUsername().equals(consumerData.getConnectionMetadata().getUsername())) {
                            MultimediaList videoFiles = p.getCatalog().getChannelTable().get(channel);
                            if (videoFiles != null) {
                                for (VideoFile f : videoFiles) {
                                    if (f.getVideoName().equals(videoName)) {
                                        found = true;
                                        owner = p;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if (found) {
                    super.sendMessage(responseFound, clientConnectionMetadata);

                    String ip = owner.getConnectionMetadata().getIp();
                    int port = owner.getConnectionMetadata().getPort();

                    System.out.println("Video will be requested from: " + ip + ":" + port);

                    ConnectionMetadata publisherConnectionMetadata = super.connect(ip, port);

                    super.sendMessage(m, publisherConnectionMetadata);
                    super.sendMessage(m2, publisherConnectionMetadata);
                    super.sendMessage(m3, publisherConnectionMetadata);

                    Message prefix = super.receiveMessage(publisherConnectionMetadata);

                    super.sendMessage(prefix, clientConnectionMetadata);

                    System.out.println("Bytes to be received: " + prefix);

                    long chunk = 512;

                    long fileSize = Long.parseLong(prefix.toString());

                    long completeChunks = fileSize / chunk;
                    long remaining = fileSize % chunk;

                    File file = new File("temp_" + new Random().nextInt() + ".mp4");

                    System.out.println("File size is: " +  fileSize);

                    try (OutputStream outputStream = new FileOutputStream(file);) {
                        byte[] buffer = new byte[(int) chunk];

                        for (int i=0;i<completeChunks;i++) {
                            long bytes_read = 0;

                            while (bytes_read < chunk) {
                                long missing = chunk - bytes_read;
                                int k = publisherConnectionMetadata.getObjectInputStream().read(buffer, (int)bytes_read, (int) missing);
                                bytes_read = bytes_read + k;
                            }

                            outputStream.write(buffer); // broker disk
                            outputStream.flush();

                            clientConnectionMetadata.getObjectOutputStream().write(buffer); // client socket
                            clientConnectionMetadata.getObjectOutputStream().flush();
                        }

                        if (remaining >0) {
                            byte[] bufferrem = new byte[(int) remaining];

                            long bytes_read = 0;

                            while (bytes_read < remaining) {
                                long missing = remaining - bytes_read;
                                int k = publisherConnectionMetadata.getObjectInputStream().read(bufferrem, (int)bytes_read, (int) missing);
                                bytes_read = bytes_read + k;
                            }
                            outputStream.write(bufferrem);
                            outputStream.flush();

                            clientConnectionMetadata.getObjectOutputStream().write(bufferrem);
                            clientConnectionMetadata.getObjectOutputStream().flush();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.out.println("File saved to disk");

                    // binary data ...
                } else {
                    super.sendMessage(responseMissing, clientConnectionMetadata);
                }

            }

            if (m.getData().equals("ask video of a tag")) { // 6
                Message m2 = super.receiveMessage(clientConnectionMetadata);
                String tag = m2.getData();

                Message m3 = super.receiveMessage(clientConnectionMetadata);
                String videoName = m3.getData();

                boolean found = false;
                PublisherData owner = null;

                synchronized (publishers) {
                    for (PublisherData p : publishers) {
                        if (!p.getConnectionMetadata().getUsername().equals(consumerData.getConnectionMetadata().getUsername())) {
                            MultimediaList videoFiles = p.getCatalog().getTagTable().get(tag);
                            if (videoFiles != null) {
                                for (VideoFile f : videoFiles) {
                                    if (f.getVideoName().equals(videoName)) {
                                        found = true;
                                        owner = p;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if (found) {
                    super.sendMessage(responseFound, clientConnectionMetadata);

                    String ip = owner.getConnectionMetadata().getIp();
                    int port = owner.getConnectionMetadata().getPort();

                    System.out.println("Video will be requested from: " + ip + ":" + port);

                    ConnectionMetadata publisherConnectionMetadata = super.connect(ip, port);

                    super.sendMessage(m, publisherConnectionMetadata);
                    super.sendMessage(m2, publisherConnectionMetadata);
                    super.sendMessage(m3, publisherConnectionMetadata);

                    Message prefix = super.receiveMessage(publisherConnectionMetadata);

                    super.sendMessage(prefix, clientConnectionMetadata);

                    System.out.println("Bytes to be received: " + prefix);

                    long chunk = 512;

                    long fileSize = Long.parseLong(prefix.toString());

                    long completeChunks = fileSize / chunk;
                    long remaining = fileSize % chunk;

                    File file = new File("temp_" + new Random().nextInt() + ".mp4");

                    System.out.println("File size is: " +  fileSize);

                    try (OutputStream outputStream = new FileOutputStream(file);) {
                        byte[] buffer = new byte[(int) chunk];

                        for (int i=0;i<completeChunks;i++) {
                            long bytes_read = 0;

                            while (bytes_read < chunk) {
                                long missing = chunk - bytes_read;
                                int k = publisherConnectionMetadata.getObjectInputStream().read(buffer, (int)bytes_read, (int) missing);
                                bytes_read = bytes_read + k;
                            }

                            outputStream.write(buffer); // broker disk
                            outputStream.flush();

                            clientConnectionMetadata.getObjectOutputStream().write(buffer); // client socket
                            clientConnectionMetadata.getObjectOutputStream().flush();
                        }

                        if (remaining >0) {
                            byte[] bufferrem = new byte[(int) remaining];

                            long bytes_read = 0;

                            while (bytes_read < remaining) {
                                long missing = remaining - bytes_read;
                                int k = publisherConnectionMetadata.getObjectInputStream().read(bufferrem, (int)bytes_read, (int) missing);
                                bytes_read = bytes_read + k;
                            }
                            outputStream.write(bufferrem);
                            outputStream.flush();

                            clientConnectionMetadata.getObjectOutputStream().write(bufferrem);
                            clientConnectionMetadata.getObjectOutputStream().flush();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.out.println("File saved to disk");

                    // binary data ...
                } else {
                    super.sendMessage(responseMissing, clientConnectionMetadata);
                }
            }

            if (m.getData().equals("bye")) {
                break;
            }
        }

        System.out.println("A consumer has logged out");
    }

    public void notifyPublisher(String s) {

    }

    public void notifyBrokersOnChanges(String s) {

    }

    public void pull(String s ){

    }

    public void filterConsumers(Consumer c) {

    }




}

