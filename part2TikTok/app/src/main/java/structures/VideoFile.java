package structures;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.media.MediaMetadataRetriever;

import org.xml.sax.SAXException;



public class VideoFile implements Comparable<VideoFile> {
    private String channelName;
    private String videoName;
    private String filepath;

    private long bytes ;
    private String type;

    private String dateCreated;
    private String framerate;
    private String frameWidth;
    private String frameHeight;
    private List<String> associatedTags = new ArrayList<String>();
    private String duration;


    public VideoFile(String videoName, String channelName, String filepath) throws FileNotFoundException, IOException, SAXException { // TikaException {
        this.channelName = channelName;
        this.videoName = videoName;
        this.filepath = filepath; // Mp4

        videoParser();
    }


    @Override
    public String toString() {

        return videoName;
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

    @Override
    public int compareTo(VideoFile videoFile) {
        return videoName.compareTo(videoFile.videoName);
    }

    public void videoParser() throws IOException, SAXException {
        File f = new File(filepath);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filepath);
        int t = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        duration = String.valueOf((t % 60000) / 1000);

        framerate= mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);
        try{
            Path fp = Paths.get(filepath);
            BasicFileAttributes bfa= Files.readAttributes(fp,BasicFileAttributes.class);
            dateCreated=bfa.creationTime().toString();
            dateCreated=dateCreated.substring(0,dateCreated.length()-10);

        }catch (IOException e){
            e.printStackTrace();
        }

        bytes = f.length();

    }
}