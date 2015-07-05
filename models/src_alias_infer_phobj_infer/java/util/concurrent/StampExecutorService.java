package java.util.concurrent;

class StampExecutorService extends java.util.concurrent.AbstractExecutorService {
    public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) { return true; }

    public <T> java.util.List<Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks) {
	for(java.util.concurrent.Callable<T> c : tasks) {
	    try {
		c.call();
	    } catch(Exception e) {}
	}
	return null;
    }

    public <T> java.util.List<Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks, long timeout, java.util.concurrent.TimeUnit unit) {
	for(java.util.concurrent.Callable<T> c : tasks) {
	    try {
		c.call();
	    } catch(Exception e) {}
	}
	return null;
    }

    public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks) {
	for(java.util.concurrent.Callable<T> c : tasks) {
	    try {
		return c.call();
	    } catch(Exception e) {}
	}
	return null;
    }

    public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks, long timeout, java.util.concurrent.TimeUnit unit) {
	for(java.util.concurrent.Callable<T> c : tasks) {
	    try {
		return c.call();
	    } catch(Exception e) {}
	}
	return null;
    }
    
    public boolean isShutdown() { return false; }

    public boolean isTerminated() { return false; }

    public void shutdown() {}

    /*
    public java.util.List<java.lang.Runnable> shutdownNow() {
	return new java.util.ArrayList<java.lang.Runnable>();
    }
    */

    public void execute(java.lang.Runnable command) {
	command.run();
    }
}
