package android.net.wifi.p2p;
public class WifiP2pDevice
  implements android.os.Parcelable
{
public  WifiP2pDevice() { throw new RuntimeException("Stub!"); }
public  WifiP2pDevice(android.net.wifi.p2p.WifiP2pDevice source) { throw new RuntimeException("Stub!"); }
public  boolean wpsPbcSupported() { throw new RuntimeException("Stub!"); }
public  boolean wpsKeypadSupported() { throw new RuntimeException("Stub!"); }
public  boolean wpsDisplaySupported() { throw new RuntimeException("Stub!"); }
public  boolean isServiceDiscoveryCapable() { throw new RuntimeException("Stub!"); }
public  boolean isGroupOwner() { throw new RuntimeException("Stub!"); }
public  boolean equals(java.lang.Object obj) { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public java.lang.String deviceName;
public java.lang.String deviceAddress;
public java.lang.String primaryDeviceType;
public java.lang.String secondaryDeviceType;
public static final int CONNECTED = 0;
public static final int INVITED = 1;
public static final int FAILED = 2;
public static final int AVAILABLE = 3;
public static final int UNAVAILABLE = 4;
public int status;
public static final android.os.Parcelable.Creator<android.net.wifi.p2p.WifiP2pDevice> CREATOR;
static { CREATOR = null; }
}
