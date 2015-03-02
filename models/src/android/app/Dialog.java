import edu.stanford.stamp.annotation.Inline;

class Dialog
{
	@Inline
	protected  Dialog(android.content.Context context, boolean cancelable, final android.content.DialogInterface.OnCancelListener cancelListener) {
		cancelListener.onCancel(Dialog.this);
	}

	@Inline
	public  void setOnCancelListener(final android.content.DialogInterface.OnCancelListener listener) {
		listener.onCancel(Dialog.this);
	}

	@Inline
	public  void setOnDismissListener(final android.content.DialogInterface.OnDismissListener listener) {
		listener.onDismiss(Dialog.this);
	}

	@Inline
	public  void setOnShowListener(final android.content.DialogInterface.OnShowListener listener) {
		listener.onShow(Dialog.this);
	}

	@Inline
	public  void setOnKeyListener(final android.content.DialogInterface.OnKeyListener onKeyListener) {
		onKeyListener.onKey(Dialog.this, 0, null);
	}

}
