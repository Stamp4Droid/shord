package android.view.accessibility;
public abstract class AccessibilityNodeProvider
{
public  AccessibilityNodeProvider() { throw new RuntimeException("Stub!"); }
public  android.view.accessibility.AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) { throw new RuntimeException("Stub!"); }
public  boolean performAction(int virtualViewId, int action, android.os.Bundle arguments) { throw new RuntimeException("Stub!"); }
public  java.util.List<android.view.accessibility.AccessibilityNodeInfo> findAccessibilityNodeInfosByText(java.lang.String text, int virtualViewId) { throw new RuntimeException("Stub!"); }
}
