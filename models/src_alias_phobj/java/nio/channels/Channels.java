class Channels {
    static java.io.OutputStream newOutputStream(final java.nio.channels.WritableByteChannel ch) {
	return new java.io.OutputStream() {
	    public void write(int b) throws java.io.IOException {
		java.nio.ByteBuffer src = new java.nio.StampByteBuffer();
		src.putInt(b);
		ch.write(src);
	    }
	};
    }
}
