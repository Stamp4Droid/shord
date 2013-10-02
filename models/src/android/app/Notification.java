class Notification
{

	public  void setLatestEventInfo(android.content.Context context, java.lang.CharSequence contentTitle, java.lang.CharSequence contentText, android.app.PendingIntent contentIntent) 
	{ 
		android.app.StampPendingIntent t = contentIntent.pi;
		android.content.Intent i0 = ((android.app.StampActivityPendingIntent) t).intent;
		context.startActivity(i0);
	}
}
