import edu.stanford.stamp.annotation.Inline;

class AbstractExecutorService
{
	@Inline
	public  java.util.concurrent.Future<?> submit(java.lang.Runnable task) {
		task.run();
		return null;
	}

	@Inline
	public <T> java.util.concurrent.Future<T> submit(java.lang.Runnable task, T result) {
		task.run();
		return null;
	}

	@Inline
	public <T> java.util.concurrent.Future<T> submit(java.util.concurrent.Callable<T> task) {
		try{
			task.call();
		}catch(Exception e){}
		return null;
	}

}
