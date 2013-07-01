package android.telephony.gsm;
@java.lang.Deprecated()
public final class SmsManager
{
SmsManager() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public static final  android.telephony.gsm.SmsManager getDefault() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public final  void sendTextMessage(java.lang.String destinationAddress, java.lang.String scAddress, java.lang.String text, android.app.PendingIntent sentIntent, android.app.PendingIntent deliveryIntent) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public final  java.util.ArrayList<java.lang.String> divideMessage(java.lang.String text) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public final  void sendMultipartTextMessage(java.lang.String destinationAddress, java.lang.String scAddress, java.util.ArrayList<java.lang.String> parts, java.util.ArrayList<android.app.PendingIntent> sentIntents, java.util.ArrayList<android.app.PendingIntent> deliveryIntents) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public final  void sendDataMessage(java.lang.String destinationAddress, java.lang.String scAddress, short destinationPort, byte[] data, android.app.PendingIntent sentIntent, android.app.PendingIntent deliveryIntent) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public static final int STATUS_ON_SIM_FREE = 0;
@java.lang.Deprecated()
public static final int STATUS_ON_SIM_READ = 1;
@java.lang.Deprecated()
public static final int STATUS_ON_SIM_UNREAD = 3;
@java.lang.Deprecated()
public static final int STATUS_ON_SIM_SENT = 5;
@java.lang.Deprecated()
public static final int STATUS_ON_SIM_UNSENT = 7;
@java.lang.Deprecated()
public static final int RESULT_ERROR_GENERIC_FAILURE = 1;
@java.lang.Deprecated()
public static final int RESULT_ERROR_RADIO_OFF = 2;
@java.lang.Deprecated()
public static final int RESULT_ERROR_NULL_PDU = 3;
@java.lang.Deprecated()
public static final int RESULT_ERROR_NO_SERVICE = 4;
}
