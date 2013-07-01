package android.hardware.input;
public final class InputManager
{
public static interface InputDeviceListener
{
public abstract  void onInputDeviceAdded(int deviceId);
public abstract  void onInputDeviceRemoved(int deviceId);
public abstract  void onInputDeviceChanged(int deviceId);
}
InputManager() { throw new RuntimeException("Stub!"); }
public  android.view.InputDevice getInputDevice(int id) { throw new RuntimeException("Stub!"); }
public  int[] getInputDeviceIds() { throw new RuntimeException("Stub!"); }
public  void registerInputDeviceListener(android.hardware.input.InputManager.InputDeviceListener listener, android.os.Handler handler) { throw new RuntimeException("Stub!"); }
public  void unregisterInputDeviceListener(android.hardware.input.InputManager.InputDeviceListener listener) { throw new RuntimeException("Stub!"); }
public static final java.lang.String ACTION_QUERY_KEYBOARD_LAYOUTS = "android.hardware.input.action.QUERY_KEYBOARD_LAYOUTS";
public static final java.lang.String META_DATA_KEYBOARD_LAYOUTS = "android.hardware.input.metadata.KEYBOARD_LAYOUTS";
}
