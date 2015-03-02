import edu.stanford.stamp.annotation.Inline;

class Application
{
	@Inline
	public Application()
	{
		super(null);
		this.onCreate();
		this.onLowMemory();
		this.onTerminate();
		this.onTrimMemory(0);
	}
}
