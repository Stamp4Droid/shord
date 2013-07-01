package android.app;
public abstract class ActionBar
{
public static interface OnNavigationListener
{
public abstract  boolean onNavigationItemSelected(int itemPosition, long itemId);
}
public static interface OnMenuVisibilityListener
{
public abstract  void onMenuVisibilityChanged(boolean isVisible);
}
public abstract static class Tab
{
public  Tab() { throw new RuntimeException("Stub!"); }
public abstract  int getPosition();
public abstract  android.graphics.drawable.Drawable getIcon();
public abstract  java.lang.CharSequence getText();
public abstract  android.app.ActionBar.Tab setIcon(android.graphics.drawable.Drawable icon);
public abstract  android.app.ActionBar.Tab setIcon(int resId);
public abstract  android.app.ActionBar.Tab setText(java.lang.CharSequence text);
public abstract  android.app.ActionBar.Tab setText(int resId);
public abstract  android.app.ActionBar.Tab setCustomView(android.view.View view);
public abstract  android.app.ActionBar.Tab setCustomView(int layoutResId);
public abstract  android.view.View getCustomView();
public abstract  android.app.ActionBar.Tab setTag(java.lang.Object obj);
public abstract  java.lang.Object getTag();
public abstract  android.app.ActionBar.Tab setTabListener(android.app.ActionBar.TabListener listener);
public abstract  void select();
public abstract  android.app.ActionBar.Tab setContentDescription(int resId);
public abstract  android.app.ActionBar.Tab setContentDescription(java.lang.CharSequence contentDesc);
public abstract  java.lang.CharSequence getContentDescription();
public static final int INVALID_POSITION = -1;
}
public static interface TabListener
{
public abstract  void onTabSelected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction ft);
public abstract  void onTabUnselected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction ft);
public abstract  void onTabReselected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction ft);
}
public static class LayoutParams
  extends android.view.ViewGroup.MarginLayoutParams
{
public  LayoutParams(android.content.Context c, android.util.AttributeSet attrs) { super((android.view.ViewGroup.LayoutParams)null); throw new RuntimeException("Stub!"); }
public  LayoutParams(int width, int height) { super((android.view.ViewGroup.LayoutParams)null); throw new RuntimeException("Stub!"); }
public  LayoutParams(int width, int height, int gravity) { super((android.view.ViewGroup.LayoutParams)null); throw new RuntimeException("Stub!"); }
public  LayoutParams(int gravity) { super((android.view.ViewGroup.LayoutParams)null); throw new RuntimeException("Stub!"); }
public  LayoutParams(android.app.ActionBar.LayoutParams source) { super((android.view.ViewGroup.LayoutParams)null); throw new RuntimeException("Stub!"); }
public  LayoutParams(android.view.ViewGroup.LayoutParams source) { super((android.view.ViewGroup.LayoutParams)null); throw new RuntimeException("Stub!"); }
@android.view.ViewDebug.ExportedProperty(category="layout",mapping={@android.view.ViewDebug.IntToString(from=-1,to="NONE"),@android.view.ViewDebug.IntToString(from=0,to="NONE"),@android.view.ViewDebug.IntToString(from=48,to="TOP"),@android.view.ViewDebug.IntToString(from=80,to="BOTTOM"),@android.view.ViewDebug.IntToString(from=3,to="LEFT"),@android.view.ViewDebug.IntToString(from=5,to="RIGHT"),@android.view.ViewDebug.IntToString(from=16,to="CENTER_VERTICAL"),@android.view.ViewDebug.IntToString(from=112,to="FILL_VERTICAL"),@android.view.ViewDebug.IntToString(from=1,to="CENTER_HORIZONTAL"),@android.view.ViewDebug.IntToString(from=7,to="FILL_HORIZONTAL"),@android.view.ViewDebug.IntToString(from=17,to="CENTER"),@android.view.ViewDebug.IntToString(from=119,to="FILL")})
public int gravity;
}
public  ActionBar() { throw new RuntimeException("Stub!"); }
public abstract  void setCustomView(android.view.View view);
public abstract  void setCustomView(android.view.View view, android.app.ActionBar.LayoutParams layoutParams);
public abstract  void setCustomView(int resId);
public abstract  void setIcon(int resId);
public abstract  void setIcon(android.graphics.drawable.Drawable icon);
public abstract  void setLogo(int resId);
public abstract  void setLogo(android.graphics.drawable.Drawable logo);
public abstract  void setListNavigationCallbacks(android.widget.SpinnerAdapter adapter, android.app.ActionBar.OnNavigationListener callback);
public abstract  void setSelectedNavigationItem(int position);
public abstract  int getSelectedNavigationIndex();
public abstract  int getNavigationItemCount();
public abstract  void setTitle(java.lang.CharSequence title);
public abstract  void setTitle(int resId);
public abstract  void setSubtitle(java.lang.CharSequence subtitle);
public abstract  void setSubtitle(int resId);
public abstract  void setDisplayOptions(int options);
public abstract  void setDisplayOptions(int options, int mask);
public abstract  void setDisplayUseLogoEnabled(boolean useLogo);
public abstract  void setDisplayShowHomeEnabled(boolean showHome);
public abstract  void setDisplayHomeAsUpEnabled(boolean showHomeAsUp);
public abstract  void setDisplayShowTitleEnabled(boolean showTitle);
public abstract  void setDisplayShowCustomEnabled(boolean showCustom);
public abstract  void setBackgroundDrawable(android.graphics.drawable.Drawable d);
public  void setStackedBackgroundDrawable(android.graphics.drawable.Drawable d) { throw new RuntimeException("Stub!"); }
public  void setSplitBackgroundDrawable(android.graphics.drawable.Drawable d) { throw new RuntimeException("Stub!"); }
public abstract  android.view.View getCustomView();
public abstract  java.lang.CharSequence getTitle();
public abstract  java.lang.CharSequence getSubtitle();
public abstract  int getNavigationMode();
public abstract  void setNavigationMode(int mode);
public abstract  int getDisplayOptions();
public abstract  android.app.ActionBar.Tab newTab();
public abstract  void addTab(android.app.ActionBar.Tab tab);
public abstract  void addTab(android.app.ActionBar.Tab tab, boolean setSelected);
public abstract  void addTab(android.app.ActionBar.Tab tab, int position);
public abstract  void addTab(android.app.ActionBar.Tab tab, int position, boolean setSelected);
public abstract  void removeTab(android.app.ActionBar.Tab tab);
public abstract  void removeTabAt(int position);
public abstract  void removeAllTabs();
public abstract  void selectTab(android.app.ActionBar.Tab tab);
public abstract  android.app.ActionBar.Tab getSelectedTab();
public abstract  android.app.ActionBar.Tab getTabAt(int index);
public abstract  int getTabCount();
public abstract  int getHeight();
public abstract  void show();
public abstract  void hide();
public abstract  boolean isShowing();
public abstract  void addOnMenuVisibilityListener(android.app.ActionBar.OnMenuVisibilityListener listener);
public abstract  void removeOnMenuVisibilityListener(android.app.ActionBar.OnMenuVisibilityListener listener);
public  void setHomeButtonEnabled(boolean enabled) { throw new RuntimeException("Stub!"); }
public  android.content.Context getThemedContext() { throw new RuntimeException("Stub!"); }
public static final int NAVIGATION_MODE_STANDARD = 0;
public static final int NAVIGATION_MODE_LIST = 1;
public static final int NAVIGATION_MODE_TABS = 2;
public static final int DISPLAY_USE_LOGO = 1;
public static final int DISPLAY_SHOW_HOME = 2;
public static final int DISPLAY_HOME_AS_UP = 4;
public static final int DISPLAY_SHOW_TITLE = 8;
public static final int DISPLAY_SHOW_CUSTOM = 16;
}
