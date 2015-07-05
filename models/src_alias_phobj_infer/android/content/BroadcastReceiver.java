import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

class BroadcastReceiver
{
	public  BroadcastReceiver() 
	{ 
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){

					android.content.Context context = new android.test.mock.MockContext();

					public void run() {
						BroadcastReceiver.this.onReceive(context, new android.content.Intent());
					}
				}); 
	}
}