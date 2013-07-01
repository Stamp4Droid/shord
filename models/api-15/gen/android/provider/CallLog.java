package android.provider;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class CallLog {

    public static class Calls implements android.provider.BaseColumns {

        public Calls() {
            throw new RuntimeException("Stub!");
        }

        public static final android.net.Uri CONTENT_URI;

        public static final android.net.Uri CONTENT_FILTER_URI;

        public static final java.lang.String DEFAULT_SORT_ORDER = "date DESC";

        public static final java.lang.String CONTENT_TYPE = "vnd.android.cursor.dir/calls";

        public static final java.lang.String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/calls";

        public static final java.lang.String TYPE = "type";

        public static final int INCOMING_TYPE = 1;

        public static final int OUTGOING_TYPE = 2;

        public static final int MISSED_TYPE = 3;

        public static final java.lang.String NUMBER = "number";

        public static final java.lang.String DATE = "date";

        public static final java.lang.String DURATION = "duration";

        public static final java.lang.String NEW = "new";

        public static final java.lang.String CACHED_NAME = "name";

        public static final java.lang.String CACHED_NUMBER_TYPE = "numbertype";

        public static final java.lang.String CACHED_NUMBER_LABEL = "numberlabel";

        public static final java.lang.String IS_READ = "is_read";

        static {
            CONTENT_URI = taintedCallLog();
            CONTENT_FILTER_URI = taintedCallLog();
        }

        @STAMP(flows = { @Flow(from = "$CallLog.LastOutgoingCall", to = "@return") })
        public static java.lang.String getLastOutgoingCall(android.content.Context context) {
            return new String();
        }
    }

    public CallLog() {
        throw new RuntimeException("Stub!");
    }

    public static final java.lang.String AUTHORITY = "call_log";

    public static final android.net.Uri CONTENT_URI;

    static {
        CONTENT_URI = taintedCallLog();
    }

    @STAMP(flows = { @Flow(from = "$CallLog", to = "@return") })
    private static android.net.Uri taintedCallLog() {
        return new android.net.StampUri("");
    }
}

