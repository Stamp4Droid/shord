package java.net;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public final class DatagramPacket {

    public synchronized java.net.InetAddress getAddress() {
        throw new RuntimeException("Stub!");
    }

    public synchronized byte[] getData() {
        throw new RuntimeException("Stub!");
    }

    public synchronized int getLength() {
        throw new RuntimeException("Stub!");
    }

    public synchronized int getOffset() {
        throw new RuntimeException("Stub!");
    }

    public synchronized int getPort() {
        throw new RuntimeException("Stub!");
    }

    public synchronized void setAddress(java.net.InetAddress addr) {
        throw new RuntimeException("Stub!");
    }

    public synchronized void setLength(int length) {
        throw new RuntimeException("Stub!");
    }

    public synchronized void setPort(int aPort) {
        throw new RuntimeException("Stub!");
    }

    public synchronized java.net.SocketAddress getSocketAddress() {
        throw new RuntimeException("Stub!");
    }

    public synchronized void setSocketAddress(java.net.SocketAddress sockAddr) {
        throw new RuntimeException("Stub!");
    }

    public DatagramPacket(byte[] data, int length) {
        transferTaint(data[0]);
    }

    public DatagramPacket(byte[] data, int offset, int length) {
        transferTaint(data[0]);
    }

    public DatagramPacket(byte[] data, int offset, int length, java.net.InetAddress host, int aPort) {
        transferTaint(data[0]);
    }

    public DatagramPacket(byte[] data, int length, java.net.InetAddress host, int port) {
        transferTaint(data[0]);
    }

    public DatagramPacket(byte[] data, int length, java.net.SocketAddress sockAddr) throws java.net.SocketException {
        transferTaint(data[0]);
    }

    public DatagramPacket(byte[] data, int offset, int length, java.net.SocketAddress sockAddr) throws java.net.SocketException {
        transferTaint(data[0]);
    }

    public synchronized void setData(byte[] data, int offset, int byteCount) {
        transferTaint(data[0]);
    }

    public synchronized void setData(byte[] buf) {
        transferTaint(buf[0]);
    }

    @STAMP(flows = { @Flow(from = "data", to = "this") })
    private void transferTaint(byte data) {
    }
}

