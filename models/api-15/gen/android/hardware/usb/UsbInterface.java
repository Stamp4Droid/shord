package android.hardware.usb;
public class UsbInterface
  implements android.os.Parcelable
{
UsbInterface() { throw new RuntimeException("Stub!"); }
public  int getId() { throw new RuntimeException("Stub!"); }
public  int getInterfaceClass() { throw new RuntimeException("Stub!"); }
public  int getInterfaceSubclass() { throw new RuntimeException("Stub!"); }
public  int getInterfaceProtocol() { throw new RuntimeException("Stub!"); }
public  int getEndpointCount() { throw new RuntimeException("Stub!"); }
public  android.hardware.usb.UsbEndpoint getEndpoint(int index) { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel parcel, int flags) { throw new RuntimeException("Stub!"); }
public static final android.os.Parcelable.Creator<android.hardware.usb.UsbInterface> CREATOR;
static { CREATOR = null; }
}
