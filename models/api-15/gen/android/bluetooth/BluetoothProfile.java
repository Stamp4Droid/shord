package android.bluetooth;
public interface BluetoothProfile
{
public static interface ServiceListener
{
public abstract  void onServiceConnected(int profile, android.bluetooth.BluetoothProfile proxy);
public abstract  void onServiceDisconnected(int profile);
}
public abstract  java.util.List<android.bluetooth.BluetoothDevice> getConnectedDevices();
public abstract  java.util.List<android.bluetooth.BluetoothDevice> getDevicesMatchingConnectionStates(int[] states);
public abstract  int getConnectionState(android.bluetooth.BluetoothDevice device);
public static final java.lang.String EXTRA_STATE = "android.bluetooth.profile.extra.STATE";
public static final java.lang.String EXTRA_PREVIOUS_STATE = "android.bluetooth.profile.extra.PREVIOUS_STATE";
public static final int STATE_DISCONNECTED = 0;
public static final int STATE_CONNECTING = 1;
public static final int STATE_CONNECTED = 2;
public static final int STATE_DISCONNECTING = 3;
public static final int HEADSET = 1;
public static final int A2DP = 2;
public static final int HEALTH = 3;
}
