package java.nio.channels;

public class StampDatagramChannel extends java.nio.channels.DatagramChannel {
    public StampDatagramChannel() { super(null); }
    public java.nio.channels.DatagramChannel bind(java.net.SocketAddress local) { return null; }
    public java.nio.channels.DatagramChannel connect(java.net.SocketAddress addr) { return null; }
    public java.nio.channels.DatagramChannel disconnect() { return null; }
    public boolean isConnected() { return false; }
    public int read(java.nio.ByteBuffer dst) { return 0; }
    public long read(java.nio.ByteBuffer[] dsts, int offset, int length) { return 0L; }
    public java.net.SocketAddress receive(java.nio.ByteBuffer dst) { return null; }
    public int send(java.nio.ByteBuffer src, java.net.SocketAddress target) { return 0; }
    public <T> java.nio.channels.DatagramChannel setOption(java.net.SocketOption<T> name, T value) { return null; }
    public java.net.DatagramSocket socket() { return null; }
    public int write(java.nio.ByteBuffer src) { return 0; }
    public long write(java.nio.ByteBuffer[] srcs, int offset, int length) { return 0L; }

    protected void implCloseSelectableChannel() {}
    protected void implConfigureBlocking(boolean block) {}
}
