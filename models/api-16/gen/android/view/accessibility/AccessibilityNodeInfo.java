package android.view.accessibility;
public class AccessibilityNodeInfo
  implements android.os.Parcelable
{
AccessibilityNodeInfo() { throw new RuntimeException("Stub!"); }
public  void setSource(android.view.View source) { throw new RuntimeException("Stub!"); }
public  void setSource(android.view.View root, int virtualDescendantId) { throw new RuntimeException("Stub!"); }
public  android.view.accessibility.AccessibilityNodeInfo findFocus(int focus) { throw new RuntimeException("Stub!"); }
public  android.view.accessibility.AccessibilityNodeInfo focusSearch(int direction) { throw new RuntimeException("Stub!"); }
public  int getWindowId() { throw new RuntimeException("Stub!"); }
public  int getChildCount() { throw new RuntimeException("Stub!"); }
public  android.view.accessibility.AccessibilityNodeInfo getChild(int index) { throw new RuntimeException("Stub!"); }
public  void addChild(android.view.View child) { throw new RuntimeException("Stub!"); }
public  void addChild(android.view.View root, int virtualDescendantId) { throw new RuntimeException("Stub!"); }
public  int getActions() { throw new RuntimeException("Stub!"); }
public  void addAction(int action) { throw new RuntimeException("Stub!"); }
public  void setMovementGranularities(int granularities) { throw new RuntimeException("Stub!"); }
public  int getMovementGranularities() { throw new RuntimeException("Stub!"); }
public  boolean performAction(int action) { throw new RuntimeException("Stub!"); }
public  boolean performAction(int action, android.os.Bundle arguments) { throw new RuntimeException("Stub!"); }
public  java.util.List<android.view.accessibility.AccessibilityNodeInfo> findAccessibilityNodeInfosByText(java.lang.String text) { throw new RuntimeException("Stub!"); }
public  android.view.accessibility.AccessibilityNodeInfo getParent() { throw new RuntimeException("Stub!"); }
public  void setParent(android.view.View parent) { throw new RuntimeException("Stub!"); }
public  void setParent(android.view.View root, int virtualDescendantId) { throw new RuntimeException("Stub!"); }
public  void getBoundsInParent(android.graphics.Rect outBounds) { throw new RuntimeException("Stub!"); }
public  void setBoundsInParent(android.graphics.Rect bounds) { throw new RuntimeException("Stub!"); }
public  void getBoundsInScreen(android.graphics.Rect outBounds) { throw new RuntimeException("Stub!"); }
public  void setBoundsInScreen(android.graphics.Rect bounds) { throw new RuntimeException("Stub!"); }
public  boolean isCheckable() { throw new RuntimeException("Stub!"); }
public  void setCheckable(boolean checkable) { throw new RuntimeException("Stub!"); }
public  boolean isChecked() { throw new RuntimeException("Stub!"); }
public  void setChecked(boolean checked) { throw new RuntimeException("Stub!"); }
public  boolean isFocusable() { throw new RuntimeException("Stub!"); }
public  void setFocusable(boolean focusable) { throw new RuntimeException("Stub!"); }
public  boolean isFocused() { throw new RuntimeException("Stub!"); }
public  void setFocused(boolean focused) { throw new RuntimeException("Stub!"); }
public  boolean isVisibleToUser() { throw new RuntimeException("Stub!"); }
public  void setVisibleToUser(boolean visibleToUser) { throw new RuntimeException("Stub!"); }
public  boolean isAccessibilityFocused() { throw new RuntimeException("Stub!"); }
public  void setAccessibilityFocused(boolean focused) { throw new RuntimeException("Stub!"); }
public  boolean isSelected() { throw new RuntimeException("Stub!"); }
public  void setSelected(boolean selected) { throw new RuntimeException("Stub!"); }
public  boolean isClickable() { throw new RuntimeException("Stub!"); }
public  void setClickable(boolean clickable) { throw new RuntimeException("Stub!"); }
public  boolean isLongClickable() { throw new RuntimeException("Stub!"); }
public  void setLongClickable(boolean longClickable) { throw new RuntimeException("Stub!"); }
public  boolean isEnabled() { throw new RuntimeException("Stub!"); }
public  void setEnabled(boolean enabled) { throw new RuntimeException("Stub!"); }
public  boolean isPassword() { throw new RuntimeException("Stub!"); }
public  void setPassword(boolean password) { throw new RuntimeException("Stub!"); }
public  boolean isScrollable() { throw new RuntimeException("Stub!"); }
public  void setScrollable(boolean scrollable) { throw new RuntimeException("Stub!"); }
public  java.lang.CharSequence getPackageName() { throw new RuntimeException("Stub!"); }
public  void setPackageName(java.lang.CharSequence packageName) { throw new RuntimeException("Stub!"); }
public  java.lang.CharSequence getClassName() { throw new RuntimeException("Stub!"); }
public  void setClassName(java.lang.CharSequence className) { throw new RuntimeException("Stub!"); }
public  java.lang.CharSequence getText() { throw new RuntimeException("Stub!"); }
public  void setText(java.lang.CharSequence text) { throw new RuntimeException("Stub!"); }
public  java.lang.CharSequence getContentDescription() { throw new RuntimeException("Stub!"); }
public  void setContentDescription(java.lang.CharSequence contentDescription) { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public static  android.view.accessibility.AccessibilityNodeInfo obtain(android.view.View source) { throw new RuntimeException("Stub!"); }
public static  android.view.accessibility.AccessibilityNodeInfo obtain(android.view.View root, int virtualDescendantId) { throw new RuntimeException("Stub!"); }
public static  android.view.accessibility.AccessibilityNodeInfo obtain() { throw new RuntimeException("Stub!"); }
public static  android.view.accessibility.AccessibilityNodeInfo obtain(android.view.accessibility.AccessibilityNodeInfo info) { throw new RuntimeException("Stub!"); }
public  void recycle() { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel parcel, int flags) { throw new RuntimeException("Stub!"); }
public  boolean equals(java.lang.Object object) { throw new RuntimeException("Stub!"); }
public  int hashCode() { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public static final int ACTION_FOCUS = 1;
public static final int ACTION_CLEAR_FOCUS = 2;
public static final int ACTION_SELECT = 4;
public static final int ACTION_CLEAR_SELECTION = 8;
public static final int ACTION_CLICK = 16;
public static final int ACTION_LONG_CLICK = 32;
public static final int ACTION_ACCESSIBILITY_FOCUS = 64;
public static final int ACTION_CLEAR_ACCESSIBILITY_FOCUS = 128;
public static final int ACTION_NEXT_AT_MOVEMENT_GRANULARITY = 256;
public static final int ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = 512;
public static final int ACTION_NEXT_HTML_ELEMENT = 1024;
public static final int ACTION_PREVIOUS_HTML_ELEMENT = 2048;
public static final int ACTION_SCROLL_FORWARD = 4096;
public static final int ACTION_SCROLL_BACKWARD = 8192;
public static final java.lang.String ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT = "ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT";
public static final java.lang.String ACTION_ARGUMENT_HTML_ELEMENT_STRING = "ACTION_ARGUMENT_HTML_ELEMENT_STRING";
public static final int FOCUS_INPUT = 1;
public static final int FOCUS_ACCESSIBILITY = 2;
public static final int MOVEMENT_GRANULARITY_CHARACTER = 1;
public static final int MOVEMENT_GRANULARITY_WORD = 2;
public static final int MOVEMENT_GRANULARITY_LINE = 4;
public static final int MOVEMENT_GRANULARITY_PARAGRAPH = 8;
public static final int MOVEMENT_GRANULARITY_PAGE = 16;
public static final android.os.Parcelable.Creator<android.view.accessibility.AccessibilityNodeInfo> CREATOR;
static { CREATOR = null; }
}
