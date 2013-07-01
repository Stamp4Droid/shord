package java.net;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class InetAddress implements java.io.Serializable {

    InetAddress() {
        throw new RuntimeException("Stub!");
    }

    public boolean equals(java.lang.Object obj) {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getCanonicalHostName() {
        throw new RuntimeException("Stub!");
    }

    public static java.net.InetAddress getLocalHost() throws java.net.UnknownHostException {
        throw new RuntimeException("Stub!");
    }

    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String toString() {
        throw new RuntimeException("Stub!");
    }

    public boolean isAnyLocalAddress() {
        throw new RuntimeException("Stub!");
    }

    public boolean isLinkLocalAddress() {
        throw new RuntimeException("Stub!");
    }

    public boolean isLoopbackAddress() {
        throw new RuntimeException("Stub!");
    }

    public boolean isMCGlobal() {
        throw new RuntimeException("Stub!");
    }

    public boolean isMCLinkLocal() {
        throw new RuntimeException("Stub!");
    }

    public boolean isMCNodeLocal() {
        throw new RuntimeException("Stub!");
    }

    public boolean isMCOrgLocal() {
        throw new RuntimeException("Stub!");
    }

    public boolean isMCSiteLocal() {
        throw new RuntimeException("Stub!");
    }

    public boolean isMulticastAddress() {
        throw new RuntimeException("Stub!");
    }

    public boolean isSiteLocalAddress() {
        throw new RuntimeException("Stub!");
    }

    public boolean isReachable(int timeout) throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public boolean isReachable(java.net.NetworkInterface networkInterface, int ttl, int timeout) throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public static java.net.InetAddress getByAddress(byte[] ipAddress) throws java.net.UnknownHostException {
        throw new RuntimeException("Stub!");
    }

    public static java.net.InetAddress getByAddress(java.lang.String hostName, byte[] ipAddress) throws java.net.UnknownHostException {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public byte[] getAddress() {
        return new byte[0];
    }

    public static java.net.InetAddress[] getAllByName(java.lang.String host) throws java.net.UnknownHostException {
        InetAddress[] addrs = new InetAddress[1];
        addrs[0] = getByName(host);
        return addrs;
    }

    @STAMP(flows = { @Flow(from = "host", to = "@return") })
    public static java.net.InetAddress getByName(java.lang.String host) throws java.net.UnknownHostException {
        return new InetAddress();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String getHostAddress() {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String getHostName() {
        return new String();
    }
}

