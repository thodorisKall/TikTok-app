package nodes;

import communication.Message;

import java.io.IOException;

public class Publisher extends Node {
    // Step 1: connect to master broker (register)
    public void register(String IP, int port) throws IOException, ClassNotFoundException {
//            super.connect(IP, port);
//
//            Message m = new Message("REGISTER AS PUBLISHER");
//
//            super.sendMessage(m);
//
//            Message r= super.receiveMessage();
//
//            System.out.println("Response receveid:" + r);
    }



    // Step 2: retrieve broker list

    // Step 3: for each channel, send the metadata (?) to the proper broker

    // Step 4: whenever needed update tags

    // Step 5: send video to incoming connections

    public String addHashTagToChannel(String s) {
        return null;
    }

    public String addHashTagToVideo(String s) {
        return null;
    }

    public String removeHashTagFromChannel(String s) {
        return null;
    }

    public String removeHashTagFromVideo(String s) {
        return null;
    }

    public void getBrokersFromNetwork() {
    }

    public void push(String channelName , String Value) {
    }

    public void notifyFailure(Broker b) {
    }

    public void notifyBrokersForHashTags(String s) {
    }


}
