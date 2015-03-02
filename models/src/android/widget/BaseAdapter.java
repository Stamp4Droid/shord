import edu.stanford.stamp.annotation.Inline;

class BaseAdapter
{
	@Inline
	public  BaseAdapter()
	{
		this.getCount();
		this.getView(0, null, null);
		this.getItem(0);
		this.getItemId(0);
		this.getItemViewType(0);
		this.getViewTypeCount();
	}
}
