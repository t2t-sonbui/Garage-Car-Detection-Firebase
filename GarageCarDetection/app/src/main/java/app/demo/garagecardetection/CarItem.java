package app.demo.garagecardetection;

import com.google.firebase.database.Exclude;

/**
 * Created by Son Bui on 24/06/2016.
 */
public class CarItem {
    private boolean in_range;
    private int distance;
    private long timestamp;
    private String name;
    @Exclude//igore name
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isIn_range() {
        return in_range;
    }

    public void setIn_range(boolean in_range) {
        this.in_range = in_range;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
