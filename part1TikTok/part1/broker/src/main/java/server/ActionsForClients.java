package main.java.server;

import java.io.*;
import java.net.*;

public class ActionsForClients extends Thread {

    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;

    public ActionsForClients(Socket socket) {
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            int a = objectInputStream.readInt();
            int b = objectInputStream.readInt();

            objectOutputStream.writeInt(a + b);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                objectInputStream.close();
                objectOutputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
