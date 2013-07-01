package android.bluetooth;
public final class BluetoothA2dp
  implements android.bluetooth.BluetoothProfile
{
BluetoothA2dp() { throw new RuntimeException("Stub!"); }
public  java.util.List<android.bluetooth.BluetoothDevice> getConnectedDevices() { throw new RuntimeException("Stub!"); }
public  java.util.List<android.bluetooth.BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) { throw new RuntimeException("Stub!"); }
public  int getConnectionState(android.bluetooth.BluetoothDevice device) { throw new RuntimeException("Stub!"); }
public  boolean isA2dpPlaying(android.bluetooth.BluetoothDevice device) { throw new RuntimeException("Stub!"); }
public static final java.lang.String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED";
public static final java.lang.String ACTION_PLAYING_STATE_CHANGED = "android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED";
public static final int STATE_PLAYING = 10;
public static final int STATE_NOT_PLAYING = 11;
}
