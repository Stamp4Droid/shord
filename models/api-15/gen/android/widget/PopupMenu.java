package android.widget;
public class PopupMenu
{
public static interface OnDismissListener
{
public abstract  void onDismiss(android.widget.PopupMenu menu);
}
public static interface OnMenuItemClickListener
{
public abstract  boolean onMenuItemClick(android.view.MenuItem item);
}
public  PopupMenu(android.content.Context context, android.view.View anchor) { throw new RuntimeException("Stub!"); }
public  android.view.Menu getMenu() { throw new RuntimeException("Stub!"); }
public  android.view.MenuInflater getMenuInflater() { throw new RuntimeException("Stub!"); }
public  void inflate(int menuRes) { throw new RuntimeException("Stub!"); }
public  void show() { throw new RuntimeException("Stub!"); }
public  void dismiss() { throw new RuntimeException("Stub!"); }
public  void setOnMenuItemClickListener(android.widget.PopupMenu.OnMenuItemClickListener listener) { throw new RuntimeException("Stub!"); }
public  void setOnDismissListener(android.widget.PopupMenu.OnDismissListener listener) { throw new RuntimeException("Stub!"); }
}
