import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;
 
public class ListActivity
{
    public  ListActivity() 
	{
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
                public void run() {
                    ListActivity.this.onListItemClick(null, null, 0, 0l);
                    ListActivity.this.onRestoreInstanceState(null);
                    ListActivity.this.onDestroy(); 
                    ListActivity.this.onContentChanged(); 
                }
			});
	}

}
