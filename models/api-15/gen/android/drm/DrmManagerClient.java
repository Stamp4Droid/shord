package android.drm;
public class DrmManagerClient
{
public static interface OnInfoListener
{
public abstract  void onInfo(android.drm.DrmManagerClient client, android.drm.DrmInfoEvent event);
}
public static interface OnEventListener
{
public abstract  void onEvent(android.drm.DrmManagerClient client, android.drm.DrmEvent event);
}
public static interface OnErrorListener
{
public abstract  void onError(android.drm.DrmManagerClient client, android.drm.DrmErrorEvent event);
}
public  DrmManagerClient(android.content.Context context) { throw new RuntimeException("Stub!"); }
protected  void finalize() { throw new RuntimeException("Stub!"); }
public synchronized  void setOnInfoListener(android.drm.DrmManagerClient.OnInfoListener infoListener) { throw new RuntimeException("Stub!"); }
public synchronized  void setOnEventListener(android.drm.DrmManagerClient.OnEventListener eventListener) { throw new RuntimeException("Stub!"); }
public synchronized  void setOnErrorListener(android.drm.DrmManagerClient.OnErrorListener errorListener) { throw new RuntimeException("Stub!"); }
public  java.lang.String[] getAvailableDrmEngines() { throw new RuntimeException("Stub!"); }
public  android.content.ContentValues getConstraints(java.lang.String path, int action) { throw new RuntimeException("Stub!"); }
public  android.content.ContentValues getMetadata(java.lang.String path) { throw new RuntimeException("Stub!"); }
public  android.content.ContentValues getConstraints(android.net.Uri uri, int action) { throw new RuntimeException("Stub!"); }
public  android.content.ContentValues getMetadata(android.net.Uri uri) { throw new RuntimeException("Stub!"); }
public  int saveRights(android.drm.DrmRights drmRights, java.lang.String rightsPath, java.lang.String contentPath) throws java.io.IOException { throw new RuntimeException("Stub!"); }
public  boolean canHandle(java.lang.String path, java.lang.String mimeType) { throw new RuntimeException("Stub!"); }
public  boolean canHandle(android.net.Uri uri, java.lang.String mimeType) { throw new RuntimeException("Stub!"); }
public  int processDrmInfo(android.drm.DrmInfo drmInfo) { throw new RuntimeException("Stub!"); }
public  android.drm.DrmInfo acquireDrmInfo(android.drm.DrmInfoRequest drmInfoRequest) { throw new RuntimeException("Stub!"); }
public  int acquireRights(android.drm.DrmInfoRequest drmInfoRequest) { throw new RuntimeException("Stub!"); }
public  int getDrmObjectType(java.lang.String path, java.lang.String mimeType) { throw new RuntimeException("Stub!"); }
public  int getDrmObjectType(android.net.Uri uri, java.lang.String mimeType) { throw new RuntimeException("Stub!"); }
public  java.lang.String getOriginalMimeType(java.lang.String path) { throw new RuntimeException("Stub!"); }
public  java.lang.String getOriginalMimeType(android.net.Uri uri) { throw new RuntimeException("Stub!"); }
public  int checkRightsStatus(java.lang.String path) { throw new RuntimeException("Stub!"); }
public  int checkRightsStatus(android.net.Uri uri) { throw new RuntimeException("Stub!"); }
public  int checkRightsStatus(java.lang.String path, int action) { throw new RuntimeException("Stub!"); }
public  int checkRightsStatus(android.net.Uri uri, int action) { throw new RuntimeException("Stub!"); }
public  int removeRights(java.lang.String path) { throw new RuntimeException("Stub!"); }
public  int removeRights(android.net.Uri uri) { throw new RuntimeException("Stub!"); }
public  int removeAllRights() { throw new RuntimeException("Stub!"); }
public  int openConvertSession(java.lang.String mimeType) { throw new RuntimeException("Stub!"); }
public  android.drm.DrmConvertedStatus convertData(int convertId, byte[] inputData) { throw new RuntimeException("Stub!"); }
public  android.drm.DrmConvertedStatus closeConvertSession(int convertId) { throw new RuntimeException("Stub!"); }
public static final int ERROR_NONE = 0;
public static final int ERROR_UNKNOWN = -2000;
}
