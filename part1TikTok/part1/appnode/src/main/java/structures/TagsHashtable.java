package structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class TagsHashtable extends TreeMap<String, MultimediaList> {
    public TagsHashtable() {
        super(String.CASE_INSENSITIVE_ORDER);
    }
}
