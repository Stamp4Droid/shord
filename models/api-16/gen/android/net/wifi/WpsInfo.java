package android.net.wifi;
public class WpsInfo
  implements android.os.Parcelable
{
public  WpsInfo() { throw new RuntimeException("Stub!"); }
public  WpsInfo(android.net.wifi.WpsInfo source) { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public static final int PBC = 0;
public static final int DISPLAY = 1;
public static final int KEYPAD = 2;
public static final int LABEL = 3;
public static final int INVALID = 4;
public int setup;
public java.lang.String pin;
public static final android.os.Parcelable.Creator<android.net.wifi.WpsInfo> CREATOR;
static { CREATOR = null; }
}
