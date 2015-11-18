package java.nio.channels;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

class StampSocketChannel extends SocketChannel {
    private java.net.SocketAddress socketAddress;
    private java.net.Socket socket;

    public StampSocketChannel() {
	super(null);
	this.socket = new java.net.Socket();
    }

    public java.nio.channels.SocketChannel bind(java.net.SocketAddress local) throws java.io.IOException {
	this.socketAddress = local;
	this.socket.bind(local);
	return this;
    }

    public boolean connect(java.net.SocketAddress remote) throws java.io.IOException {
	this.socketAddress = remote;
	this.socket.connect(remote);
	return true;
    }

    public boolean finishConnect() throws java.io.IOException {
	return true;
    }

    public java.net.SocketAddress getRemoteAddress() throws java.io.IOException {
	return this.socketAddress;
    }

    public boolean isConnected() {
	return true;
    }

    public boolean isConnectionPending() {
	return true;
    }

    public int read(java.nio.ByteBuffer dst) throws java.io.IOException {
	dst.putInt(this.socket.getInputStream().read());
	return 0;
    }

    public long read(java.nio.ByteBuffer[] dsts, int offset, int length) throws java.io.IOException {
	for(java.nio.ByteBuffer dst : dsts) {
	    this.read(dst);
	}
	return (long)0;
    }

    public <T> java.nio.channels.SocketChannel setOption(java.net.SocketOption<T> name, T value) throws java.io.IOException {
	return this;
    }

    public java.nio.channels.SocketChannel shutdownInput() throws java.io.IOException {
	return this;
    }

    public java.nio.channels.SocketChannel shutdownOutput() throws java.io.IOException {
	return this;
    }

    public java.net.Socket socket() {
	return this.socket;
    }

    public int write(java.nio.ByteBuffer src) throws java.io.IOException {
	this.socket.getOutputStream().write(src.getInt());
	return 0;
    }

    public long write(java.nio.ByteBuffer[] srcs, int offset, int length) throws java.io.IOException {
	for(java.nio.ByteBuffer src : srcs) {
	    this.write(src);
	}
	return 0;
    }

    protected void implCloseSelectableChannel() {}

    protected void implConfigureBlocking(boolean block) {}
}

