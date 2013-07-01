package android.content;
public abstract class ContentProvider
  implements android.content.ComponentCallbacks2
{
public static interface PipeDataWriter<T>
{
public abstract  void writeDataToPipe(android.os.ParcelFileDescriptor output, android.net.Uri uri, java.lang.String mimeType, android.os.Bundle opts, T args);
}
public  ContentProvider() { throw new RuntimeException("Stub!"); }
public final  android.content.Context getContext() { throw new RuntimeException("Stub!"); }
protected final  void setReadPermission(java.lang.String permission) { throw new RuntimeException("Stub!"); }
public final  java.lang.String getReadPermission() { throw new RuntimeException("Stub!"); }
protected final  void setWritePermission(java.lang.String permission) { throw new RuntimeException("Stub!"); }
public final  java.lang.String getWritePermission() { throw new RuntimeException("Stub!"); }
protected final  void setPathPermissions(android.content.pm.PathPermission[] permissions) { throw new RuntimeException("Stub!"); }
public final  android.content.pm.PathPermission[] getPathPermissions() { throw new RuntimeException("Stub!"); }
public abstract  boolean onCreate();
public  void onConfigurationChanged(android.content.res.Configuration newConfig) { throw new RuntimeException("Stub!"); }
public  void onLowMemory() { throw new RuntimeException("Stub!"); }
public  void onTrimMemory(int level) { throw new RuntimeException("Stub!"); }
public abstract  android.database.Cursor query(android.net.Uri uri, java.lang.String[] projection, java.lang.String selection, java.lang.String[] selectionArgs, java.lang.String sortOrder);
public abstract  java.lang.String getType(android.net.Uri uri);
public abstract  android.net.Uri insert(android.net.Uri uri, android.content.ContentValues values);
public  int bulkInsert(android.net.Uri uri, android.content.ContentValues[] values) { throw new RuntimeException("Stub!"); }
public abstract  int delete(android.net.Uri uri, java.lang.String selection, java.lang.String[] selectionArgs);
public abstract  int update(android.net.Uri uri, android.content.ContentValues values, java.lang.String selection, java.lang.String[] selectionArgs);
public  android.os.ParcelFileDescriptor openFile(android.net.Uri uri, java.lang.String mode) throws java.io.FileNotFoundException { throw new RuntimeException("Stub!"); }
public  android.content.res.AssetFileDescriptor openAssetFile(android.net.Uri uri, java.lang.String mode) throws java.io.FileNotFoundException { throw new RuntimeException("Stub!"); }
protected final  android.os.ParcelFileDescriptor openFileHelper(android.net.Uri uri, java.lang.String mode) throws java.io.FileNotFoundException { throw new RuntimeException("Stub!"); }
public  java.lang.String[] getStreamTypes(android.net.Uri uri, java.lang.String mimeTypeFilter) { throw new RuntimeException("Stub!"); }
public  android.content.res.AssetFileDescriptor openTypedAssetFile(android.net.Uri uri, java.lang.String mimeTypeFilter, android.os.Bundle opts) throws java.io.FileNotFoundException { throw new RuntimeException("Stub!"); }
public <T> android.os.ParcelFileDescriptor openPipeHelper(android.net.Uri uri, java.lang.String mimeType, android.os.Bundle opts, T args, android.content.ContentProvider.PipeDataWriter<T> func) throws java.io.FileNotFoundException { throw new RuntimeException("Stub!"); }
protected  boolean isTemporary() { throw new RuntimeException("Stub!"); }
public  void attachInfo(android.content.Context context, android.content.pm.ProviderInfo info) { throw new RuntimeException("Stub!"); }
public  android.content.ContentProviderResult[] applyBatch(java.util.ArrayList<android.content.ContentProviderOperation> operations) throws android.content.OperationApplicationException { throw new RuntimeException("Stub!"); }
public  android.os.Bundle call(java.lang.String method, java.lang.String arg, android.os.Bundle extras) { throw new RuntimeException("Stub!"); }
public  void shutdown() { throw new RuntimeException("Stub!"); }
}
