import edu.stanford.stamp.annotation.Inline;

class ContextWrapper
{
	protected Context mBase;

	public ContextWrapper(android.content.Context base) {
		mBase = base;
	}

    protected void attachBaseContext(android.content.Context base) {
        mBase = base;
    }

    private android.content.ContentResolver contentResolver = new android.test.mock.MockContentResolver();

    public  android.content.ContentResolver getContentResolver()
    {
		return contentResolver;
    }

	@Inline
	public  android.content.Intent registerReceiver(android.content.BroadcastReceiver receiver, android.content.IntentFilter filter) {
		receiver.onReceive(this, new Intent());
		return new Intent();
	}

	@Inline
	public  android.content.Intent registerReceiver(android.content.BroadcastReceiver receiver, android.content.IntentFilter filter, java.lang.String broadcastPermission, android.os.Handler scheduler)
	{
		receiver.onReceive(this, new Intent());
		return new Intent();
	}

	@STAMP(flows = {@Flow(from="!Service.bind",to="service")})
	@Inline
    public boolean bindService(android.content.Intent service, android.content.ServiceConnection conn, int flags)
	{
		conn.onServiceConnected(null, null);
		conn.onServiceDisconnected(null);
		return true;
    }

	public  android.content.Context getApplicationContext()
	{
		return this;
	}

	@STAMP(flows = {@Flow(from="!Activity",to="intent"),@Flow(from="!Activity",to="options")})
	public void startActivity(android.content.Intent intent, android.os.Bundle options) {
    }

	@STAMP(flows = {@Flow(from="!Activity",to="intent")})
	public void startActivity(android.content.Intent intent) {
    }

	@STAMP(flows = {@Flow(from="!Activity",to="intents"),@Flow(from="!Activity",to="options")})
	public void startActivities(android.content.Intent[] intents, android.os.Bundle options) {
	}

	@STAMP(flows = {@Flow(from="!Activity",to="intents")})
    public void startActivities(android.content.Intent[] intents) {
    }

	@STAMP(flows = {@Flow(from="!Service.start",to="service")})
    public android.content.ComponentName startService(android.content.Intent service) {
		return null;
    }

	@STAMP(flows = {@Flow(from="!Broadcast",to="intent")})
	public void sendBroadcast(android.content.Intent intent) {
    }

	@STAMP(flows = {@Flow(from="!Broadcast",to="intent")})
    public void sendBroadcast(android.content.Intent intent, java.lang.String receiverPermission) {
		throw new RuntimeException("Stub!");
    }

	@STAMP(flows = {@Flow(from="!Broadcast",to="intent")})
    public void sendOrderedBroadcast(android.content.Intent intent, java.lang.String receiverPermission) {
		throw new RuntimeException("Stub!");
    }

	@STAMP(flows = {@Flow(from="!Broadcast",to="intent"),@Flow(from="!Broadcast",to="initialExtras")})
    public void sendOrderedBroadcast(android.content.Intent intent, java.lang.String receiverPermission, android.content.BroadcastReceiver resultReceiver, android.os.Handler scheduler, int initialCode, java.lang.String initialData, android.os.Bundle initialExtras) {
        throw new RuntimeException("Stub!");
    }

	@STAMP(flows = {@Flow(from="!Broadcast",to="intent")})
    public void sendStickyBroadcast(android.content.Intent intent) {
    }

    public android.content.pm.PackageManager getPackageManager() {
		return new android.test.mock.MockPackageManager();
    }

	public  android.content.SharedPreferences getSharedPreferences(java.lang.String name, int mode) {
		return android.content.StampSharedPreferences.INSTANCE;
	}

    public java.io.FileInputStream openFileInput(java.lang.String name) throws java.io.FileNotFoundException {
		return new java.io.FileInputStream(name);
    }

    public java.io.FileOutputStream openFileOutput(java.lang.String name, int mode) throws java.io.FileNotFoundException {
		return new java.io.FileOutputStream(name);
    }

	public  java.lang.Object getSystemService(java.lang.String name)
	{
		return mBase.getSystemService(name);
	}
}
