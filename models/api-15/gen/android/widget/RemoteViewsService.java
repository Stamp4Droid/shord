package android.widget;
public abstract class RemoteViewsService
  extends android.app.Service
{
public static interface RemoteViewsFactory
{
public abstract  void onCreate();
public abstract  void onDataSetChanged();
public abstract  void onDestroy();
public abstract  int getCount();
public abstract  android.widget.RemoteViews getViewAt(int position);
public abstract  android.widget.RemoteViews getLoadingView();
public abstract  int getViewTypeCount();
public abstract  long getItemId(int position);
public abstract  boolean hasStableIds();
}
public  RemoteViewsService() { throw new RuntimeException("Stub!"); }
public  android.os.IBinder onBind(android.content.Intent intent) { throw new RuntimeException("Stub!"); }
public abstract  android.widget.RemoteViewsService.RemoteViewsFactory onGetViewFactory(android.content.Intent intent);
}
