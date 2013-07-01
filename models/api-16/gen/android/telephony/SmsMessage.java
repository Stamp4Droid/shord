package android.telephony;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class SmsMessage {

    public static enum MessageClass {

        CLASS_0, CLASS_1, CLASS_2, CLASS_3, UNKNOWN
    }

    public static class SubmitPdu {

        SubmitPdu() {
            throw new RuntimeException("Stub!");
        }

        public java.lang.String toString() {
            throw new RuntimeException("Stub!");
        }

        public byte[] encodedScAddress = null;

        public byte[] encodedMessage = null;
    }

    SmsMessage() {
        throw new RuntimeException("Stub!");
    }

    public static int getTPLayerLengthForPDU(java.lang.String pdu) {
        throw new RuntimeException("Stub!");
    }

    public static int[] calculateLength(java.lang.CharSequence msgBody, boolean use7bitOnly) {
        throw new RuntimeException("Stub!");
    }

    public static int[] calculateLength(java.lang.String messageBody, boolean use7bitOnly) {
        throw new RuntimeException("Stub!");
    }

    public static android.telephony.SmsMessage.SubmitPdu getSubmitPdu(java.lang.String scAddress, java.lang.String destinationAddress, java.lang.String message, boolean statusReportRequested) {
        throw new RuntimeException("Stub!");
    }

    public static android.telephony.SmsMessage.SubmitPdu getSubmitPdu(java.lang.String scAddress, java.lang.String destinationAddress, short destinationPort, byte[] data, boolean statusReportRequested) {
        throw new RuntimeException("Stub!");
    }

    public android.telephony.SmsMessage.MessageClass getMessageClass() {
        throw new RuntimeException("Stub!");
    }

    public static final int ENCODING_UNKNOWN = 0;

    public static final int ENCODING_7BIT = 1;

    public static final int ENCODING_8BIT = 2;

    public static final int ENCODING_16BIT = 3;

    public static final int MAX_USER_DATA_BYTES = 140;

    public static final int MAX_USER_DATA_BYTES_WITH_HEADER = 134;

    public static final int MAX_USER_DATA_SEPTETS = 160;

    public static final int MAX_USER_DATA_SEPTETS_WITH_HEADER = 153;

    @STAMP(flows = { @Flow(from = "pdu", to = "@return") })
    public static android.telephony.SmsMessage createFromPdu(byte[] pdu) {
        return new SmsMessage();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String getServiceCenterAddress() {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String getOriginatingAddress() {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String getDisplayOriginatingAddress() {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String getMessageBody() {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String getDisplayMessageBody() {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String getPseudoSubject() {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public long getTimestampMillis() {
        return 0L;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public boolean isEmail() {
        return false;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String getEmailBody() {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String getEmailFrom() {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int getProtocolIdentifier() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public boolean isReplace() {
        return false;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public boolean isCphsMwiMessage() {
        return false;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public boolean isMWIClearMessage() {
        return false;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public boolean isMWISetMessage() {
        return false;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public boolean isMwiDontStore() {
        return false;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public byte[] getUserData() {
        return new byte[1];
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public byte[] getPdu() {
        return new byte[1];
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int getStatusOnSim() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int getStatusOnIcc() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int getIndexOnSim() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int getIndexOnIcc() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public int getStatus() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public boolean isStatusReportMessage() {
        return false;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public boolean isReplyPathPresent() {
        return false;
    }
}

