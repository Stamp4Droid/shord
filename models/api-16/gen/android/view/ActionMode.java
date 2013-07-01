package android.view;
public abstract class ActionMode
{
public static interface Callback
{
public abstract  boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu);
public abstract  boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu);
public abstract  boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item);
public abstract  void onDestroyActionMode(android.view.ActionMode mode);
}
public  ActionMode() { throw new RuntimeException("Stub!"); }
public  void setTag(java.lang.Object tag) { throw new RuntimeException("Stub!"); }
public  java.lang.Object getTag() { throw new RuntimeException("Stub!"); }
public abstract  void setTitle(java.lang.CharSequence title);
public abstract  void setTitle(int resId);
public abstract  void setSubtitle(java.lang.CharSequence subtitle);
public abstract  void setSubtitle(int resId);
public  void setTitleOptionalHint(boolean titleOptional) { throw new RuntimeException("Stub!"); }
public  boolean getTitleOptionalHint() { throw new RuntimeException("Stub!"); }
public  boolean isTitleOptional() { throw new RuntimeException("Stub!"); }
public abstract  void setCustomView(android.view.View view);
public abstract  void invalidate();
public abstract  void finish();
public abstract  android.view.Menu getMenu();
public abstract  java.lang.CharSequence getTitle();
public abstract  java.lang.CharSequence getSubtitle();
public abstract  android.view.View getCustomView();
public abstract  android.view.MenuInflater getMenuInflater();
}
