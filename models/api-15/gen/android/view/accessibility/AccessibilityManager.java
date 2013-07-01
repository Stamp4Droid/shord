package android.view.accessibility;
public final class AccessibilityManager
{
public static interface AccessibilityStateChangeListener
{
public abstract  void onAccessibilityStateChanged(boolean enabled);
}
AccessibilityManager() { throw new RuntimeException("Stub!"); }
public  boolean isEnabled() { throw new RuntimeException("Stub!"); }
public  boolean isTouchExplorationEnabled() { throw new RuntimeException("Stub!"); }
public  void sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) { throw new RuntimeException("Stub!"); }
public  void interrupt() { throw new RuntimeException("Stub!"); }
public  java.util.List<android.content.pm.ServiceInfo> getAccessibilityServiceList() { throw new RuntimeException("Stub!"); }
public  java.util.List<android.accessibilityservice.AccessibilityServiceInfo> getInstalledAccessibilityServiceList() { throw new RuntimeException("Stub!"); }
public  java.util.List<android.accessibilityservice.AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackTypeFlags) { throw new RuntimeException("Stub!"); }
public  boolean addAccessibilityStateChangeListener(android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener listener) { throw new RuntimeException("Stub!"); }
public  boolean removeAccessibilityStateChangeListener(android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener listener) { throw new RuntimeException("Stub!"); }
}
