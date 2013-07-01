package java.net;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class InetSocketAddress extends java.net.SocketAddress {

    public InetSocketAddress(int port) {
        throw new RuntimeException("Stub!");
    }

    public final int getPort() {
        throw new RuntimeException("Stub!");
    }

    public final java.net.InetAddress getAddress() {
        throw new RuntimeException("Stub!");
    }

    public final java.lang.String getHostName() {
        throw new RuntimeException("Stub!");
    }

    public final boolean isUnresolved() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String toString() {
        throw new RuntimeException("Stub!");
    }

    public final boolean equals(java.lang.Object socketAddr) {
        throw new RuntimeException("Stub!");
    }

    public final int hashCode() {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "address", to = "this") })
    public InetSocketAddress(java.net.InetAddress address, int port) {
    }

    @STAMP(flows = { @Flow(from = "host", to = "this") })
    public InetSocketAddress(java.lang.String host, int port) {
    }

    public static java.net.InetSocketAddress createUnresolved(java.lang.String host, int port) {
        return new InetSocketAddress(host, port);
    }
}

