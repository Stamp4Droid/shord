package android.location;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class LocationManager {

    LocationManager() {
        throw new RuntimeException("Stub!");
    }

    public java.util.List<java.lang.String> getAllProviders() {
        throw new RuntimeException("Stub!");
    }

    public java.util.List<java.lang.String> getProviders(boolean enabledOnly) {
        throw new RuntimeException("Stub!");
    }

    public android.location.LocationProvider getProvider(java.lang.String name) {
        throw new RuntimeException("Stub!");
    }

    public java.util.List<java.lang.String> getProviders(android.location.Criteria criteria, boolean enabledOnly) {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getBestProvider(android.location.Criteria criteria, boolean enabledOnly) {
        throw new RuntimeException("Stub!");
    }

    public void removeUpdates(android.location.LocationListener listener) {
        throw new RuntimeException("Stub!");
    }

    public void removeUpdates(android.app.PendingIntent intent) {
        throw new RuntimeException("Stub!");
    }

    public void removeProximityAlert(android.app.PendingIntent intent) {
        throw new RuntimeException("Stub!");
    }

    public boolean isProviderEnabled(java.lang.String provider) {
        throw new RuntimeException("Stub!");
    }

    public void addTestProvider(java.lang.String name, boolean requiresNetwork, boolean requiresSatellite, boolean requiresCell, boolean hasMonetaryCost, boolean supportsAltitude, boolean supportsSpeed, boolean supportsBearing, int powerRequirement, int accuracy) {
        throw new RuntimeException("Stub!");
    }

    public void removeTestProvider(java.lang.String provider) {
        throw new RuntimeException("Stub!");
    }

    public void setTestProviderLocation(java.lang.String provider, android.location.Location loc) {
        throw new RuntimeException("Stub!");
    }

    public void clearTestProviderLocation(java.lang.String provider) {
        throw new RuntimeException("Stub!");
    }

    public void setTestProviderEnabled(java.lang.String provider, boolean enabled) {
        throw new RuntimeException("Stub!");
    }

    public void clearTestProviderEnabled(java.lang.String provider) {
        throw new RuntimeException("Stub!");
    }

    public void setTestProviderStatus(java.lang.String provider, int status, android.os.Bundle extras, long updateTime) {
        throw new RuntimeException("Stub!");
    }

    public void clearTestProviderStatus(java.lang.String provider) {
        throw new RuntimeException("Stub!");
    }

    public boolean addGpsStatusListener(android.location.GpsStatus.Listener listener) {
        throw new RuntimeException("Stub!");
    }

    public void removeGpsStatusListener(android.location.GpsStatus.Listener listener) {
        throw new RuntimeException("Stub!");
    }

    public void removeNmeaListener(android.location.GpsStatus.NmeaListener listener) {
        throw new RuntimeException("Stub!");
    }

    public android.location.GpsStatus getGpsStatus(android.location.GpsStatus status) {
        throw new RuntimeException("Stub!");
    }

    public boolean sendExtraCommand(java.lang.String provider, java.lang.String command, android.os.Bundle extras) {
        throw new RuntimeException("Stub!");
    }

    public static final java.lang.String NETWORK_PROVIDER = "network";

    public static final java.lang.String GPS_PROVIDER = "gps";

    public static final java.lang.String PASSIVE_PROVIDER = "passive";

    public static final java.lang.String KEY_PROXIMITY_ENTERING = "entering";

    public static final java.lang.String KEY_STATUS_CHANGED = "status";

    public static final java.lang.String KEY_PROVIDER_ENABLED = "providerEnabled";

    public static final java.lang.String KEY_LOCATION_CHANGED = "location";

    public static final java.lang.String PROVIDERS_CHANGED_ACTION = "android.location.PROVIDERS_CHANGED";

    @STAMP(flows = { @Flow(from = "$LOCATION", to = "@return") })
    private Location getLocation() {
        return new Location((String) null);
    }

    private void registerListener(final android.location.LocationListener listener) {
        edu.stanford.stamp.harness.ApplicationDriver.getInstance().registerCallback(new edu.stanford.stamp.harness.Callback() {

            public void run() {
                listener.onLocationChanged(getLocation());
            }
        });
    }

    @STAMP(flows = { @Flow(from = "$FINE_LOCATION", to = "!INTENT") })
    public void requestLocationUpdates(java.lang.String provider, long minTime, float minDistance, android.app.PendingIntent intent) {
    }

    public void requestLocationUpdates(java.lang.String provider, long minTime, float minDistance, android.location.LocationListener listener) {
        registerListener(listener);
    }

    public void requestLocationUpdates(java.lang.String provider, long minTime, float minDistance, android.location.LocationListener listener, android.os.Looper looper) {
        registerListener(listener);
    }

    public void requestLocationUpdates(long minTime, float minDistance, android.location.Criteria criteria, android.location.LocationListener listener, android.os.Looper looper) {
        registerListener(listener);
    }

    @STAMP(flows = { @Flow(from = "$FINE_LOCATION", to = "!INTENT") })
    public void requestLocationUpdates(long minTime, float minDistance, android.location.Criteria criteria, android.app.PendingIntent intent) {
    }

    public void requestSingleUpdate(java.lang.String provider, android.location.LocationListener listener, android.os.Looper looper) {
        registerListener(listener);
    }

    public void requestSingleUpdate(android.location.Criteria criteria, android.location.LocationListener listener, android.os.Looper looper) {
        registerListener(listener);
    }

    @STAMP(flows = { @Flow(from = "$FINE_LOCATION", to = "!INTENT") })
    public void requestSingleUpdate(java.lang.String provider, android.app.PendingIntent intent) {
    }

    @STAMP(flows = { @Flow(from = "$FINE_LOCATION", to = "!INTENT") })
    public void requestSingleUpdate(android.location.Criteria criteria, android.app.PendingIntent intent) {
    }

    public android.location.Location getLastKnownLocation(java.lang.String provider) {
        return getLocation();
    }

    private static LocationManager locationManager = new LocationManager();

    public static LocationManager getInstance() {
        return locationManager;
    }

    @STAMP(flows = { @Flow(from = "$FINE_LOCATION", to = "!INTENT") })
    public void addProximityAlert(double latitude, double longitude, float radius, long expiration, android.app.PendingIntent intent) {
    }

    @STAMP(flows = { @Flow(from = "$FINE_LOCATION.nmea", to = "@return") })
    private String getNmea() {
        return new String();
    }

    public boolean addNmeaListener(final android.location.GpsStatus.NmeaListener listener) {
        edu.stanford.stamp.harness.ApplicationDriver.getInstance().registerCallback(new edu.stanford.stamp.harness.Callback() {

            public void run() {
                listener.onNmeaReceived(0, getNmea());
            }
        });
        return true;
    }
}

