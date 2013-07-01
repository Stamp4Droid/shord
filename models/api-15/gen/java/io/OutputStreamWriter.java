package java.io;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class OutputStreamWriter extends java.io.Writer {

    public OutputStreamWriter(java.io.OutputStream out) {
        throw new RuntimeException("Stub!");
    }

    public OutputStreamWriter(java.io.OutputStream out, java.lang.String enc) throws java.io.UnsupportedEncodingException {
        throw new RuntimeException("Stub!");
    }

    public OutputStreamWriter(java.io.OutputStream out, java.nio.charset.Charset cs) {
        throw new RuntimeException("Stub!");
    }

    public OutputStreamWriter(java.io.OutputStream out, java.nio.charset.CharsetEncoder enc) {
        throw new RuntimeException("Stub!");
    }

    public void close() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public void flush() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getEncoding() {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "buffer", to = "!FILE") })
    public void write(char[] buffer, int offset, int count) throws java.io.IOException {
        return;
    }

    @STAMP(flows = { @Flow(from = "oneChar", to = "!FILE") })
    public void write(int oneChar) throws java.io.IOException {
        return;
    }

    @STAMP(flows = { @Flow(from = "str", to = "!FILE") })
    public void write(java.lang.String str, int offset, int count) throws java.io.IOException {
        return;
    }
}

