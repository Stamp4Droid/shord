class Thread
{
    private Runnable r;

    public  Thread() 
    { 
		this.r = this;
    }

    public  Thread(java.lang.Runnable runnable) 
    { 
		this.r = runnable;
    }

    public synchronized  void start() 
    { 
		r.run();
    }
}
