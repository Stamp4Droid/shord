package android.widget;
public abstract class AdapterViewAnimator
  extends android.widget.AdapterView<android.widget.Adapter>
{
public  AdapterViewAnimator(android.content.Context context) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  AdapterViewAnimator(android.content.Context context, android.util.AttributeSet attrs) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  AdapterViewAnimator(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  void setDisplayedChild(int whichChild) { throw new RuntimeException("Stub!"); }
public  int getDisplayedChild() { throw new RuntimeException("Stub!"); }
public  void showNext() { throw new RuntimeException("Stub!"); }
public  void showPrevious() { throw new RuntimeException("Stub!"); }
public  boolean onTouchEvent(android.view.MotionEvent ev) { throw new RuntimeException("Stub!"); }
protected  void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { throw new RuntimeException("Stub!"); }
protected  void onLayout(boolean changed, int left, int top, int right, int bottom) { throw new RuntimeException("Stub!"); }
public  android.os.Parcelable onSaveInstanceState() { throw new RuntimeException("Stub!"); }
public  void onRestoreInstanceState(android.os.Parcelable state) { throw new RuntimeException("Stub!"); }
public  android.view.View getCurrentView() { throw new RuntimeException("Stub!"); }
public  android.animation.ObjectAnimator getInAnimation() { throw new RuntimeException("Stub!"); }
public  void setInAnimation(android.animation.ObjectAnimator inAnimation) { throw new RuntimeException("Stub!"); }
public  android.animation.ObjectAnimator getOutAnimation() { throw new RuntimeException("Stub!"); }
public  void setOutAnimation(android.animation.ObjectAnimator outAnimation) { throw new RuntimeException("Stub!"); }
public  void setInAnimation(android.content.Context context, int resourceID) { throw new RuntimeException("Stub!"); }
public  void setOutAnimation(android.content.Context context, int resourceID) { throw new RuntimeException("Stub!"); }
public  void setAnimateFirstView(boolean animate) { throw new RuntimeException("Stub!"); }
public  int getBaseline() { throw new RuntimeException("Stub!"); }
public  android.widget.Adapter getAdapter() { throw new RuntimeException("Stub!"); }
public  void setAdapter(android.widget.Adapter adapter) { throw new RuntimeException("Stub!"); }
public  void setRemoteViewsAdapter(android.content.Intent intent) { throw new RuntimeException("Stub!"); }
public  void setSelection(int position) { throw new RuntimeException("Stub!"); }
public  android.view.View getSelectedView() { throw new RuntimeException("Stub!"); }
public  void deferNotifyDataSetChanged() { throw new RuntimeException("Stub!"); }
public  boolean onRemoteAdapterConnected() { throw new RuntimeException("Stub!"); }
public  void onRemoteAdapterDisconnected() { throw new RuntimeException("Stub!"); }
public  void advance() { throw new RuntimeException("Stub!"); }
public  void fyiWillBeAdvancedByHostKThx() { throw new RuntimeException("Stub!"); }
}
