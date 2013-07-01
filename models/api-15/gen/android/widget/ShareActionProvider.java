package android.widget;
public class ShareActionProvider
  extends android.view.ActionProvider
{
public static interface OnShareTargetSelectedListener
{
public abstract  boolean onShareTargetSelected(android.widget.ShareActionProvider source, android.content.Intent intent);
}
public  ShareActionProvider(android.content.Context context) { super((android.content.Context)null); throw new RuntimeException("Stub!"); }
public  void setOnShareTargetSelectedListener(android.widget.ShareActionProvider.OnShareTargetSelectedListener listener) { throw new RuntimeException("Stub!"); }
public  android.view.View onCreateActionView() { throw new RuntimeException("Stub!"); }
public  boolean hasSubMenu() { throw new RuntimeException("Stub!"); }
public  void onPrepareSubMenu(android.view.SubMenu subMenu) { throw new RuntimeException("Stub!"); }
public  void setShareHistoryFileName(java.lang.String shareHistoryFile) { throw new RuntimeException("Stub!"); }
public  void setShareIntent(android.content.Intent shareIntent) { throw new RuntimeException("Stub!"); }
public static final java.lang.String DEFAULT_SHARE_HISTORY_FILE_NAME = "share_history.xml";
}
