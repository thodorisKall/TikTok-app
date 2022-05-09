import config.BrokerConfiguration;
import server.Server;

public class Main {
    public static void main(String [] args) {
        System.out.println("Update hashes ...");
        BrokerConfiguration.updateHashes();
        BrokerConfiguration.print();

        System.out.println("Sort hashes ...");
        BrokerConfiguration.sortHashes();
        BrokerConfiguration.print();

        System.out.println("-----------------------------------------------------------");
        System.out.println("I am   : Broker #" + BrokerConfiguration.WHO_AM_I);
        System.out.println("My IP  : " + BrokerConfiguration.MY_IP);
        System.out.println("My PORT: " + BrokerConfiguration.MY_PORT);

        Server server = new Server();

        server.openServer(BrokerConfiguration.MY_IP, BrokerConfiguration.MY_PORT);

    }
}
