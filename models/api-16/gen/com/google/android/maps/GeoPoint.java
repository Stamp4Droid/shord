package com.google.android.maps;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class GeoPoint {

    public boolean equals(java.lang.Object param1) {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "param1", to = "this"), @Flow(from = "param2", to = "this") })
    public GeoPoint(int param1, int param2) {
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int getLatitudeE6() {
        return 13000000;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int getLongitudeE6() {
        return 13000000;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String toString() {
        return "";
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int hashCode() {
        return 424242;
    }
}

