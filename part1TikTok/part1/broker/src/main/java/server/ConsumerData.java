package server;

import communication.ConnectionMetadata;

public class ConsumerData {
    ConnectionMetadata connectionMetadata;

    public ConsumerData(ConnectionMetadata connectionMetadata) {
        this.connectionMetadata = connectionMetadata;
    }

    public ConnectionMetadata getConnectionMetadata() {
        return connectionMetadata;
    }

    public void setConnectionMetadata(ConnectionMetadata connectionMetadata) {
        this.connectionMetadata = connectionMetadata;
    }
}
