package hashing;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

}
