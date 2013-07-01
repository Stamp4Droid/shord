package android.net.nsd;
public final class NsdManager
{
public static interface DiscoveryListener
{
public abstract  void onStartDiscoveryFailed(java.lang.String serviceType, int errorCode);
public abstract  void onStopDiscoveryFailed(java.lang.String serviceType, int errorCode);
public abstract  void onDiscoveryStarted(java.lang.String serviceType);
public abstract  void onDiscoveryStopped(java.lang.String serviceType);
public abstract  void onServiceFound(android.net.nsd.NsdServiceInfo serviceInfo);
public abstract  void onServiceLost(android.net.nsd.NsdServiceInfo serviceInfo);
}
public static interface RegistrationListener
{
public abstract  void onRegistrationFailed(android.net.nsd.NsdServiceInfo serviceInfo, int errorCode);
public abstract  void onUnregistrationFailed(android.net.nsd.NsdServiceInfo serviceInfo, int errorCode);
public abstract  void onServiceRegistered(android.net.nsd.NsdServiceInfo serviceInfo);
public abstract  void onServiceUnregistered(android.net.nsd.NsdServiceInfo serviceInfo);
}
public static interface ResolveListener
{
public abstract  void onResolveFailed(android.net.nsd.NsdServiceInfo serviceInfo, int errorCode);
public abstract  void onServiceResolved(android.net.nsd.NsdServiceInfo serviceInfo);
}
NsdManager() { throw new RuntimeException("Stub!"); }
public  void registerService(android.net.nsd.NsdServiceInfo serviceInfo, int protocolType, android.net.nsd.NsdManager.RegistrationListener listener) { throw new RuntimeException("Stub!"); }
public  void unregisterService(android.net.nsd.NsdManager.RegistrationListener listener) { throw new RuntimeException("Stub!"); }
public  void discoverServices(java.lang.String serviceType, int protocolType, android.net.nsd.NsdManager.DiscoveryListener listener) { throw new RuntimeException("Stub!"); }
public  void stopServiceDiscovery(android.net.nsd.NsdManager.DiscoveryListener listener) { throw new RuntimeException("Stub!"); }
public  void resolveService(android.net.nsd.NsdServiceInfo serviceInfo, android.net.nsd.NsdManager.ResolveListener listener) { throw new RuntimeException("Stub!"); }
public static final java.lang.String ACTION_NSD_STATE_CHANGED = "android.net.nsd.STATE_CHANGED";
public static final java.lang.String EXTRA_NSD_STATE = "nsd_state";
public static final int NSD_STATE_DISABLED = 1;
public static final int NSD_STATE_ENABLED = 2;
public static final int PROTOCOL_DNS_SD = 1;
public static final int FAILURE_INTERNAL_ERROR = 0;
public static final int FAILURE_ALREADY_ACTIVE = 3;
public static final int FAILURE_MAX_LIMIT = 4;
}
