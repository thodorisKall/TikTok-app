package nodes;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Broker extends Node {
    public List<Consumer> consumers = new ArrayList<>();
    public List<Consumer> publishers = new ArrayList<>();


    // Step 1 - accept connection from either publisher or consumer

    // Step 2- send broker list if first communication

    // Step 2a - if connection from publisher, receive metadata and store locally

    // Step 2b - if connection from consumer, find proper publisher, request file and forward it to consumer

    public void calculateKeys() {

    }

    public void acceptConnection(Publisher p ) {

    }

    public void acceptConnection(Consumer c) {

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

