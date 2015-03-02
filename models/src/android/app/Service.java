import edu.stanford.stamp.annotation.Inline;

class Service
{
	@Inline
	public  Service()
	{
		super((android.content.Context)null);

		this.onCreate();
		this.onStart(new android.content.Intent(), 0);
		this.onStartCommand(new android.content.Intent(), 0, 0);
		this.onDestroy();
		this.onConfigurationChanged(null);
		this.onLowMemory();
		this.onUnbind(new android.content.Intent());
		this.onRebind(new android.content.Intent());
		this.onBind(new android.content.Intent());
	}
}
