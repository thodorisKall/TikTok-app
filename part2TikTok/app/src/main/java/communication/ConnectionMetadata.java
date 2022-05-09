package communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectionMetadata {
    private Socket requestSocket = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;

    public ConnectionMetadata(Socket requestSocket, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        this.requestSocket = requestSocket;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
    }

    public boolean isConnected() {
        return requestSocket != null;
    }

    public Socket getRequestSocket() {
        return requestSocket;
    }

    public void setRequestSocket(Socket requestSocket) {
        this.requestSocket = requestSocket;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public void setObjectOutputStream(ObjectOutputStream objectOutputStream) {
        this.objectOutputStream = objectOutputStream;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public void setObjectInputStream(ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
    }

    public void disconnect() {
        if (requestSocket != null) {
            try {
                objectOutputStream.close();
                objectInputStream.close();
                requestSocket.close();

                objectOutputStream = null;
                objectInputStream = null;
                requestSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
