class ContextWrapper
{

    private android.content.ContentResolver contentResolver = new android.test.mock.MockContentResolver();

    public  android.content.ContentResolver getContentResolver() 
    { 
		return contentResolver;
    }
	
	public  android.content.Intent registerReceiver(android.content.BroadcastReceiver receiver, android.content.IntentFilter filter) { 
		final BroadcastReceiver r = receiver;
		edu.stanford.stamp.harness.ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						r.onReceive(ContextWrapper.this, new Intent());
					}
				}); 
		return new Intent();
	}

	public  android.content.Intent registerReceiver(android.content.BroadcastReceiver receiver, android.content.IntentFilter filter, java.lang.String broadcastPermission, android.os.Handler scheduler) 
	{ 
		final BroadcastReceiver r = receiver;
		edu.stanford.stamp.harness.ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						r.onReceive(ContextWrapper.this, new Intent());
					}
				}); 
		return new Intent();
	}

	public  android.content.Context getApplicationContext() 
	{ 
		return this;
	}
}
