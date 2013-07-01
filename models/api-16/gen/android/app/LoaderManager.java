package android.app;
public abstract class LoaderManager
{
public static interface LoaderCallbacks<D>
{
public abstract  android.content.Loader<D> onCreateLoader(int id, android.os.Bundle args);
public abstract  void onLoadFinished(android.content.Loader<D> loader, D data);
public abstract  void onLoaderReset(android.content.Loader<D> loader);
}
public  LoaderManager() { throw new RuntimeException("Stub!"); }
public abstract <D> android.content.Loader<D> initLoader(int id, android.os.Bundle args, android.app.LoaderManager.LoaderCallbacks<D> callback);
public abstract <D> android.content.Loader<D> restartLoader(int id, android.os.Bundle args, android.app.LoaderManager.LoaderCallbacks<D> callback);
public abstract  void destroyLoader(int id);
public abstract <D> android.content.Loader<D> getLoader(int id);
public abstract  void dump(java.lang.String prefix, java.io.FileDescriptor fd, java.io.PrintWriter writer, java.lang.String[] args);
public static  void enableDebugLogging(boolean enabled) { throw new RuntimeException("Stub!"); }
}
