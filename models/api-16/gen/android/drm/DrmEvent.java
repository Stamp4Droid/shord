package android.drm;
public class DrmEvent
{
protected  DrmEvent(int uniqueId, int type, java.lang.String message, java.util.HashMap<java.lang.String, java.lang.Object> attributes) { throw new RuntimeException("Stub!"); }
protected  DrmEvent(int uniqueId, int type, java.lang.String message) { throw new RuntimeException("Stub!"); }
public  int getUniqueId() { throw new RuntimeException("Stub!"); }
public  int getType() { throw new RuntimeException("Stub!"); }
public  java.lang.String getMessage() { throw new RuntimeException("Stub!"); }
public  java.lang.Object getAttribute(java.lang.String key) { throw new RuntimeException("Stub!"); }
public static final int TYPE_ALL_RIGHTS_REMOVED = 1001;
public static final int TYPE_DRM_INFO_PROCESSED = 1002;
public static final java.lang.String DRM_INFO_STATUS_OBJECT = "drm_info_status_object";
public static final java.lang.String DRM_INFO_OBJECT = "drm_info_object";
}
