package android.view.textservice;
public final class SpellCheckerInfo
  implements android.os.Parcelable
{
SpellCheckerInfo() { throw new RuntimeException("Stub!"); }
public  java.lang.String getId() { throw new RuntimeException("Stub!"); }
public  android.content.ComponentName getComponent() { throw new RuntimeException("Stub!"); }
public  java.lang.String getPackageName() { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public  java.lang.CharSequence loadLabel(android.content.pm.PackageManager pm) { throw new RuntimeException("Stub!"); }
public  android.graphics.drawable.Drawable loadIcon(android.content.pm.PackageManager pm) { throw new RuntimeException("Stub!"); }
public  android.content.pm.ServiceInfo getServiceInfo() { throw new RuntimeException("Stub!"); }
public  java.lang.String getSettingsActivity() { throw new RuntimeException("Stub!"); }
public  int getSubtypeCount() { throw new RuntimeException("Stub!"); }
public  android.view.textservice.SpellCheckerSubtype getSubtypeAt(int index) { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public static final android.os.Parcelable.Creator<android.view.textservice.SpellCheckerInfo> CREATOR;
static { CREATOR = null; }
}
