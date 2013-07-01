package android.content;
public class Loader<D>
{
public final class ForceLoadContentObserver
  extends android.database.ContentObserver
{
public  ForceLoadContentObserver() { super((android.os.Handler)null); throw new RuntimeException("Stub!"); }
public  boolean deliverSelfNotifications() { throw new RuntimeException("Stub!"); }
public  void onChange(boolean selfChange) { throw new RuntimeException("Stub!"); }
}
public static interface OnLoadCompleteListener<D>
{
public abstract  void onLoadComplete(android.content.Loader<D> loader, D data);
}
public static interface OnLoadCanceledListener<D>
{
public abstract  void onLoadCanceled(android.content.Loader<D> loader);
}
public  Loader(android.content.Context context) { throw new RuntimeException("Stub!"); }
public  void deliverResult(D data) { throw new RuntimeException("Stub!"); }
public  void deliverCancellation() { throw new RuntimeException("Stub!"); }
public  android.content.Context getContext() { throw new RuntimeException("Stub!"); }
public  int getId() { throw new RuntimeException("Stub!"); }
public  void registerListener(int id, android.content.Loader.OnLoadCompleteListener<D> listener) { throw new RuntimeException("Stub!"); }
public  void unregisterListener(android.content.Loader.OnLoadCompleteListener<D> listener) { throw new RuntimeException("Stub!"); }
public  void registerOnLoadCanceledListener(android.content.Loader.OnLoadCanceledListener<D> listener) { throw new RuntimeException("Stub!"); }
public  void unregisterOnLoadCanceledListener(android.content.Loader.OnLoadCanceledListener<D> listener) { throw new RuntimeException("Stub!"); }
public  boolean isStarted() { throw new RuntimeException("Stub!"); }
public  boolean isAbandoned() { throw new RuntimeException("Stub!"); }
public  boolean isReset() { throw new RuntimeException("Stub!"); }
public final  void startLoading() { throw new RuntimeException("Stub!"); }
protected  void onStartLoading() { throw new RuntimeException("Stub!"); }
public  boolean cancelLoad() { throw new RuntimeException("Stub!"); }
protected  boolean onCancelLoad() { throw new RuntimeException("Stub!"); }
public  void forceLoad() { throw new RuntimeException("Stub!"); }
protected  void onForceLoad() { throw new RuntimeException("Stub!"); }
public  void stopLoading() { throw new RuntimeException("Stub!"); }
protected  void onStopLoading() { throw new RuntimeException("Stub!"); }
public  void abandon() { throw new RuntimeException("Stub!"); }
protected  void onAbandon() { throw new RuntimeException("Stub!"); }
public  void reset() { throw new RuntimeException("Stub!"); }
protected  void onReset() { throw new RuntimeException("Stub!"); }
public  boolean takeContentChanged() { throw new RuntimeException("Stub!"); }
public  void onContentChanged() { throw new RuntimeException("Stub!"); }
public  java.lang.String dataToString(D data) { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public  void dump(java.lang.String prefix, java.io.FileDescriptor fd, java.io.PrintWriter writer, java.lang.String[] args) { throw new RuntimeException("Stub!"); }
}
