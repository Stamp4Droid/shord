import edu.stanford.stamp.annotation.Inline;

class FragmentBreadCrumbs
{
	@Inline
	public  FragmentBreadCrumbs(android.content.Context context) { 
		super(context, (android.util.AttributeSet)null, 0); 
		this.onBackStackChanged();
	}

	@Inline
	public  void setOnBreadCrumbClickListener(android.app.FragmentBreadCrumbs.OnBreadCrumbClickListener listener) 
	{ 
		listener.onBreadCrumbClick(null, 0);
	}
}