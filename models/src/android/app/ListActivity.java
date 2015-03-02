import edu.stanford.stamp.annotation.Inline;

public class ListActivity
{
	@Inline
    public  ListActivity()
	{
		this.onListItemClick(null, null, 0, 0l);
		this.onRestoreInstanceState(null);
		this.onDestroy();
		this.onContentChanged();
	}

}
