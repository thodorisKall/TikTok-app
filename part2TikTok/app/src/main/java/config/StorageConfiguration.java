package config;

import android.os.Environment;

import java.io.File;

public class StorageConfiguration {
    public static final String PARENT_DIRECTORY_NAME = "TIKTOK";

    public static File currentDirectory = new File(Environment.getExternalStorageDirectory(), StorageConfiguration.PARENT_DIRECTORY_NAME);
    public static String profileDirectory = "user2";
    public static String resourceDirectory = currentDirectory.getAbsolutePath() + File.separator + profileDirectory;
    public static String downloadsDirectory = currentDirectory.getAbsolutePath() + File.separator + profileDirectory + File.separator + "downloads";
    public static String tagsDirectory = currentDirectory.getAbsolutePath() + File.separator + profileDirectory + File.separator + "tags";


    public static void setupDirectories(String username) {
        currentDirectory = new File(Environment.getExternalStorageDirectory(), StorageConfiguration.PARENT_DIRECTORY_NAME);
        profileDirectory = username;
        resourceDirectory = currentDirectory.getAbsolutePath() + File.separator + profileDirectory;
        downloadsDirectory = currentDirectory.getAbsolutePath() + File.separator + profileDirectory + File.separator + "downloads";
        tagsDirectory = currentDirectory.getAbsolutePath() + File.separator + profileDirectory + File.separator + "tags";
    }

    public static String getNewVideoPath(String channel, String videoName) {
        String s = resourceDirectory + File.separator + channel +  File.separator + videoName;
        return s;
    }
}
