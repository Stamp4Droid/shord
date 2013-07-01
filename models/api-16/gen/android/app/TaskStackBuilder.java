package android.app;
public class TaskStackBuilder
{
TaskStackBuilder() { throw new RuntimeException("Stub!"); }
public static  android.app.TaskStackBuilder create(android.content.Context context) { throw new RuntimeException("Stub!"); }
public  android.app.TaskStackBuilder addNextIntent(android.content.Intent nextIntent) { throw new RuntimeException("Stub!"); }
public  android.app.TaskStackBuilder addNextIntentWithParentStack(android.content.Intent nextIntent) { throw new RuntimeException("Stub!"); }
public  android.app.TaskStackBuilder addParentStack(android.app.Activity sourceActivity) { throw new RuntimeException("Stub!"); }
public  android.app.TaskStackBuilder addParentStack(java.lang.Class<?> sourceActivityClass) { throw new RuntimeException("Stub!"); }
public  android.app.TaskStackBuilder addParentStack(android.content.ComponentName sourceActivityName) { throw new RuntimeException("Stub!"); }
public  int getIntentCount() { throw new RuntimeException("Stub!"); }
public  android.content.Intent editIntentAt(int index) { throw new RuntimeException("Stub!"); }
public  void startActivities() { throw new RuntimeException("Stub!"); }
public  void startActivities(android.os.Bundle options) { throw new RuntimeException("Stub!"); }
public  android.app.PendingIntent getPendingIntent(int requestCode, int flags) { throw new RuntimeException("Stub!"); }
public  android.app.PendingIntent getPendingIntent(int requestCode, int flags, android.os.Bundle options) { throw new RuntimeException("Stub!"); }
public  android.content.Intent[] getIntents() { throw new RuntimeException("Stub!"); }
}
