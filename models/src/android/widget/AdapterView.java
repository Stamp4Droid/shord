import edu.stanford.stamp.annotation.Inline;

class AdapterView
{
	@Inline
	public  void setOnItemClickListener(final android.widget.AdapterView.OnItemClickListener listener)
	{
		listener.onItemClick(AdapterView.this, null, 0, 0L);
	}

	@Inline
	public  void setOnItemLongClickListener(final android.widget.AdapterView.OnItemLongClickListener listener)
	{
		listener.onItemLongClick(AdapterView.this, null, 0, 0L);
	}

	@Inline
	public  void setOnItemSelectedListener(final android.widget.AdapterView.OnItemSelectedListener listener)
	{
		listener.onItemSelected(AdapterView.this, null, 0, 0L);
		listener.onNothingSelected(AdapterView.this);
	}
}
