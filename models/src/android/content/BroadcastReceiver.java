import edu.stanford.stamp.annotation.Inline;

class BroadcastReceiver
{
	@Inline
	public  BroadcastReceiver() 
	{ 
		android.content.Context context = new android.test.mock.MockContext();
		BroadcastReceiver.this.onReceive(context, new android.content.Intent());
	}
}