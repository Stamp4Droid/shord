package android.telephony.cdma;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class CdmaCellLocation extends android.telephony.CellLocation {

    public CdmaCellLocation() {
        throw new RuntimeException("Stub!");
    }

    public CdmaCellLocation(android.os.Bundle bundle) {
        throw new RuntimeException("Stub!");
    }

    public void setStateInvalid() {
        throw new RuntimeException("Stub!");
    }

    public void setCellLocationData(int baseStationId, int baseStationLatitude, int baseStationLongitude) {
        throw new RuntimeException("Stub!");
    }

    public void setCellLocationData(int baseStationId, int baseStationLatitude, int baseStationLongitude, int systemId, int networkId) {
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

    public void fillInNotifierBundle(android.os.Bundle bundleToFill) {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "$CDMA_LOCATION", to = "@return") })
    public int getBaseStationId() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "$CDMA_LOCATION", to = "@return") })
    public int getBaseStationLatitude() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "$CDMA_LOCATION", to = "@return") })
    public int getBaseStationLongitude() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "$CDMA_SYSTEM_ID", to = "@return") })
    public int getSystemId() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "$CDMA_NETWORK_ID", to = "@return") })
    public int getNetworkId() {
        return 0;
    }
}

