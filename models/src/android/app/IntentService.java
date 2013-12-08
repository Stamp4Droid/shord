import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

class IntentService
{
	public  IntentService(java.lang.String name) 
	{ 
		super();

	    IntentService.this.onHandleIntent(new android.content.Intent());
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						IntentService.this.onHandleIntent(new android.content.Intent());
					}
				}); 
		
	}

}
