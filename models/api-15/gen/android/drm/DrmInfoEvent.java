package android.drm;
public class DrmInfoEvent
  extends android.drm.DrmEvent
{
public  DrmInfoEvent(int uniqueId, int type, java.lang.String message) { super(0,0,(java.lang.String)null); throw new RuntimeException("Stub!"); }
public  DrmInfoEvent(int uniqueId, int type, java.lang.String message, java.util.HashMap<java.lang.String, java.lang.Object> attributes) { super(0,0,(java.lang.String)null); throw new RuntimeException("Stub!"); }
public static final int TYPE_ALREADY_REGISTERED_BY_ANOTHER_ACCOUNT = 1;
public static final int TYPE_REMOVE_RIGHTS = 2;
public static final int TYPE_RIGHTS_INSTALLED = 3;
public static final int TYPE_WAIT_FOR_RIGHTS = 4;
public static final int TYPE_ACCOUNT_ALREADY_REGISTERED = 5;
public static final int TYPE_RIGHTS_REMOVED = 6;
}
