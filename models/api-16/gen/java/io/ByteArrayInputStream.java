package java.io;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class ByteArrayInputStream extends java.io.InputStream {

    public synchronized int available() {
        throw new RuntimeException("Stub!");
    }

    public void close() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public synchronized void mark(int readlimit) {
        throw new RuntimeException("Stub!");
    }

    public boolean markSupported() {
        throw new RuntimeException("Stub!");
    }

    public synchronized void reset() {
        throw new RuntimeException("Stub!");
    }

    public synchronized long skip(long byteCount) {
        throw new RuntimeException("Stub!");
    }

    protected byte[] buf = null;

    protected int pos;

    protected int mark;

    protected int count;

    @STAMP(flows = { @Flow(from = "buf", to = "this") })
    public ByteArrayInputStream(byte[] buf) {
    }

    @STAMP(flows = { @Flow(from = "buf", to = "this") })
    public ByteArrayInputStream(byte[] buf, int offset, int length) {
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public synchronized int read() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "buffer") })
    public synchronized int read(byte[] buffer, int offset, int length) {
        return 0;
    }
}

