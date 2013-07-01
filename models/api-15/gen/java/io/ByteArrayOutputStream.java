package java.io;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class ByteArrayOutputStream extends java.io.OutputStream {

    public void close() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public synchronized void reset() {
        throw new RuntimeException("Stub!");
    }

    public int size() {
        throw new RuntimeException("Stub!");
    }

    protected byte[] buf = null;

    protected int count;

    public ByteArrayOutputStream() {
    }

    public ByteArrayOutputStream(int size) {
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public synchronized byte[] toByteArray() {
        return new byte[1];
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String toString() {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String toString(int hibyte) {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String toString(java.lang.String enc) throws java.io.UnsupportedEncodingException {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "buffer", to = "this") })
    public synchronized void write(byte[] buffer, int offset, int len) {
    }

    @STAMP(flows = { @Flow(from = "oneByte", to = "this") })
    public synchronized void write(int oneByte) {
    }

    public synchronized void writeTo(java.io.OutputStream out) throws java.io.IOException {
        out.write(toByteArray());
    }
}

