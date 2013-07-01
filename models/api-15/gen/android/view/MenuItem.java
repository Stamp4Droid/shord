package android.view;
public interface MenuItem
{
public static interface OnMenuItemClickListener
{
public abstract  boolean onMenuItemClick(android.view.MenuItem item);
}
public static interface OnActionExpandListener
{
public abstract  boolean onMenuItemActionExpand(android.view.MenuItem item);
public abstract  boolean onMenuItemActionCollapse(android.view.MenuItem item);
}
public abstract  int getItemId();
public abstract  int getGroupId();
public abstract  int getOrder();
public abstract  android.view.MenuItem setTitle(java.lang.CharSequence title);
public abstract  android.view.MenuItem setTitle(int title);
public abstract  java.lang.CharSequence getTitle();
public abstract  android.view.MenuItem setTitleCondensed(java.lang.CharSequence title);
public abstract  java.lang.CharSequence getTitleCondensed();
public abstract  android.view.MenuItem setIcon(android.graphics.drawable.Drawable icon);
public abstract  android.view.MenuItem setIcon(int iconRes);
public abstract  android.graphics.drawable.Drawable getIcon();
public abstract  android.view.MenuItem setIntent(android.content.Intent intent);
public abstract  android.content.Intent getIntent();
public abstract  android.view.MenuItem setShortcut(char numericChar, char alphaChar);
public abstract  android.view.MenuItem setNumericShortcut(char numericChar);
public abstract  char getNumericShortcut();
public abstract  android.view.MenuItem setAlphabeticShortcut(char alphaChar);
public abstract  char getAlphabeticShortcut();
public abstract  android.view.MenuItem setCheckable(boolean checkable);
public abstract  boolean isCheckable();
public abstract  android.view.MenuItem setChecked(boolean checked);
public abstract  boolean isChecked();
public abstract  android.view.MenuItem setVisible(boolean visible);
public abstract  boolean isVisible();
public abstract  android.view.MenuItem setEnabled(boolean enabled);
public abstract  boolean isEnabled();
public abstract  boolean hasSubMenu();
public abstract  android.view.SubMenu getSubMenu();
public abstract  android.view.MenuItem setOnMenuItemClickListener(android.view.MenuItem.OnMenuItemClickListener menuItemClickListener);
public abstract  android.view.ContextMenu.ContextMenuInfo getMenuInfo();
public abstract  void setShowAsAction(int actionEnum);
public abstract  android.view.MenuItem setShowAsActionFlags(int actionEnum);
public abstract  android.view.MenuItem setActionView(android.view.View view);
public abstract  android.view.MenuItem setActionView(int resId);
public abstract  android.view.View getActionView();
public abstract  android.view.MenuItem setActionProvider(android.view.ActionProvider actionProvider);
public abstract  android.view.ActionProvider getActionProvider();
public abstract  boolean expandActionView();
public abstract  boolean collapseActionView();
public abstract  boolean isActionViewExpanded();
public abstract  android.view.MenuItem setOnActionExpandListener(android.view.MenuItem.OnActionExpandListener listener);
public static final int SHOW_AS_ACTION_NEVER = 0;
public static final int SHOW_AS_ACTION_IF_ROOM = 1;
public static final int SHOW_AS_ACTION_ALWAYS = 2;
public static final int SHOW_AS_ACTION_WITH_TEXT = 4;
public static final int SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW = 8;
}
