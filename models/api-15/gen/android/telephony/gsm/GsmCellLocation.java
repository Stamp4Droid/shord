package android.telephony.gsm;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class GsmCellLocation extends android.telephony.CellLocation {

    public GsmCellLocation() {
        throw new RuntimeException("Stub!");
    }

    public GsmCellLocation(android.os.Bundle bundle) {
        throw new RuntimeException("Stub!");
    }

    public void setStateInvalid() {
        throw new RuntimeException("Stub!");
    }

    public void setLacAndCid(int lac, int cid) {
        throw new RuntimeException("Stub!");
    }

    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    public boolean equals(java.lang.Object o) {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String toString() {
        throw new RuntimeException("Stub!");
    }

    public void fillInNotifierBundle(android.os.Bundle m) {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "$FINE_LOCATION", to = "@return") })
    public int getLac() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "$FINE_LOCATION", to = "@return") })
    public int getCid() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "$FINE_LOCATION", to = "@return") })
    public int getPsc() {
        return 0;
    }
}

