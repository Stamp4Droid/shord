import edu.stanford.stamp.annotation.Inline;

class Thread
{
    private Runnable r;

	@Inline
    public  Thread()
    {
		this.run();
    }

	@Inline
    public  Thread(java.lang.Runnable runnable)
    {
		runnable.run();
    }
}
