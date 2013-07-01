package android.net;
public class VpnService
  extends android.app.Service
{
public class Builder
{
public  Builder() { throw new RuntimeException("Stub!"); }
public  android.net.VpnService.Builder setSession(java.lang.String session) { throw new RuntimeException("Stub!"); }
public  android.net.VpnService.Builder setConfigureIntent(android.app.PendingIntent intent) { throw new RuntimeException("Stub!"); }
public  android.net.VpnService.Builder setMtu(int mtu) { throw new RuntimeException("Stub!"); }
public  android.net.VpnService.Builder addAddress(java.net.InetAddress address, int prefixLength) { throw new RuntimeException("Stub!"); }
public  android.net.VpnService.Builder addAddress(java.lang.String address, int prefixLength) { throw new RuntimeException("Stub!"); }
public  android.net.VpnService.Builder addRoute(java.net.InetAddress address, int prefixLength) { throw new RuntimeException("Stub!"); }
public  android.net.VpnService.Builder addRoute(java.lang.String address, int prefixLength) { throw new RuntimeException("Stub!"); }
public  android.net.VpnService.Builder addDnsServer(java.net.InetAddress address) { throw new RuntimeException("Stub!"); }
public  android.net.VpnService.Builder addDnsServer(java.lang.String address) { throw new RuntimeException("Stub!"); }
public  android.net.VpnService.Builder addSearchDomain(java.lang.String domain) { throw new RuntimeException("Stub!"); }
public  android.os.ParcelFileDescriptor establish() { throw new RuntimeException("Stub!"); }
}
public  VpnService() { throw new RuntimeException("Stub!"); }
public static  android.content.Intent prepare(android.content.Context context) { throw new RuntimeException("Stub!"); }
public  boolean protect(int socket) { throw new RuntimeException("Stub!"); }
public  boolean protect(java.net.Socket socket) { throw new RuntimeException("Stub!"); }
public  boolean protect(java.net.DatagramSocket socket) { throw new RuntimeException("Stub!"); }
public  android.os.IBinder onBind(android.content.Intent intent) { throw new RuntimeException("Stub!"); }
public  void onRevoke() { throw new RuntimeException("Stub!"); }
public static final java.lang.String SERVICE_INTERFACE = "android.net.VpnService";
}
