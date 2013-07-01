package android.net;
public class TrafficStats
{
public  TrafficStats() { throw new RuntimeException("Stub!"); }
public static  void setThreadStatsTag(int tag) { throw new RuntimeException("Stub!"); }
public static  int getThreadStatsTag() { throw new RuntimeException("Stub!"); }
public static  void clearThreadStatsTag() { throw new RuntimeException("Stub!"); }
public static  void tagSocket(java.net.Socket socket) throws java.net.SocketException { throw new RuntimeException("Stub!"); }
public static  void untagSocket(java.net.Socket socket) throws java.net.SocketException { throw new RuntimeException("Stub!"); }
public static  void incrementOperationCount(int operationCount) { throw new RuntimeException("Stub!"); }
public static  void incrementOperationCount(int tag, int operationCount) { throw new RuntimeException("Stub!"); }
public static  long getMobileTxPackets() { throw new RuntimeException("Stub!"); }
public static  long getMobileRxPackets() { throw new RuntimeException("Stub!"); }
public static  long getMobileTxBytes() { throw new RuntimeException("Stub!"); }
public static  long getMobileRxBytes() { throw new RuntimeException("Stub!"); }
public static  long getTotalTxPackets() { throw new RuntimeException("Stub!"); }
public static  long getTotalRxPackets() { throw new RuntimeException("Stub!"); }
public static  long getTotalTxBytes() { throw new RuntimeException("Stub!"); }
public static  long getTotalRxBytes() { throw new RuntimeException("Stub!"); }
public static native  long getUidTxBytes(int uid);
public static native  long getUidRxBytes(int uid);
public static native  long getUidTxPackets(int uid);
public static native  long getUidRxPackets(int uid);
public static native  long getUidTcpTxBytes(int uid);
public static native  long getUidTcpRxBytes(int uid);
public static native  long getUidUdpTxBytes(int uid);
public static native  long getUidUdpRxBytes(int uid);
public static native  long getUidTcpTxSegments(int uid);
public static native  long getUidTcpRxSegments(int uid);
public static native  long getUidUdpTxPackets(int uid);
public static native  long getUidUdpRxPackets(int uid);
public static final int UNSUPPORTED = -1;
}
