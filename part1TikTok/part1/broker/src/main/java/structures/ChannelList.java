package structures;

import java.util.ArrayList;

public class ChannelList extends ArrayList<Channel> {
    public Channel findByTitle(String title) {
        for (Channel m : this) {
            if (m.getName().equals(title)) {
                return m;
            }
        }
        return null;
    }
}
