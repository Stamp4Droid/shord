package android.app;
public class FragmentBreadCrumbs
  extends android.view.ViewGroup
  implements android.app.FragmentManager.OnBackStackChangedListener
{
public static interface OnBreadCrumbClickListener
{
public abstract  boolean onBreadCrumbClick(android.app.FragmentManager.BackStackEntry backStack, int flags);
}
public  FragmentBreadCrumbs(android.content.Context context) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  FragmentBreadCrumbs(android.content.Context context, android.util.AttributeSet attrs) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  FragmentBreadCrumbs(android.content.Context context, android.util.AttributeSet attrs, int defStyle) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  void setActivity(android.app.Activity a) { throw new RuntimeException("Stub!"); }
public  void setMaxVisible(int visibleCrumbs) { throw new RuntimeException("Stub!"); }
public  void setParentTitle(java.lang.CharSequence title, java.lang.CharSequence shortTitle, android.view.View.OnClickListener listener) { throw new RuntimeException("Stub!"); }
public  void setOnBreadCrumbClickListener(android.app.FragmentBreadCrumbs.OnBreadCrumbClickListener listener) { throw new RuntimeException("Stub!"); }
public  void setTitle(java.lang.CharSequence title, java.lang.CharSequence shortTitle) { throw new RuntimeException("Stub!"); }
protected  void onLayout(boolean changed, int l, int t, int r, int b) { throw new RuntimeException("Stub!"); }
protected  void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { throw new RuntimeException("Stub!"); }
public  void onBackStackChanged() { throw new RuntimeException("Stub!"); }
}
