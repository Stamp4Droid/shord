import edu.stanford.stamp.annotation.Inline;

class AbstractThreadedSyncAdapter
{
	@Inline
	public AbstractThreadedSyncAdapter(android.content.Context context, boolean autoInitialize)  
	{
		this.onPerformSync(null, new android.os.Bundle(), new String(), null, null);
		this.onSyncCanceled();
		this.onSyncCanceled(null);
	}
}

