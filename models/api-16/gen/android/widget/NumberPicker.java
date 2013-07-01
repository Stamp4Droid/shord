package android.widget;
public class NumberPicker
  extends android.widget.LinearLayout
{
public static interface OnValueChangeListener
{
public abstract  void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal);
}
public static interface OnScrollListener
{
public abstract  void onScrollStateChange(android.widget.NumberPicker view, int scrollState);
public static final int SCROLL_STATE_IDLE = 0;
public static final int SCROLL_STATE_TOUCH_SCROLL = 1;
public static final int SCROLL_STATE_FLING = 2;
}
public static interface Formatter
{
public abstract  java.lang.String format(int value);
}
public  NumberPicker(android.content.Context context) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  NumberPicker(android.content.Context context, android.util.AttributeSet attrs) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  NumberPicker(android.content.Context context, android.util.AttributeSet attrs, int defStyle) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
protected  void onLayout(boolean changed, int left, int top, int right, int bottom) { throw new RuntimeException("Stub!"); }
protected  void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { throw new RuntimeException("Stub!"); }
public  boolean onInterceptTouchEvent(android.view.MotionEvent event) { throw new RuntimeException("Stub!"); }
public  boolean onTouchEvent(android.view.MotionEvent event) { throw new RuntimeException("Stub!"); }
public  boolean dispatchTouchEvent(android.view.MotionEvent event) { throw new RuntimeException("Stub!"); }
public  boolean dispatchKeyEvent(android.view.KeyEvent event) { throw new RuntimeException("Stub!"); }
public  boolean dispatchTrackballEvent(android.view.MotionEvent event) { throw new RuntimeException("Stub!"); }
protected  boolean dispatchHoverEvent(android.view.MotionEvent event) { throw new RuntimeException("Stub!"); }
public  void computeScroll() { throw new RuntimeException("Stub!"); }
public  void setEnabled(boolean enabled) { throw new RuntimeException("Stub!"); }
public  void scrollBy(int x, int y) { throw new RuntimeException("Stub!"); }
public  int getSolidColor() { throw new RuntimeException("Stub!"); }
public  void setOnValueChangedListener(android.widget.NumberPicker.OnValueChangeListener onValueChangedListener) { throw new RuntimeException("Stub!"); }
public  void setOnScrollListener(android.widget.NumberPicker.OnScrollListener onScrollListener) { throw new RuntimeException("Stub!"); }
public  void setFormatter(android.widget.NumberPicker.Formatter formatter) { throw new RuntimeException("Stub!"); }
public  void setValue(int value) { throw new RuntimeException("Stub!"); }
public  boolean getWrapSelectorWheel() { throw new RuntimeException("Stub!"); }
public  void setWrapSelectorWheel(boolean wrapSelectorWheel) { throw new RuntimeException("Stub!"); }
public  void setOnLongPressUpdateInterval(long intervalMillis) { throw new RuntimeException("Stub!"); }
public  int getValue() { throw new RuntimeException("Stub!"); }
public  int getMinValue() { throw new RuntimeException("Stub!"); }
public  void setMinValue(int minValue) { throw new RuntimeException("Stub!"); }
public  int getMaxValue() { throw new RuntimeException("Stub!"); }
public  void setMaxValue(int maxValue) { throw new RuntimeException("Stub!"); }
public  java.lang.String[] getDisplayedValues() { throw new RuntimeException("Stub!"); }
public  void setDisplayedValues(java.lang.String[] displayedValues) { throw new RuntimeException("Stub!"); }
protected  float getTopFadingEdgeStrength() { throw new RuntimeException("Stub!"); }
protected  float getBottomFadingEdgeStrength() { throw new RuntimeException("Stub!"); }
protected  void onDetachedFromWindow() { throw new RuntimeException("Stub!"); }
protected  void onDraw(android.graphics.Canvas canvas) { throw new RuntimeException("Stub!"); }
public  void addFocusables(java.util.ArrayList<android.view.View> views, int direction, int focusableMode) { throw new RuntimeException("Stub!"); }
public  void onInitializeAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) { throw new RuntimeException("Stub!"); }
public  android.view.accessibility.AccessibilityNodeProvider getAccessibilityNodeProvider() { throw new RuntimeException("Stub!"); }
}
