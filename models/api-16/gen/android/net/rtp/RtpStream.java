package android.net.rtp;
public class RtpStream
{
RtpStream() { throw new RuntimeException("Stub!"); }
public  java.net.InetAddress getLocalAddress() { throw new RuntimeException("Stub!"); }
public  int getLocalPort() { throw new RuntimeException("Stub!"); }
public  java.net.InetAddress getRemoteAddress() { throw new RuntimeException("Stub!"); }
public  int getRemotePort() { throw new RuntimeException("Stub!"); }
public  boolean isBusy() { throw new RuntimeException("Stub!"); }
public  int getMode() { throw new RuntimeException("Stub!"); }
public  void setMode(int mode) { throw new RuntimeException("Stub!"); }
public  void associate(java.net.InetAddress address, int port) { throw new RuntimeException("Stub!"); }
public  void release() { throw new RuntimeException("Stub!"); }
protected  void finalize() throws java.lang.Throwable { throw new RuntimeException("Stub!"); }
public static final int MODE_NORMAL = 0;
public static final int MODE_SEND_ONLY = 1;
public static final int MODE_RECEIVE_ONLY = 2;
}
