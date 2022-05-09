package structures;

import java.util.ArrayList;

public class MultimediaList extends ArrayList<VideoFile> {
    public VideoFile findByTitle(String title) {
        for (VideoFile m : this) {
            if (m.getVideoName().equals(title)) {
                return m;
            }
        }
        return null;
    }
}
