package nodes;

import communication.ConnectionMetadata;
import communication.Message;
import hashing.HashCalculator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private List<Broker> brokers = new ArrayList<>();


    public void init() {

    }

    public List<Broker> getBrokers() {
        return brokers;
    }

    public boolean isConnected( ConnectionMetadata connectionMetadata) {
        return connectionMetadata.isConnected();
    }

    public ConnectionMetadata connect(String IP, int port) throws IOException {
        System.out.println("Trying to connect ...");
        Socket requestSocket = new Socket(InetAddress.getByName(IP), port);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(requestSocket.getInputStream());

        ConnectionMetadata metadata = new ConnectionMetadata(requestSocket, objectOutputStream, objectInputStream);
        return metadata;
    }

    public void disconnect(ConnectionMetadata connectionMetadata) {
        try {
            if (isConnected(connectionMetadata)) {
                connectionMetadata.getObjectInputStream().close();
                connectionMetadata.getObjectInputStream().close();
                connectionMetadata.getRequestSocket().close();
                connectionMetadata.setRequestSocket(null);
                System.out.println("Info: Disconnect successful");
            } else {
                System.out.println("Info: Disconnect failed (already disconnected)");
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void sendMessage(Message m,  ConnectionMetadata connectionMetadata) throws IOException {
        if (isConnected(connectionMetadata)) {
            connectionMetadata.getObjectOutputStream().writeObject(m);
            connectionMetadata.getObjectOutputStream().flush();
        }
    }

    public Message receiveMessage(ConnectionMetadata connectionMetadata) throws IOException, ClassNotFoundException {
        if (isConnected(connectionMetadata)) {
            Message m = (Message) connectionMetadata.getObjectInputStream().readObject();
            return m;
        } else {
            return null;
        }
    }

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



    public BigInteger hashTopic(String topic) { //video
        return HashCalculator.hash(topic);
    }


    public boolean equal(BigInteger x, BigInteger y) {
        if (x.equals(y)) {
            return true;
        } else {
            return false;
        }
    }

}
