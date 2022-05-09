package server;

import communication.ConnectionMetadata;
import structures.*;

public class PublisherData {
    ConnectionMetadata connectionMetadata;
    Catalog catalog = new Catalog();

    public PublisherData(ConnectionMetadata connectionMetadata) {
        this.connectionMetadata = connectionMetadata;
    }

    public ConnectionMetadata getConnectionMetadata() {
        return connectionMetadata;
    }

    public void setConnectionMetadata(ConnectionMetadata connectionMetadata) {
        this.connectionMetadata = connectionMetadata;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }
}
