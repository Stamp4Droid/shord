package android.accessibilityservice;
public abstract class AccessibilityService
  extends android.app.Service
{
public  AccessibilityService() { throw new RuntimeException("Stub!"); }
public abstract  void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event);
public abstract  void onInterrupt();
protected  void onServiceConnected() { throw new RuntimeException("Stub!"); }
protected  boolean onGesture(int gestureId) { throw new RuntimeException("Stub!"); }
public  android.view.accessibility.AccessibilityNodeInfo getRootInActiveWindow() { throw new RuntimeException("Stub!"); }
public final  boolean performGlobalAction(int action) { throw new RuntimeException("Stub!"); }
public final  android.accessibilityservice.AccessibilityServiceInfo getServiceInfo() { throw new RuntimeException("Stub!"); }
public final  void setServiceInfo(android.accessibilityservice.AccessibilityServiceInfo info) { throw new RuntimeException("Stub!"); }
public final  android.os.IBinder onBind(android.content.Intent intent) { throw new RuntimeException("Stub!"); }
public static final int GESTURE_SWIPE_UP = 1;
public static final int GESTURE_SWIPE_DOWN = 2;
public static final int GESTURE_SWIPE_LEFT = 3;
public static final int GESTURE_SWIPE_RIGHT = 4;
public static final int GESTURE_SWIPE_LEFT_AND_RIGHT = 5;
public static final int GESTURE_SWIPE_RIGHT_AND_LEFT = 6;
public static final int GESTURE_SWIPE_UP_AND_DOWN = 7;
public static final int GESTURE_SWIPE_DOWN_AND_UP = 8;
public static final int GESTURE_SWIPE_LEFT_AND_UP = 9;
public static final int GESTURE_SWIPE_LEFT_AND_DOWN = 10;
public static final int GESTURE_SWIPE_RIGHT_AND_UP = 11;
public static final int GESTURE_SWIPE_RIGHT_AND_DOWN = 12;
public static final int GESTURE_SWIPE_UP_AND_LEFT = 13;
public static final int GESTURE_SWIPE_UP_AND_RIGHT = 14;
public static final int GESTURE_SWIPE_DOWN_AND_LEFT = 15;
public static final int GESTURE_SWIPE_DOWN_AND_RIGHT = 16;
public static final java.lang.String SERVICE_INTERFACE = "android.accessibilityservice.AccessibilityService";
public static final java.lang.String SERVICE_META_DATA = "android.accessibilityservice";
public static final int GLOBAL_ACTION_BACK = 1;
public static final int GLOBAL_ACTION_HOME = 2;
public static final int GLOBAL_ACTION_RECENTS = 3;
public static final int GLOBAL_ACTION_NOTIFICATIONS = 4;
}
