package structures;

import java.util.ArrayList;
import java.util.List;

public class Channel implements Comparable<Channel> {
    private String name;
    private String filepath;
    private List<String> associatedTags = new ArrayList<String>();

    public Channel(String name, String filepath) {
        this.name = name;
        this.filepath = filepath;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "name='" + name + '\'' +
                ", filepath='" + filepath + '\'' +
                ", associatedTags=" + associatedTags +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public List<String> getAssociatedTags() {
        return associatedTags;
    }

    public void setAssociatedTags(List<String> associatedTags) {
        this.associatedTags = associatedTags;
    }

    public void print() {
        System.out.println(this.toString());
    }

    @Override
    public int compareTo(Channel channel) {
        return name.compareTo(channel.name);
    }
}
