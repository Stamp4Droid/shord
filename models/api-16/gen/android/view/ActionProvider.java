package android.view;
public abstract class ActionProvider
{
public static interface VisibilityListener
{
public abstract  void onActionProviderVisibilityChanged(boolean isVisible);
}
public  ActionProvider(android.content.Context context) { throw new RuntimeException("Stub!"); }
@Deprecated
public abstract  android.view.View onCreateActionView();
public  android.view.View onCreateActionView(android.view.MenuItem forItem) { throw new RuntimeException("Stub!"); }
public  boolean overridesItemVisibility() { throw new RuntimeException("Stub!"); }
public  boolean isVisible() { throw new RuntimeException("Stub!"); }
public  void refreshVisibility() { throw new RuntimeException("Stub!"); }
public  boolean onPerformDefaultAction() { throw new RuntimeException("Stub!"); }
public  boolean hasSubMenu() { throw new RuntimeException("Stub!"); }
public  void onPrepareSubMenu(android.view.SubMenu subMenu) { throw new RuntimeException("Stub!"); }
public  void setVisibilityListener(android.view.ActionProvider.VisibilityListener listener) { throw new RuntimeException("Stub!"); }
}
