import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;
 
public class ListActivity
{
    public  ListActivity() 
	{
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
/*						Activity.this.onCreate(null);
						Activity.this.onStart();
						Activity.this.onRestart();
						Activity.this.onPause();
						Activity.this.onResume();
						Activity.this.onPostResume();
						Activity.this.onStop();
						Activity.this.onDestroy();
						Activity.this.onRestoreInstanceState(null);
						Activity.this.onPostCreate(null);
						Activity.this.onNewIntent(null);
						Activity.this.onSaveInstanceState(null);
						Activity.this.onUserLeaveHint();
						Activity.this.onCreateThumbnail(null, null);
						Activity.this.onCreateDescription();
						Activity.this.onRetainNonConfigurationInstance();
						Activity.this.onConfigurationChanged(null);
						Activity.this.onLowMemory();
						Activity.this.onKeyDown(0, null);
						Activity.this.onKeyLongPress(0, null);
						Activity.this.onKeyUp(0, null);
						Activity.this.onKeyMultiple(0, 0, null);
						Activity.this.onBackPressed();
						Activity.this.onTouchEvent(null);
						Activity.this.onTrackballEvent(null);
						Activity.this.onUserInteraction();
						Activity.this.onWindowAttributesChanged(null);
						Activity.this.onContentChanged();
						Activity.this.onWindowFocusChanged(false);
						Activity.this.onAttachedToWindow();
						Activity.this.onDetachedFromWindow();
						Activity.this.onCreatePanelMenu(0, null);
						Activity.this.onPreparePanel(0, null, null);
						Activity.this.onMenuOpened(0, null);
						Activity.this.onMenuItemSelected(0, null);
						Activity.this.onPanelClosed(0, null);
						Activity.this.onCreateOptionsMenu(null);
						Activity.this.onPrepareOptionsMenu(null);
						Activity.this.onOptionsItemSelected(null);
						Activity.this.onOptionsMenuClosed(null);
						Activity.this.onCreateContextMenu(null, null, null);
						Activity.this.dispatchKeyEvent(null);
						Activity.this.dispatchKeyShortcutEvent(null);
						Activity.this.dispatchTouchEvent(null);
						Activity.this.dispatchTrackballEvent(null);
						Activity.this.dispatchGenericMotionEvent(null);
						Activity.this.dispatchPopulateAccessibilityEvent(null);
						Activity.this.onCreatePanelView(0);
						Activity.this.onSearchRequested();
						Activity.this.onWindowStartingActionMode(null);
						Activity.this.onActionModeStarted(null);
						Activity.this.onActionModeFinished(null);*/

                        ListActivity.this.onListItemClick(null, null, 0, 0l);
                        ListActivity.this.onRestoreInstanceState(null);
                        ListActivity.this.onDestroy(); 
                        ListActivity.this.onContentChanged(); 

					}
				});
	}

}
