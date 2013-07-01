package java.io;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class FileInputStream extends java.io.InputStream implements java.io.Closeable {

    public int available() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public void close() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    protected void finalize() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public java.nio.channels.FileChannel getChannel() {
        throw new RuntimeException("Stub!");
    }

    public long skip(long byteCount) throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "file", to = "this") })
    public FileInputStream(java.io.File file) throws java.io.FileNotFoundException {
    }

    @STAMP(flows = { @Flow(from = "fd", to = "this") })
    public FileInputStream(java.io.FileDescriptor fd) {
    }

    @STAMP(flows = { @Flow(from = "path", to = "this"), @Flow(from = "$File", to = "this") })
    public FileInputStream(java.lang.String path) throws java.io.FileNotFoundException {
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    private byte taintByte() {
        return (byte) 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int read() throws java.io.IOException {
        return 0;
    }

    public int read(byte[] buffer, int byteOffset, int byteCount) throws java.io.IOException {
        buffer[0] = taintByte();
        return 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public final java.io.FileDescriptor getFD() throws java.io.IOException {
        return new FileDescriptor();
    }
}

