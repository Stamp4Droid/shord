package java.io;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class BufferedReader extends java.io.Reader {

    public void close() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public void mark(int markLimit) throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public boolean markSupported() {
        throw new RuntimeException("Stub!");
    }

    public boolean ready() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public void reset() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public long skip(long byteCount) throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "in", to = "this") })
    public BufferedReader(java.io.Reader in) {
    }

    @STAMP(flows = { @Flow(from = "in", to = "this") })
    public BufferedReader(java.io.Reader in, int size) {
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int read() throws java.io.IOException {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "buffer") })
    public int read(char[] buffer, int offset, int length) throws java.io.IOException {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String readLine() throws java.io.IOException {
        return new String();
    }
}

