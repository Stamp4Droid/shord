import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

class Service
{
	public  Service() 
	{ 
		super((android.content.Context)null); 
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Service.this.onCreate();
					}
				}); 
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Service.this.onStart(new android.content.Intent(), 0);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Service.this.onStartCommand(new android.content.Intent(), 0, 0);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Service.this.onDestroy();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Service.this.onConfigurationChanged(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Service.this.onLowMemory();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Service.this.onUnbind(new android.content.Intent());
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Service.this.onRebind(new android.content.Intent());
					}
				});

		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Service.this.onBind(new android.content.Intent());
					}
				});
	}		
}
