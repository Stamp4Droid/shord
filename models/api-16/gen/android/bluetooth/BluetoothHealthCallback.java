package android.bluetooth;
public abstract class BluetoothHealthCallback
{
public  BluetoothHealthCallback() { throw new RuntimeException("Stub!"); }
public  void onHealthAppConfigurationStatusChange(android.bluetooth.BluetoothHealthAppConfiguration config, int status) { throw new RuntimeException("Stub!"); }
public  void onHealthChannelStateChange(android.bluetooth.BluetoothHealthAppConfiguration config, android.bluetooth.BluetoothDevice device, int prevState, int newState, android.os.ParcelFileDescriptor fd, int channelId) { throw new RuntimeException("Stub!"); }
}
