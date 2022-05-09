package structures;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.entity.ContentType;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TIFF;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import static org.apache.tika.metadata.Office.CREATION_DATE;

public class VideoFile {
    private String channelName;
    private String videoName;
    private String filepath;

    private long bytes = 0;
    //private String length = String.valueOf(0); // seconds
    private String type;

    private String dateCreated;
    private String framerate;
    private String frameWidth;
    private String frameHeight;
    private List<String> associatedTags = new ArrayList<String>();
    private String duration;


    public VideoFile(String videoName, String channelName, String filepath) throws FileNotFoundException, IOException, TikaException, SAXException {
        this.channelName = channelName;
        this.videoName = videoName;
        this.filepath = filepath; // Mp4

        // TODO: bytes, length, dateCreated, framerate, framewidth, frameheight => find from mp4

        videoParser(videoName);
//        bytes = 0;
//        framerate = "10";
//        frameWidth = String.valueOf(20);
//        frameHeight = String.valueOf(30);


//        length = Math.round((100.0*movieBox.getMovieHeaderBox().getDuration()/(double)movieBox.getMovieHeaderBox().getTimescale())/100); // frames / (frames/sec = Hz)
//        framerate = movieBox.getMovieHeaderBox().getRate()
//        bytes = 1234;
//        duration = String.valueOf(4567);
//        framerate = String.valueOf(890);

    }

    private void printDetails(String s, int bitRate) {
//        System.out.println(String.format("%s : %s", item, details));
    }

    @Override
    public String toString() {
        return "VideoFile{" +
                "channelName='" + channelName + '\'' +
                ", videoName='" + videoName + '\'' +
                ", filepath='" + filepath + '\'' +
                ", bytes=" + bytes +
                ", duration=" + duration +
                ", dateCreated=" + dateCreated +
                ", type=" + type +
                ", framerate=" + framerate +
                ", frameWidth=" + frameWidth +
                ", frameHeight=" + frameHeight +
                ", associatedTags=" + associatedTags +
                '}';
    }

    public void print() {
        System.out.println(this.toString());
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String length) {
        this.duration = length;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getFramerate() {
        return framerate;
    }

    public void setFramerate(String framerate) {
        this.framerate = framerate;
    }

    public String getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(String frameWidth) {
        this.frameWidth = frameWidth;
    }

    public String getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(String frameHeight) {
        this.frameHeight = frameHeight;
    }

    public List<String> getAssociatedTags() {
        return associatedTags;
    }

    public void setAssociatedTags(List<String> associatedTags) {
        this.associatedTags = associatedTags;
    }

    public String getType() {
        return type;
    }

    public void setType(String type){
        this.type=type;
    }

    public void videoParser(String videoName) throws IOException, TikaException, SAXException {
        File f = new File(filepath);
        FileInputStream inputStream = new FileInputStream(f);
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext parseContext = new ParseContext();
        MP4Parser mp4Parser = new MP4Parser();
        mp4Parser.parse(inputStream, handler, metadata, parseContext);

        String[] metadataNames = metadata.names();
        bytes = f.length();
        frameWidth = metadata.get(TIFF.IMAGE_WIDTH);
        dateCreated = metadata.get(CREATION_DATE);
        duration=metadata.get(XMPDM.DURATION);
        type = metadata.get("Content-Type");
        System.out.println("FrameWidth: "+ frameWidth + " Date Created: " + dateCreated + " Duration: " + duration + " Type: " + type);
        for (String name : metadataNames) {
            System.out.println(name + ": " + metadata.get(name) + "Type: " + name.getClass());
        }
    }
}