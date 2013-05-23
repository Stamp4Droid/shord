import android.telephony.TelephonyManager;
import android.location.LocationManager;

import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;
 
public class Activity
{
    public  Activity() 
	{
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onCreate(null);
					}
				}); 

		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onStart();
					}
				}); 

		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onRestart();
					}
				}); 		

		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onPause();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onResume();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onPostResume();
					}
				});

		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onStop();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onDestroy();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onRestoreInstanceState(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onPostCreate(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onNewIntent(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onSaveInstanceState(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onUserLeaveHint();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onCreateThumbnail(null, null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onCreateDescription();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onConfigurationChanged(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onRetainNonConfigurationInstance();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onLowMemory();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onKeyDown(0, null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onKeyLongPress(0, null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onKeyUp(0, null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onKeyMultiple(0, 0, null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onBackPressed();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onTouchEvent(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onTrackballEvent(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onUserInteraction();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onWindowAttributesChanged(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onContentChanged();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onWindowFocusChanged(false);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onAttachedToWindow();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onDetachedFromWindow();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onCreatePanelMenu(0, null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onPreparePanel(0, null, null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onMenuOpened(0, null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onMenuItemSelected(0, null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onPanelClosed(0, null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onCreateOptionsMenu(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onPrepareOptionsMenu(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onOptionsItemSelected(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onOptionsMenuClosed(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onCreateContextMenu(null, null, null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.dispatchKeyEvent(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.dispatchKeyShortcutEvent(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.dispatchTouchEvent(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.dispatchTrackballEvent(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.dispatchGenericMotionEvent(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.dispatchPopulateAccessibilityEvent(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onCreatePanelView(0);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onSearchRequested();
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onWindowStartingActionMode(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onActionModeStarted(null);
					}
				});
		
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onActionModeFinished(null);
					}
				});
	}



    public final  android.database.Cursor managedQuery(android.net.Uri uri, java.lang.String[] projection, java.lang.String selection, java.lang.String[] selectionArgs, java.lang.String sortOrder) 
    { 
		return getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

	public  android.view.View findViewById(int id) 
	{ 
		return edu.stanford.stamp.harness.ViewFactory.findViewById(null, id);
	}


	public  java.lang.Object getSystemService(java.lang.String name) 
	{ 
		if(name.equals(TELEPHONY_SERVICE))
			return TelephonyManager.getInstance();
		else if(name.equals(LOCATION_SERVICE))
			return LocationManager.getInstance();
		else
			return null;//TODO
	}
	
	public  void startActivityForResult(android.content.Intent intent, int requestCode) 
	{ 
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onActivityResult(0, 0, new android.content.Intent());
					}
				});
	}

	public  void startActivityForResult(android.content.Intent intent, int requestCode, android.os.Bundle options) 
	{ 
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						Activity.this.onActivityResult(0, 0, new android.content.Intent());
					}
				});
	}
	
	public final void showDialog (int id)
	{
	    this.onCreateDialog(id);
	}

}
