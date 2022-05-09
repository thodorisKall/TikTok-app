package hashing;

import config.BrokerConfiguration;
import nodes.Broker;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

public class HashCalculator {
    public static BigInteger hash(String key) { //video
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(key.getBytes());
            byte[] digest = md.digest();
            return new BigInteger(1, digest);
        }catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean lessThan(BigInteger x, BigInteger y) {
        return x.compareTo(y) <= 0;
    }

    public static int decideWhereToSend(BigInteger x) {
        if (BrokerConfiguration.CONNECT_ONLY_TO_ONE_BROKER == true) {
            return 1;
        }

        if (lessThan(x, BrokerConfiguration.BROKER_HASH_1)) {
            return 1;
        }

        if (lessThan(x, BrokerConfiguration.BROKER_HASH_2)) {
            return 2;
        }

        if (lessThan(x, BrokerConfiguration.BROKER_HASH_3)) {
            return 3;
        }

        BigInteger y = x.mod(new BigInteger("3"));

        int z = y.intValue();

        if (z == 0) {
            return 1;
        }

        if (z == 1) {
            return 2;
        }

        if (z == 2) {
            return 3;
        }

        return 1;
    }
}
