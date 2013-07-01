package android.accessibilityservice;
public class AccessibilityServiceInfo
  implements android.os.Parcelable
{
public  AccessibilityServiceInfo() { throw new RuntimeException("Stub!"); }
public  java.lang.String getId() { throw new RuntimeException("Stub!"); }
public  android.content.pm.ResolveInfo getResolveInfo() { throw new RuntimeException("Stub!"); }
public  java.lang.String getSettingsActivityName() { throw new RuntimeException("Stub!"); }
public  boolean getCanRetrieveWindowContent() { throw new RuntimeException("Stub!"); }
@Deprecated
public  java.lang.String getDescription() { throw new RuntimeException("Stub!"); }
public  java.lang.String loadDescription(android.content.pm.PackageManager packageManager) { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel parcel, int flagz) { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public static  java.lang.String feedbackTypeToString(int feedbackType) { throw new RuntimeException("Stub!"); }
public static  java.lang.String flagToString(int flag) { throw new RuntimeException("Stub!"); }
public static final int FEEDBACK_SPOKEN = 1;
public static final int FEEDBACK_HAPTIC = 2;
public static final int FEEDBACK_AUDIBLE = 4;
public static final int FEEDBACK_VISUAL = 8;
public static final int FEEDBACK_GENERIC = 16;
public static final int FEEDBACK_ALL_MASK = -1;
public static final int DEFAULT = 1;
public static final int FLAG_INCLUDE_NOT_IMPORTANT_VIEWS = 2;
public static final int FLAG_REQUEST_TOUCH_EXPLORATION_MODE = 4;
public int eventTypes;
public java.lang.String[] packageNames = null;
public int feedbackType;
public long notificationTimeout;
public int flags;
public static final android.os.Parcelable.Creator<android.accessibilityservice.AccessibilityServiceInfo> CREATOR;
static { CREATOR = null; }
}
