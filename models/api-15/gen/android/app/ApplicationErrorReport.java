package android.app;
public class ApplicationErrorReport
  implements android.os.Parcelable
{
public static class CrashInfo
{
public  CrashInfo() { throw new RuntimeException("Stub!"); }
public  CrashInfo(java.lang.Throwable tr) { throw new RuntimeException("Stub!"); }
public  CrashInfo(android.os.Parcel in) { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public  void dump(android.util.Printer pw, java.lang.String prefix) { throw new RuntimeException("Stub!"); }
public java.lang.String exceptionClassName;
public java.lang.String exceptionMessage;
public java.lang.String throwFileName;
public java.lang.String throwClassName;
public java.lang.String throwMethodName;
public int throwLineNumber;
public java.lang.String stackTrace;
}
public static class AnrInfo
{
public  AnrInfo() { throw new RuntimeException("Stub!"); }
public  AnrInfo(android.os.Parcel in) { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public  void dump(android.util.Printer pw, java.lang.String prefix) { throw new RuntimeException("Stub!"); }
public java.lang.String activity;
public java.lang.String cause;
public java.lang.String info;
}
public static class BatteryInfo
{
public  BatteryInfo() { throw new RuntimeException("Stub!"); }
public  BatteryInfo(android.os.Parcel in) { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public  void dump(android.util.Printer pw, java.lang.String prefix) { throw new RuntimeException("Stub!"); }
public int usagePercent;
public long durationMicros;
public java.lang.String usageDetails;
public java.lang.String checkinDetails;
}
public static class RunningServiceInfo
{
public  RunningServiceInfo() { throw new RuntimeException("Stub!"); }
public  RunningServiceInfo(android.os.Parcel in) { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public  void dump(android.util.Printer pw, java.lang.String prefix) { throw new RuntimeException("Stub!"); }
public long durationMillis;
public java.lang.String serviceDetails;
}
public  ApplicationErrorReport() { throw new RuntimeException("Stub!"); }
public static  android.content.ComponentName getErrorReportReceiver(android.content.Context context, java.lang.String packageName, int appFlags) { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public  void readFromParcel(android.os.Parcel in) { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public  void dump(android.util.Printer pw, java.lang.String prefix) { throw new RuntimeException("Stub!"); }
public static final int TYPE_NONE = 0;
public static final int TYPE_CRASH = 1;
public static final int TYPE_ANR = 2;
public static final int TYPE_BATTERY = 3;
public static final int TYPE_RUNNING_SERVICE = 5;
public int type;
public java.lang.String packageName;
public java.lang.String installerPackageName;
public java.lang.String processName;
public long time;
public boolean systemApp;
public android.app.ApplicationErrorReport.CrashInfo crashInfo;
public android.app.ApplicationErrorReport.AnrInfo anrInfo;
public android.app.ApplicationErrorReport.BatteryInfo batteryInfo;
public android.app.ApplicationErrorReport.RunningServiceInfo runningServiceInfo;
public static final android.os.Parcelable.Creator<android.app.ApplicationErrorReport> CREATOR;
static { CREATOR = null; }
}
