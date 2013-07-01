package java.io;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public abstract class OutputStream implements java.io.Closeable, java.io.Flushable {

    public OutputStream() {
        throw new RuntimeException("Stub!");
    }

    public void close() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public void flush() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "buffer", to = "!this") })
    public void write(byte[] buffer) throws java.io.IOException {
    }

    @STAMP(flows = { @Flow(from = "buffer", to = "!this") })
    public void write(byte[] buffer, int offset, int count) throws java.io.IOException {
    }

    @STAMP(flows = { @Flow(from = "oneByte", to = "!this") })
    public abstract void write(int oneByte) throws java.io.IOException;
}

