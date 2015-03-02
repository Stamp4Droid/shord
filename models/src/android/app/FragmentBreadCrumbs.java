class FragmentBreadCrumbs
{
	public  FragmentBreadCrumbs(android.content.Context context) { 
		super(context, (android.util.AttributeSet)null, 0); 
		
		this.onBackStackChanged();
	}

	public  void setOnBreadCrumbClickListener(android.app.FragmentBreadCrumbs.OnBreadCrumbClickListener listener) 
	{ 
		listener.onBreadCrumbClick(null, 0);
	}
}