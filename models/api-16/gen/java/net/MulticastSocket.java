package java.net;
public class MulticastSocket
  extends java.net.DatagramSocket
{
public  MulticastSocket() throws java.io.IOException { super((java.net.DatagramSocketImpl)null); throw new RuntimeException("Stub!"); }
public  MulticastSocket(int port) throws java.io.IOException { super((java.net.DatagramSocketImpl)null); throw new RuntimeException("Stub!"); }
public  MulticastSocket(java.net.SocketAddress localAddress) throws java.io.IOException { super((java.net.DatagramSocketImpl)null); throw new RuntimeException("Stub!"); }
public  java.net.InetAddress getInterface() throws java.net.SocketException { throw new RuntimeException("Stub!"); }
public  java.net.NetworkInterface getNetworkInterface() throws java.net.SocketException { throw new RuntimeException("Stub!"); }
public  int getTimeToLive() throws java.io.IOException { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  byte getTTL() throws java.io.IOException { throw new RuntimeException("Stub!"); }
public  void joinGroup(java.net.InetAddress groupAddr) throws java.io.IOException { throw new RuntimeException("Stub!"); }
public  void joinGroup(java.net.SocketAddress groupAddress, java.net.NetworkInterface netInterface) throws java.io.IOException { throw new RuntimeException("Stub!"); }
public  void leaveGroup(java.net.InetAddress groupAddr) throws java.io.IOException { throw new RuntimeException("Stub!"); }
public  void leaveGroup(java.net.SocketAddress groupAddress, java.net.NetworkInterface netInterface) throws java.io.IOException { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  void send(java.net.DatagramPacket packet, byte ttl) throws java.io.IOException { throw new RuntimeException("Stub!"); }
public  void setInterface(java.net.InetAddress address) throws java.net.SocketException { throw new RuntimeException("Stub!"); }
public  void setNetworkInterface(java.net.NetworkInterface networkInterface) throws java.net.SocketException { throw new RuntimeException("Stub!"); }
public  void setTimeToLive(int ttl) throws java.io.IOException { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  void setTTL(byte ttl) throws java.io.IOException { throw new RuntimeException("Stub!"); }
public  boolean getLoopbackMode() throws java.net.SocketException { throw new RuntimeException("Stub!"); }
public  void setLoopbackMode(boolean disable) throws java.net.SocketException { throw new RuntimeException("Stub!"); }
}
