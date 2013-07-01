package java.io;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class InputStreamReader extends java.io.Reader {

    public void close() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getEncoding() {
        throw new RuntimeException("Stub!");
    }

    public boolean ready() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "in", to = "this") })
    public InputStreamReader(java.io.InputStream in) {
    }

    @STAMP(flows = { @Flow(from = "in", to = "this") })
    public InputStreamReader(java.io.InputStream in, java.lang.String enc) throws java.io.UnsupportedEncodingException {
    }

    @STAMP(flows = { @Flow(from = "in", to = "this") })
    public InputStreamReader(java.io.InputStream in, java.nio.charset.CharsetDecoder dec) {
    }

    @STAMP(flows = { @Flow(from = "in", to = "this") })
    public InputStreamReader(java.io.InputStream in, java.nio.charset.Charset charset) {
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int read() throws java.io.IOException {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "buffer") })
    public int read(char[] buffer, int offset, int length) throws java.io.IOException {
        return 0;
    }
}

