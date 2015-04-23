class Dialog
{
	public android.view.StampLayoutInflater stamp_inflater;

	protected  Dialog(android.content.Context context, boolean cancelable, final android.content.DialogInterface.OnCancelListener cancelListener) { 
		cancelListener.onCancel(Dialog.this);
	}

	public  void setOnCancelListener(final android.content.DialogInterface.OnCancelListener listener) { 
		listener.onCancel(Dialog.this);
	}

	public  void setOnDismissListener(final android.content.DialogInterface.OnDismissListener listener) { 
		listener.onDismiss(Dialog.this);
	}

	public  void setOnShowListener(final android.content.DialogInterface.OnShowListener listener) { 
		listener.onShow(Dialog.this);
	}

	public  void setOnKeyListener(final android.content.DialogInterface.OnKeyListener onKeyListener) {
		onKeyListener.onKey(Dialog.this, 0, null);
	}

	public  void setContentView(int layoutResID) { 
		//dont make it a stub
	}

	public  void setContentView(android.view.View view) { 
		this.stamp_inflater = view.stamp_inflater;
	}

	public  void setContentView(android.view.View view, android.view.ViewGroup.LayoutParams params) { 
		this.stamp_inflater = view.stamp_inflater;
	}

	public  void addContentView(android.view.View view, android.view.ViewGroup.LayoutParams params) { 
		this.stamp_inflater = view.stamp_inflater;
	}

	public final  android.content.Context getContext()
	{
		return this.stamp_inflater.context;
	}
}