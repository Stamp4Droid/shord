package android.content;
public class CursorLoader
  extends android.content.AsyncTaskLoader<android.database.Cursor>
{
public  CursorLoader(android.content.Context context) { super((android.content.Context)null); throw new RuntimeException("Stub!"); }
public  CursorLoader(android.content.Context context, android.net.Uri uri, java.lang.String[] projection, java.lang.String selection, java.lang.String[] selectionArgs, java.lang.String sortOrder) { super((android.content.Context)null); throw new RuntimeException("Stub!"); }
public  android.database.Cursor loadInBackground() { throw new RuntimeException("Stub!"); }
public  void cancelLoadInBackground() { throw new RuntimeException("Stub!"); }
public  void deliverResult(android.database.Cursor cursor) { throw new RuntimeException("Stub!"); }
protected  void onStartLoading() { throw new RuntimeException("Stub!"); }
protected  void onStopLoading() { throw new RuntimeException("Stub!"); }
public  void onCanceled(android.database.Cursor cursor) { throw new RuntimeException("Stub!"); }
protected  void onReset() { throw new RuntimeException("Stub!"); }
public  android.net.Uri getUri() { throw new RuntimeException("Stub!"); }
public  void setUri(android.net.Uri uri) { throw new RuntimeException("Stub!"); }
public  java.lang.String[] getProjection() { throw new RuntimeException("Stub!"); }
public  void setProjection(java.lang.String[] projection) { throw new RuntimeException("Stub!"); }
public  java.lang.String getSelection() { throw new RuntimeException("Stub!"); }
public  void setSelection(java.lang.String selection) { throw new RuntimeException("Stub!"); }
public  java.lang.String[] getSelectionArgs() { throw new RuntimeException("Stub!"); }
public  void setSelectionArgs(java.lang.String[] selectionArgs) { throw new RuntimeException("Stub!"); }
public  java.lang.String getSortOrder() { throw new RuntimeException("Stub!"); }
public  void setSortOrder(java.lang.String sortOrder) { throw new RuntimeException("Stub!"); }
public  void dump(java.lang.String prefix, java.io.FileDescriptor fd, java.io.PrintWriter writer, java.lang.String[] args) { throw new RuntimeException("Stub!"); }
}
