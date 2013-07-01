package android.hardware.usb;
public class UsbDevice
  implements android.os.Parcelable
{
UsbDevice() { throw new RuntimeException("Stub!"); }
public  java.lang.String getDeviceName() { throw new RuntimeException("Stub!"); }
public  int getDeviceId() { throw new RuntimeException("Stub!"); }
public  int getVendorId() { throw new RuntimeException("Stub!"); }
public  int getProductId() { throw new RuntimeException("Stub!"); }
public  int getDeviceClass() { throw new RuntimeException("Stub!"); }
public  int getDeviceSubclass() { throw new RuntimeException("Stub!"); }
public  int getDeviceProtocol() { throw new RuntimeException("Stub!"); }
public  int getInterfaceCount() { throw new RuntimeException("Stub!"); }
public  android.hardware.usb.UsbInterface getInterface(int index) { throw new RuntimeException("Stub!"); }
public  boolean equals(java.lang.Object o) { throw new RuntimeException("Stub!"); }
public  int hashCode() { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel parcel, int flags) { throw new RuntimeException("Stub!"); }
public static  int getDeviceId(java.lang.String name) { throw new RuntimeException("Stub!"); }
public static  java.lang.String getDeviceName(int id) { throw new RuntimeException("Stub!"); }
public static final android.os.Parcelable.Creator<android.hardware.usb.UsbDevice> CREATOR;
static { CREATOR = null; }
}
