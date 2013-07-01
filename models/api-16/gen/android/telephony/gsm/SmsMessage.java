package android.telephony.gsm;
@java.lang.Deprecated()
public class SmsMessage
{
@java.lang.Deprecated()
public static enum MessageClass
{
CLASS_0(),
CLASS_1(),
CLASS_2(),
CLASS_3(),
UNKNOWN();
}
@java.lang.Deprecated()
public static class SubmitPdu
{
@java.lang.Deprecated()
public  SubmitPdu() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public byte[] encodedScAddress = null;
@java.lang.Deprecated()
public byte[] encodedMessage = null;
}
@java.lang.Deprecated()
public  SmsMessage() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public static  android.telephony.gsm.SmsMessage createFromPdu(byte[] pdu) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public static  int getTPLayerLengthForPDU(java.lang.String pdu) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public static  int[] calculateLength(java.lang.CharSequence messageBody, boolean use7bitOnly) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public static  int[] calculateLength(java.lang.String messageBody, boolean use7bitOnly) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public static  android.telephony.gsm.SmsMessage.SubmitPdu getSubmitPdu(java.lang.String scAddress, java.lang.String destinationAddress, java.lang.String message, boolean statusReportRequested) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public static  android.telephony.gsm.SmsMessage.SubmitPdu getSubmitPdu(java.lang.String scAddress, java.lang.String destinationAddress, short destinationPort, byte[] data, boolean statusReportRequested) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  java.lang.String getServiceCenterAddress() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  java.lang.String getOriginatingAddress() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  java.lang.String getDisplayOriginatingAddress() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  java.lang.String getMessageBody() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  android.telephony.gsm.SmsMessage.MessageClass getMessageClass() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  java.lang.String getDisplayMessageBody() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  java.lang.String getPseudoSubject() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  long getTimestampMillis() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  boolean isEmail() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  java.lang.String getEmailBody() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  java.lang.String getEmailFrom() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  int getProtocolIdentifier() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  boolean isReplace() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  boolean isCphsMwiMessage() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  boolean isMWIClearMessage() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  boolean isMWISetMessage() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  boolean isMwiDontStore() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  byte[] getUserData() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  byte[] getPdu() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  int getStatusOnSim() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  int getIndexOnSim() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  int getStatus() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  boolean isStatusReportMessage() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  boolean isReplyPathPresent() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public static final int ENCODING_UNKNOWN = 0;
@java.lang.Deprecated()
public static final int ENCODING_7BIT = 1;
@java.lang.Deprecated()
public static final int ENCODING_8BIT = 2;
@java.lang.Deprecated()
public static final int ENCODING_16BIT = 3;
@java.lang.Deprecated()
public static final int MAX_USER_DATA_BYTES = 140;
@java.lang.Deprecated()
public static final int MAX_USER_DATA_SEPTETS = 160;
@java.lang.Deprecated()
public static final int MAX_USER_DATA_SEPTETS_WITH_HEADER = 153;
}
