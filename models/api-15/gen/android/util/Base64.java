package android.util;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class Base64 {

    Base64() {
        throw new RuntimeException("Stub!");
    }

    public static final int DEFAULT = 0;

    public static final int NO_PADDING = 1;

    public static final int NO_WRAP = 2;

    public static final int CRLF = 4;

    public static final int URL_SAFE = 8;

    public static final int NO_CLOSE = 16;

    @STAMP(flows = { @Flow(from = "str", to = "@return") })
    public static byte[] decode(java.lang.String str, int flags) {
        return new byte[0];
    }

    @STAMP(flows = { @Flow(from = "input", to = "@return") })
    public static byte[] decode(byte[] input, int flags) {
        return new byte[0];
    }

    @STAMP(flows = { @Flow(from = "input", to = "@return") })
    public static byte[] decode(byte[] input, int offset, int len, int flags) {
        return new byte[0];
    }

    @STAMP(flows = { @Flow(from = "input", to = "@return") })
    public static java.lang.String encodeToString(byte[] input, int flags) {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "input", to = "@return") })
    public static java.lang.String encodeToString(byte[] input, int offset, int len, int flags) {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "input", to = "@return") })
    public static byte[] encode(byte[] input, int flags) {
        return new byte[0];
    }

    @STAMP(flows = { @Flow(from = "input", to = "@return") })
    public static byte[] encode(byte[] input, int offset, int len, int flags) {
        return new byte[0];
    }
}

