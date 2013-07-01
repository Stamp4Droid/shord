package android.mtp;
public final class MtpDevice
{
public  MtpDevice(android.hardware.usb.UsbDevice device) { throw new RuntimeException("Stub!"); }
public  boolean open(android.hardware.usb.UsbDeviceConnection connection) { throw new RuntimeException("Stub!"); }
public  void close() { throw new RuntimeException("Stub!"); }
protected  void finalize() throws java.lang.Throwable { throw new RuntimeException("Stub!"); }
public  java.lang.String getDeviceName() { throw new RuntimeException("Stub!"); }
public  int getDeviceId() { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public  android.mtp.MtpDeviceInfo getDeviceInfo() { throw new RuntimeException("Stub!"); }
public  int[] getStorageIds() { throw new RuntimeException("Stub!"); }
public  int[] getObjectHandles(int storageId, int format, int objectHandle) { throw new RuntimeException("Stub!"); }
public  byte[] getObject(int objectHandle, int objectSize) { throw new RuntimeException("Stub!"); }
public  byte[] getThumbnail(int objectHandle) { throw new RuntimeException("Stub!"); }
public  android.mtp.MtpStorageInfo getStorageInfo(int storageId) { throw new RuntimeException("Stub!"); }
public  android.mtp.MtpObjectInfo getObjectInfo(int objectHandle) { throw new RuntimeException("Stub!"); }
public  boolean deleteObject(int objectHandle) { throw new RuntimeException("Stub!"); }
public  long getParent(int objectHandle) { throw new RuntimeException("Stub!"); }
public  long getStorageId(int objectHandle) { throw new RuntimeException("Stub!"); }
public  boolean importFile(int objectHandle, java.lang.String destPath) { throw new RuntimeException("Stub!"); }
}
