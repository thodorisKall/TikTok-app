package config;

import hashing.HashCalculator;

import java.math.BigInteger;

public class BrokerConfiguration {
    // Common for all brokers:
    public static String BROKER_IP_1 = "192.168.2.17";
    public static String BROKER_IP_2 = "192.168.2.17";
    public static String BROKER_IP_3 = "192.168.2.17";

    public static int BROKER_PORT_1 = 14321;
    public static int BROKER_PORT_2 = 14322;
    public static int BROKER_PORT_3 = 14323;

    public static BigInteger BROKER_HASH_1 = null;
    public static BigInteger BROKER_HASH_2 = null;
    public static BigInteger BROKER_HASH_3 = null;

    // Broker specific:
    public static final int WHO_AM_I = 1;
    public static final String MY_IP = BROKER_IP_1;
    public static final int MY_PORT = BROKER_PORT_1;

    // *******************************************************************
    public static final boolean CONNECT_ONLY_TO_ONE_BROKER = false;

    public static void print() {
        System.out.println("Broker 1 configuration: " + BrokerConfiguration.BROKER_IP_1 + ":" + BrokerConfiguration.BROKER_PORT_1 + " hash: " + BROKER_HASH_1);
        System.out.println("Broker 2 configuration: " + BrokerConfiguration.BROKER_IP_2 + ":" + BrokerConfiguration.BROKER_PORT_2 + " hash: " + BROKER_HASH_2);
        System.out.println("Broker 3 configuration: " + BrokerConfiguration.BROKER_IP_3 + ":" + BrokerConfiguration.BROKER_PORT_3 + " hash: " + BROKER_HASH_3);
    }

    public static void updateHashes() {
        BROKER_HASH_1 = HashCalculator.hash(BROKER_IP_1 + ":" + BROKER_PORT_1);
        BROKER_HASH_2 = HashCalculator.hash(BROKER_IP_2 + ":" + BROKER_PORT_2);
        BROKER_HASH_3 = HashCalculator.hash(BROKER_IP_3 + ":" + BROKER_PORT_3);
    }

    public static void sortHashes() {
        BigInteger [] brokerHashArray = new BigInteger[3];

        brokerHashArray[0] = BrokerConfiguration.BROKER_HASH_1;
        brokerHashArray[1] = BrokerConfiguration.BROKER_HASH_2;
        brokerHashArray[2] = BrokerConfiguration.BROKER_HASH_3;


        String [] tempIPS = new String [] { BROKER_IP_1, BROKER_IP_2, BROKER_IP_3};
        int [] tempPorts = new int[] { BROKER_PORT_1, BROKER_PORT_2, BROKER_PORT_3 };


        int n = brokerHashArray.length;
        for (int i=0; i < n; i++){
            for(int j=1; j < (n-i); j++){
                if (brokerHashArray[j-1].compareTo(brokerHashArray[j]) > 0){
                    BigInteger temp = brokerHashArray[j-1];
                    brokerHashArray[j-1] = brokerHashArray[j];
                    brokerHashArray[j] = temp;

                    String tempIP = tempIPS[j-1];
                    tempIPS[j-1] = tempIPS[j];
                    tempIPS[j] = tempIP;

                    int tempPort = tempPorts[j-1];
                    tempPorts[j-1] = tempPorts[j];
                    tempPorts[j] = tempPort;
                }
            }
        }

        BROKER_HASH_1 = brokerHashArray[0];
        BROKER_HASH_2 = brokerHashArray[1];
        BROKER_HASH_3 = brokerHashArray[2];

        BROKER_IP_1 = tempIPS[0];
        BROKER_IP_2 = tempIPS[1];
        BROKER_IP_3 = tempIPS[2];

        BROKER_PORT_1 = tempPorts[0];
        BROKER_PORT_2 = tempPorts[1];
        BROKER_PORT_3 = tempPorts[2];
    }

}
