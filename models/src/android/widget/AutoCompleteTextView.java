import edu.stanford.stamp.annotation.Inline;

class AutoCompleteTextView
{
	@Inline
	public  void setOnClickListener(android.view.View.OnClickListener listener) 
	{ 
		listener.onClick(this);
	}
}