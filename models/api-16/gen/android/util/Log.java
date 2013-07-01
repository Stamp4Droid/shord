package android.util;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public final class Log {

    Log() {
        throw new RuntimeException("Stub!");
    }

    public static native boolean isLoggable(java.lang.String tag, int level);

    public static int w(java.lang.String tag, java.lang.Throwable tr) {
        throw new RuntimeException("Stub!");
    }

    public static int wtf(java.lang.String tag, java.lang.Throwable tr) {
        throw new RuntimeException("Stub!");
    }

    public static java.lang.String getStackTraceString(java.lang.Throwable tr) {
        throw new RuntimeException("Stub!");
    }

    public static int println(int priority, java.lang.String tag, java.lang.String msg) {
        throw new RuntimeException("Stub!");
    }

    public static final int VERBOSE = 2;

    public static final int DEBUG = 3;

    public static final int INFO = 4;

    public static final int WARN = 5;

    public static final int ERROR = 6;

    public static final int ASSERT = 7;

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int v(java.lang.String tag, java.lang.String msg) {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int v(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int d(java.lang.String tag, java.lang.String msg) {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int d(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int i(java.lang.String tag, java.lang.String msg) {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int i(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int w(java.lang.String tag, java.lang.String msg) {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int w(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int e(java.lang.String tag, java.lang.String msg) {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int e(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int wtf(java.lang.String tag, java.lang.String msg) {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "msg", to = "!LOG") })
    public static int wtf(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) {
        return 0;
    }
}

