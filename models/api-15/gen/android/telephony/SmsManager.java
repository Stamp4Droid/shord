package android.telephony;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public final class SmsManager {

    SmsManager() {
        throw new RuntimeException("Stub!");
    }

    public static final int STATUS_ON_ICC_FREE = 0;

    public static final int STATUS_ON_ICC_READ = 1;

    public static final int STATUS_ON_ICC_UNREAD = 3;

    public static final int STATUS_ON_ICC_SENT = 5;

    public static final int STATUS_ON_ICC_UNSENT = 7;

    public static final int RESULT_ERROR_GENERIC_FAILURE = 1;

    public static final int RESULT_ERROR_RADIO_OFF = 2;

    public static final int RESULT_ERROR_NULL_PDU = 3;

    public static final int RESULT_ERROR_NO_SERVICE = 4;

    @STAMP(flows = { @Flow(from = "text", to = "!sendTextMessage") })
    public void sendTextMessage(java.lang.String destinationAddress, java.lang.String scAddress, java.lang.String text, android.app.PendingIntent sentIntent, android.app.PendingIntent deliveryIntent) {
    }

    public java.util.ArrayList<java.lang.String> divideMessage(java.lang.String text) {
        java.util.ArrayList<java.lang.String> result = new java.util.ArrayList<java.lang.String>();
        result.add(text);
        return result;
    }

    @STAMP(flows = { @Flow(from = "parts", to = "!sendMultipartTextMessage") })
    public void sendMultipartTextMessage(java.lang.String destinationAddress, java.lang.String scAddress, java.util.ArrayList<java.lang.String> parts, java.util.ArrayList<android.app.PendingIntent> sentIntents, java.util.ArrayList<android.app.PendingIntent> deliveryIntents) {
    }

    @STAMP(flows = { @Flow(from = "data", to = "!sendDataMessage") })
    public void sendDataMessage(java.lang.String destinationAddress, java.lang.String scAddress, short destinationPort, byte[] data, android.app.PendingIntent sentIntent, android.app.PendingIntent deliveryIntent) {
    }

    private static SmsManager smsManager = new SmsManager();

    public static android.telephony.SmsManager getDefault() {
        return smsManager;
    }
}

