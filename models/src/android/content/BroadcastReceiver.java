import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

class BroadcastReceiver
{
	public  BroadcastReceiver() 
	{ 

		BroadcastReceiver.this.onReceive(null, new android.content.Intent());
		/*ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						BroadcastReceiver.this.onReceive(null, new android.content.Intent());
					}
				}); */
	}
}
