package structures;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class ChannelHashtable extends TreeMap<String, MultimediaList> {
    public ChannelHashtable() {
        super(String.CASE_INSENSITIVE_ORDER);
    }
}
