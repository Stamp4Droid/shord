import edu.stanford.stamp.harness.ApplicationDriver;

class Handler
{
	public final  boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
	    ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						Handler.this.handleMessage(null);
					}
				});
	    return true;
	}

	public final  boolean sendEmptyMessageDelayed(int what, long delayMillis) {
	    ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						Handler.this.handleMessage(null);
					}
				});
	    return true;
	}

	public final boolean sendEmptyMessage(int what) {
	    ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						Handler.this.handleMessage(null);
					}
				});
	    return true;
	}

	public final boolean sendMessage(final android.os.Message msg) {
	    ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						Handler.this.handleMessage(msg);
					}
				});
	    return true;
	}
	
	public final boolean sendMessageDelayed(final android.os.Message msg, final long delayMillis) {
	    ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
					    try {
					        Thread.sleep(delayMillis);
					    } catch(InterruptedException e) { }
						Handler.this.handleMessage(msg);
					}
				});
	    return true;
	}

	public  boolean sendMessageAtTime(final android.os.Message msg, long uptimeMillis) {
		ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						Handler.this.handleMessage(msg);
					}
				});
	    return true;
	}

	public final  boolean sendMessageAtFrontOfQueue(final android.os.Message msg) {
		ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						Handler.this.handleMessage(msg);
					}
				});
	    return true;
	}

	public final  boolean post(final java.lang.Runnable r) {
		ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						r.run();
					}
				});
	    return true;
	}
	
	public final  boolean postAtTime(final java.lang.Runnable r, long uptimeMillis) {
		ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						r.run();
					}
				});
	    return true;
	}


	public final  boolean postAtTime(final java.lang.Runnable r, java.lang.Object token, long uptimeMillis) {
		ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						r.run();
					}
				});
	    return true;
	}


	public final  boolean postDelayed(final java.lang.Runnable r, long delayMillis) {
		ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						r.run();
					}
				});
	    return true;
	}

	public final  boolean postAtFrontOfQueue(final java.lang.Runnable r) {
		ApplicationDriver.getInstance().
			registerCallback(new edu.stanford.stamp.harness.Callback(){
					public void run() {
						r.run();
					}
				});
	    return true;
	}

	public final  android.os.Message obtainMessage(int what, int arg1, int arg2, java.lang.Object obj) {
	    android.os.Message msg = new android.os.Message();
	    msg.what = what;
	    msg.arg1 = arg1;
	    msg.arg2 = arg2;
	    msg.obj = obj;
	    return msg;
	}
	
	public final  android.os.Message obtainMessage(int what, int arg1, int arg2) {
	    return this.obtainMessage(what, arg1, arg2, null);
	}
	
	public final  android.os.Message obtainMessage(int what, java.lang.Object obj) {
	    return this.obtainMessage(what, 0, 0, obj);
	}
	
	public final  android.os.Message obtainMessage(int what) {
	    return this.obtainMessage(what, 0, 0, null);
	}

	public final  android.os.Message obtainMessage() {
		return this.obtainMessage(0, 0, 0, null);
	}
}
