package android.appwidget;
public class AppWidgetProviderInfo
  implements android.os.Parcelable
{
public  AppWidgetProviderInfo() { throw new RuntimeException("Stub!"); }
public  AppWidgetProviderInfo(android.os.Parcel in) { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel out, int flags) { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public static final int RESIZE_NONE = 0;
public static final int RESIZE_HORIZONTAL = 1;
public static final int RESIZE_VERTICAL = 2;
public static final int RESIZE_BOTH = 3;
public android.content.ComponentName provider;
public int minWidth;
public int minHeight;
public int minResizeWidth;
public int minResizeHeight;
public int updatePeriodMillis;
public int initialLayout;
public android.content.ComponentName configure;
public java.lang.String label;
public int icon;
public int autoAdvanceViewId;
public int previewImage;
public int resizeMode;
public static final android.os.Parcelable.Creator<android.appwidget.AppWidgetProviderInfo> CREATOR;
static { CREATOR = null; }
}
