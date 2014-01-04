import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

class BroadcastReceiver
{
	public  BroadcastReceiver() 
	{ 
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){

					android.content.Context context = android.test.mock.MockContext.g;

					public void run() {
						BroadcastReceiver.this.onReceive(context, new android.content.Intent());
					}
				}); 
	}
}