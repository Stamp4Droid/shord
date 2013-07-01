package android.content;
public abstract class AsyncTaskLoader<D>
  extends android.content.Loader<D>
{
public  AsyncTaskLoader(android.content.Context context) { super((android.content.Context)null); throw new RuntimeException("Stub!"); }
public  void setUpdateThrottle(long delayMS) { throw new RuntimeException("Stub!"); }
protected  void onForceLoad() { throw new RuntimeException("Stub!"); }
protected  boolean onCancelLoad() { throw new RuntimeException("Stub!"); }
public  void onCanceled(D data) { throw new RuntimeException("Stub!"); }
public abstract  D loadInBackground();
protected  D onLoadInBackground() { throw new RuntimeException("Stub!"); }
public  void cancelLoadInBackground() { throw new RuntimeException("Stub!"); }
public  boolean isLoadInBackgroundCanceled() { throw new RuntimeException("Stub!"); }
public  void dump(java.lang.String prefix, java.io.FileDescriptor fd, java.io.PrintWriter writer, java.lang.String[] args) { throw new RuntimeException("Stub!"); }
}
