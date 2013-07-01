package android.hardware.usb;
public class UsbDeviceConnection
{
UsbDeviceConnection() { throw new RuntimeException("Stub!"); }
public  void close() { throw new RuntimeException("Stub!"); }
public  int getFileDescriptor() { throw new RuntimeException("Stub!"); }
public  byte[] getRawDescriptors() { throw new RuntimeException("Stub!"); }
public  boolean claimInterface(android.hardware.usb.UsbInterface intf, boolean force) { throw new RuntimeException("Stub!"); }
public  boolean releaseInterface(android.hardware.usb.UsbInterface intf) { throw new RuntimeException("Stub!"); }
public  int controlTransfer(int requestType, int request, int value, int index, byte[] buffer, int length, int timeout) { throw new RuntimeException("Stub!"); }
public  int bulkTransfer(android.hardware.usb.UsbEndpoint endpoint, byte[] buffer, int length, int timeout) { throw new RuntimeException("Stub!"); }
public  android.hardware.usb.UsbRequest requestWait() { throw new RuntimeException("Stub!"); }
public  java.lang.String getSerial() { throw new RuntimeException("Stub!"); }
}
