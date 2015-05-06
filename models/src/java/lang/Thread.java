import edu.stanford.stamp.annotation.Inline;

class Thread
{
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

	@Inline
	public  Thread(java.lang.Runnable runnable, java.lang.String threadName)
	{ 
		runnable.run();
	}

	@Inline
	public  Thread(java.lang.String threadName) 
	{
		this.run();
	}

	@Inline
	public  Thread(java.lang.ThreadGroup group, java.lang.Runnable runnable)
	{ 
		runnable.run();
	}

	@Inline
	public  Thread(java.lang.ThreadGroup group, java.lang.Runnable runnable, java.lang.String threadName) 
	{ 
		runnable.run();
	}

	@Inline
	public  Thread(java.lang.ThreadGroup group, java.lang.String threadName) { 
		this.run();
	}

	@Inline
	public  Thread(java.lang.ThreadGroup group, java.lang.Runnable runnable, java.lang.String threadName, long stackSize) 
	{ 
		runnable.run();
	}

    public synchronized  void start() 
    { 
    }
}
