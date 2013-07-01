package android.view;
public abstract class ActionProvider
{
public  ActionProvider(android.content.Context context) { throw new RuntimeException("Stub!"); }
public abstract  android.view.View onCreateActionView();
public  boolean onPerformDefaultAction() { throw new RuntimeException("Stub!"); }
public  boolean hasSubMenu() { throw new RuntimeException("Stub!"); }
public  void onPrepareSubMenu(android.view.SubMenu subMenu) { throw new RuntimeException("Stub!"); }
}
