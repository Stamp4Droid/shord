package java.io;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public abstract class InputStream implements java.io.Closeable {

    public InputStream() {
        throw new RuntimeException("Stub!");
    }

    public int available() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public void close() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public void mark(int readlimit) {
        throw new RuntimeException("Stub!");
    }

    public boolean markSupported() {
        throw new RuntimeException("Stub!");
    }

    public abstract int read() throws java.io.IOException;

    public synchronized void reset() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public long skip(long byteCount) throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    private byte taintByte() {
        return (byte) 0;
    }

    public int read(byte[] buffer) throws java.io.IOException {
        buffer[0] = taintByte();
        return 0;
    }

    public int read(byte[] buffer, int offset, int length) throws java.io.IOException {
        buffer[0] = taintByte();
        return 0;
    }
}

