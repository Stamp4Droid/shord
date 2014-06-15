class SocketChannel {
    public static java.nio.channels.SocketChannel open() {
	return new java.nio.channels.StampSocketChannel();
    }

    public static java.nio.channels.SocketChannel open(java.net.SocketAddress remote) throws java.io.IOException {
	java.nio.channels.SocketChannel socketChannel = new java.nio.channels.StampSocketChannel();
	socketChannel.connect(remote);
	return socketChannel;
    }

    public SocketChannel(java.nio.channels.spi.SelectorProvider provider) {
	super(provider);
    }

    public long read(java.nio.ByteBuffer[] dsts) throws java.io.IOException {
	this.read(dsts, 0, 0);
	return (long)0;
    }

    public int validOps() {
	return 0;
    }
    
    public long write(java.nio.ByteBuffer[] srcs) throws java.io.IOException {
	for(java.nio.ByteBuffer src : srcs) {
	    this.write(src);
	}
	return 0;
    }
}
