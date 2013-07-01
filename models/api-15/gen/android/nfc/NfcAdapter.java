package android.nfc;
public final class NfcAdapter
{
public static interface OnNdefPushCompleteCallback
{
public abstract  void onNdefPushComplete(android.nfc.NfcEvent event);
}
public static interface CreateNdefMessageCallback
{
public abstract  android.nfc.NdefMessage createNdefMessage(android.nfc.NfcEvent event);
}
NfcAdapter() { throw new RuntimeException("Stub!"); }
public static  android.nfc.NfcAdapter getDefaultAdapter(android.content.Context context) { throw new RuntimeException("Stub!"); }
public static  android.nfc.NfcAdapter getDefaultAdapter() { throw new RuntimeException("Stub!"); }
public  boolean isEnabled() { throw new RuntimeException("Stub!"); }
public  void setNdefPushMessage(android.nfc.NdefMessage message, android.app.Activity activity, android.app.Activity... activities) { throw new RuntimeException("Stub!"); }
public  void setNdefPushMessageCallback(android.nfc.NfcAdapter.CreateNdefMessageCallback callback, android.app.Activity activity, android.app.Activity... activities) { throw new RuntimeException("Stub!"); }
public  void setOnNdefPushCompleteCallback(android.nfc.NfcAdapter.OnNdefPushCompleteCallback callback, android.app.Activity activity, android.app.Activity... activities) { throw new RuntimeException("Stub!"); }
public  void enableForegroundDispatch(android.app.Activity activity, android.app.PendingIntent intent, android.content.IntentFilter[] filters, java.lang.String[][] techLists) { throw new RuntimeException("Stub!"); }
public  void disableForegroundDispatch(android.app.Activity activity) { throw new RuntimeException("Stub!"); }
public  void enableForegroundNdefPush(android.app.Activity activity, android.nfc.NdefMessage message) { throw new RuntimeException("Stub!"); }
public  void disableForegroundNdefPush(android.app.Activity activity) { throw new RuntimeException("Stub!"); }
public static final java.lang.String ACTION_NDEF_DISCOVERED = "android.nfc.action.NDEF_DISCOVERED";
public static final java.lang.String ACTION_TECH_DISCOVERED = "android.nfc.action.TECH_DISCOVERED";
public static final java.lang.String ACTION_TAG_DISCOVERED = "android.nfc.action.TAG_DISCOVERED";
public static final java.lang.String EXTRA_TAG = "android.nfc.extra.TAG";
public static final java.lang.String EXTRA_NDEF_MESSAGES = "android.nfc.extra.NDEF_MESSAGES";
public static final java.lang.String EXTRA_ID = "android.nfc.extra.ID";
}
