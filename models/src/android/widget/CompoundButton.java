import edu.stanford.stamp.annotation.Inline;

class CompoundButton
{
	@Inline
	public void setOnCheckedChangeListener(final android.widget.CompoundButton.OnCheckedChangeListener l) 
	{ 
		l.onCheckedChanged(CompoundButton.this, false);
	}
}
