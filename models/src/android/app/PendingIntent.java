class PendingIntent
{
	static android.app.StampPendingIntent pi;

	public static  android.app.PendingIntent getActivity(android.content.Context context, int requestCode, android.content.Intent intent, int flags) 
	{ 
		pi = new android.app.StampActivityPendingIntent();
		pi.intent = intent;
		throw new RuntimeException("Stub!"); 
	}

	public static  android.app.PendingIntent getActivity(android.content.Context context, int requestCode, android.content.Intent intent, int flags, android.os.Bundle options) 
	{ 
		throw new RuntimeException("Stub!"); 
	}

	public static  android.app.PendingIntent getActivities(android.content.Context context, int requestCode, android.content.Intent[] intents, int flags) 
	{ 
		throw new RuntimeException("Stub!"); 
	}

	public static  android.app.PendingIntent getActivities(android.content.Context context, int requestCode, android.content.Intent[] intents, int flags, android.os.Bundle options) 
	{ 
		throw new RuntimeException("Stub!"); 
	
	}

	public static  android.app.PendingIntent getBroadcast(android.content.Context context, int requestCode, android.content.Intent intent, int flags) 
	{ 
		throw new RuntimeException("Stub!"); 
	}

	public static  android.app.PendingIntent getService(android.content.Context context, int requestCode, android.content.Intent intent, int flags) 
	{ 
		throw new RuntimeException("Stub!"); 
	}

	public  void send(android.content.Context context, int code, android.content.Intent intent) throws android.app.PendingIntent.CanceledException 
	{ 
		throw new RuntimeException("Stub!"); 
	}

}
