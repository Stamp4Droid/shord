class LayoutInflater
{
	public android.content.Context context;

	protected  LayoutInflater(android.content.Context context) { 
		this.context = context;
	}

	public static  android.view.LayoutInflater from(android.content.Context context) { 
		return new StampLayoutInflater(context);
	}
	
	public  android.view.View inflate(int resource, android.view.ViewGroup root) { 
		if(root == null)
			return new android.view.View(context);
		else
			return root;
	}

	public  android.view.View inflate(int resource, android.view.ViewGroup root, boolean attachToRoot) { 
		if(root != null && attachToRoot)
			return root;
		else
			return new android.view.View(context);
	}

	public  android.content.Context getContext() { throw new RuntimeException("Stub!"); }

}