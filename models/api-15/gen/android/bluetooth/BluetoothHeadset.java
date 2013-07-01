package android.bluetooth;
public final class BluetoothHeadset
  implements android.bluetooth.BluetoothProfile
{
BluetoothHeadset() { throw new RuntimeException("Stub!"); }
public  java.util.List<android.bluetooth.BluetoothDevice> getConnectedDevices() { throw new RuntimeException("Stub!"); }
public  java.util.List<android.bluetooth.BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) { throw new RuntimeException("Stub!"); }
public  int getConnectionState(android.bluetooth.BluetoothDevice device) { throw new RuntimeException("Stub!"); }
public  boolean startVoiceRecognition(android.bluetooth.BluetoothDevice device) { throw new RuntimeException("Stub!"); }
public  boolean stopVoiceRecognition(android.bluetooth.BluetoothDevice device) { throw new RuntimeException("Stub!"); }
public  boolean isAudioConnected(android.bluetooth.BluetoothDevice device) { throw new RuntimeException("Stub!"); }
public static final java.lang.String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED";
public static final java.lang.String ACTION_AUDIO_STATE_CHANGED = "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED";
public static final java.lang.String ACTION_VENDOR_SPECIFIC_HEADSET_EVENT = "android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT";
public static final java.lang.String EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD = "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_CMD";
public static final java.lang.String EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE = "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE";
public static final int AT_CMD_TYPE_READ = 0;
public static final int AT_CMD_TYPE_TEST = 1;
public static final int AT_CMD_TYPE_SET = 2;
public static final int AT_CMD_TYPE_BASIC = 3;
public static final int AT_CMD_TYPE_ACTION = 4;
public static final java.lang.String EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS = "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_ARGS";
public static final java.lang.String VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY = "android.bluetooth.headset.intent.category.companyid";
public static final int STATE_AUDIO_DISCONNECTED = 10;
public static final int STATE_AUDIO_CONNECTING = 11;
public static final int STATE_AUDIO_CONNECTED = 12;
}
