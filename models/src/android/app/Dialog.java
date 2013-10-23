import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

class Dialog
{
	protected  Dialog(android.content.Context context, boolean cancelable, final android.content.DialogInterface.OnCancelListener cancelListener) { 
		cancelListener.onCancel(Dialog.this);
		/*ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						cancelListener.onCancel(Dialog.this);
					}
				});*/
		
	}

	public  void setOnCancelListener(final android.content.DialogInterface.OnCancelListener listener) { 
		listener.onCancel(Dialog.this);
		/*ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						listener.onCancel(Dialog.this);
					}
				});*/
	}

	public  void setOnDismissListener(final android.content.DialogInterface.OnDismissListener listener) { 
		listener.onDismiss(Dialog.this);
		/*ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						listener.onDismiss(Dialog.this);
					}
				});*/
		
	}

	public  void setOnShowListener(final android.content.DialogInterface.OnShowListener listener) { 
		listener.onShow(Dialog.this);
		/*ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						listener.onShow(Dialog.this);
					}
				});*/
	}

	public  void setOnKeyListener(final android.content.DialogInterface.OnKeyListener onKeyListener) {
		onKeyListener.onKey(Dialog.this, 0, null);
		/*ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						onKeyListener.onKey(Dialog.this, 0, null);
					}
				});*/
	}

}
