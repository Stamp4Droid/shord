class Application
{
	public Application()
	{
		super(null);
		this.onCreate();
		this.onLowMemory();
		this.onTerminate();
		this.onTrimMemory(0);
	}
}