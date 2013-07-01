package android.app;
public class Notification
  implements android.os.Parcelable
{
public static class Builder
{
public  Builder(android.content.Context context) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setWhen(long when) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setUsesChronometer(boolean b) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setSmallIcon(int icon) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setSmallIcon(int icon, int level) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setContentTitle(java.lang.CharSequence title) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setContentText(java.lang.CharSequence text) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setSubText(java.lang.CharSequence text) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setNumber(int number) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setContentInfo(java.lang.CharSequence info) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setProgress(int max, int progress, boolean indeterminate) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setContent(android.widget.RemoteViews views) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setContentIntent(android.app.PendingIntent intent) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setDeleteIntent(android.app.PendingIntent intent) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setFullScreenIntent(android.app.PendingIntent intent, boolean highPriority) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setTicker(java.lang.CharSequence tickerText) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setTicker(java.lang.CharSequence tickerText, android.widget.RemoteViews views) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setLargeIcon(android.graphics.Bitmap icon) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setSound(android.net.Uri sound) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setSound(android.net.Uri sound, int streamType) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setVibrate(long[] pattern) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setLights(int argb, int onMs, int offMs) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setOngoing(boolean ongoing) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setOnlyAlertOnce(boolean onlyAlertOnce) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setAutoCancel(boolean autoCancel) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setDefaults(int defaults) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setPriority(int pri) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder addAction(int icon, java.lang.CharSequence title, android.app.PendingIntent intent) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.Builder setStyle(android.app.Notification.Style style) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  android.app.Notification getNotification() { throw new RuntimeException("Stub!"); }
public  android.app.Notification build() { throw new RuntimeException("Stub!"); }
}
public abstract static class Style
{
public  Style() { throw new RuntimeException("Stub!"); }
protected  void internalSetBigContentTitle(java.lang.CharSequence title) { throw new RuntimeException("Stub!"); }
protected  void internalSetSummaryText(java.lang.CharSequence cs) { throw new RuntimeException("Stub!"); }
public  void setBuilder(android.app.Notification.Builder builder) { throw new RuntimeException("Stub!"); }
protected  void checkBuilder() { throw new RuntimeException("Stub!"); }
protected  android.widget.RemoteViews getStandardView(int layoutId) { throw new RuntimeException("Stub!"); }
public abstract  android.app.Notification build();
protected android.app.Notification.Builder mBuilder;
}
public static class BigPictureStyle
  extends android.app.Notification.Style
{
public  BigPictureStyle() { throw new RuntimeException("Stub!"); }
public  BigPictureStyle(android.app.Notification.Builder builder) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.BigPictureStyle setBigContentTitle(java.lang.CharSequence title) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.BigPictureStyle setSummaryText(java.lang.CharSequence cs) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.BigPictureStyle bigPicture(android.graphics.Bitmap b) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.BigPictureStyle bigLargeIcon(android.graphics.Bitmap b) { throw new RuntimeException("Stub!"); }
public  android.app.Notification build() { throw new RuntimeException("Stub!"); }
}
public static class BigTextStyle
  extends android.app.Notification.Style
{
public  BigTextStyle() { throw new RuntimeException("Stub!"); }
public  BigTextStyle(android.app.Notification.Builder builder) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.BigTextStyle setBigContentTitle(java.lang.CharSequence title) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.BigTextStyle setSummaryText(java.lang.CharSequence cs) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.BigTextStyle bigText(java.lang.CharSequence cs) { throw new RuntimeException("Stub!"); }
public  android.app.Notification build() { throw new RuntimeException("Stub!"); }
}
public static class InboxStyle
  extends android.app.Notification.Style
{
public  InboxStyle() { throw new RuntimeException("Stub!"); }
public  InboxStyle(android.app.Notification.Builder builder) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.InboxStyle setBigContentTitle(java.lang.CharSequence title) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.InboxStyle setSummaryText(java.lang.CharSequence cs) { throw new RuntimeException("Stub!"); }
public  android.app.Notification.InboxStyle addLine(java.lang.CharSequence cs) { throw new RuntimeException("Stub!"); }
public  android.app.Notification build() { throw new RuntimeException("Stub!"); }
}
public  Notification() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  Notification(int icon, java.lang.CharSequence tickerText, long when) { throw new RuntimeException("Stub!"); }
public  Notification(android.os.Parcel parcel) { throw new RuntimeException("Stub!"); }
public  android.app.Notification clone() { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel parcel, int flags) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  void setLatestEventInfo(android.content.Context context, java.lang.CharSequence contentTitle, java.lang.CharSequence contentText, android.app.PendingIntent contentIntent) { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public static final int DEFAULT_ALL = -1;
public static final int DEFAULT_SOUND = 1;
public static final int DEFAULT_VIBRATE = 2;
public static final int DEFAULT_LIGHTS = 4;
public long when;
public int icon;
public int iconLevel;
public int number;
public android.app.PendingIntent contentIntent;
public android.app.PendingIntent deleteIntent;
public android.app.PendingIntent fullScreenIntent;
public java.lang.CharSequence tickerText;
public android.widget.RemoteViews tickerView;
public android.widget.RemoteViews contentView;
public android.widget.RemoteViews bigContentView;
public android.graphics.Bitmap largeIcon;
public android.net.Uri sound;
public static final int STREAM_DEFAULT = -1;
public int audioStreamType;
public long[] vibrate = null;
public int ledARGB;
public int ledOnMS;
public int ledOffMS;
public int defaults;
public static final int FLAG_SHOW_LIGHTS = 1;
public static final int FLAG_ONGOING_EVENT = 2;
public static final int FLAG_INSISTENT = 4;
public static final int FLAG_ONLY_ALERT_ONCE = 8;
public static final int FLAG_AUTO_CANCEL = 16;
public static final int FLAG_NO_CLEAR = 32;
public static final int FLAG_FOREGROUND_SERVICE = 64;
@Deprecated
public static final int FLAG_HIGH_PRIORITY = 128;
public int flags;
public static final int PRIORITY_DEFAULT = 0;
public static final int PRIORITY_LOW = -1;
public static final int PRIORITY_MIN = -2;
public static final int PRIORITY_HIGH = 1;
public static final int PRIORITY_MAX = 2;
public int priority;
public static final android.os.Parcelable.Creator<android.app.Notification> CREATOR;
static { CREATOR = null; }
}
