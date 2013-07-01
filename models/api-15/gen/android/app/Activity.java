package android.app;

import android.telephony.TelephonyManager;
import android.location.LocationManager;
import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

public class Activity extends android.view.ContextThemeWrapper implements android.view.LayoutInflater.Factory2, android.view.Window.Callback, android.view.KeyEvent.Callback, android.view.View.OnCreateContextMenuListener, android.content.ComponentCallbacks2 {

    public android.content.Intent getIntent() {
        throw new RuntimeException("Stub!");
    }

    public void setIntent(android.content.Intent newIntent) {
        throw new RuntimeException("Stub!");
    }

    public final android.app.Application getApplication() {
        throw new RuntimeException("Stub!");
    }

    public final boolean isChild() {
        throw new RuntimeException("Stub!");
    }

    public final android.app.Activity getParent() {
        throw new RuntimeException("Stub!");
    }

    public android.view.WindowManager getWindowManager() {
        throw new RuntimeException("Stub!");
    }

    public android.view.Window getWindow() {
        throw new RuntimeException("Stub!");
    }

    public android.app.LoaderManager getLoaderManager() {
        throw new RuntimeException("Stub!");
    }

    public android.view.View getCurrentFocus() {
        throw new RuntimeException("Stub!");
    }

    protected void onCreate(android.os.Bundle savedInstanceState) {
        throw new RuntimeException("Stub!");
    }

    protected void onRestoreInstanceState(android.os.Bundle savedInstanceState) {
        throw new RuntimeException("Stub!");
    }

    protected void onPostCreate(android.os.Bundle savedInstanceState) {
        throw new RuntimeException("Stub!");
    }

    protected void onStart() {
        throw new RuntimeException("Stub!");
    }

    protected void onRestart() {
        throw new RuntimeException("Stub!");
    }

    protected void onResume() {
        throw new RuntimeException("Stub!");
    }

    protected void onPostResume() {
        throw new RuntimeException("Stub!");
    }

    protected void onNewIntent(android.content.Intent intent) {
        throw new RuntimeException("Stub!");
    }

    protected void onSaveInstanceState(android.os.Bundle outState) {
        throw new RuntimeException("Stub!");
    }

    protected void onPause() {
        throw new RuntimeException("Stub!");
    }

    protected void onUserLeaveHint() {
        throw new RuntimeException("Stub!");
    }

    public boolean onCreateThumbnail(android.graphics.Bitmap outBitmap, android.graphics.Canvas canvas) {
        throw new RuntimeException("Stub!");
    }

    public java.lang.CharSequence onCreateDescription() {
        throw new RuntimeException("Stub!");
    }

    protected void onStop() {
        throw new RuntimeException("Stub!");
    }

    protected void onDestroy() {
        throw new RuntimeException("Stub!");
    }

    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        throw new RuntimeException("Stub!");
    }

    public int getChangingConfigurations() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.Object getLastNonConfigurationInstance() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.Object onRetainNonConfigurationInstance() {
        throw new RuntimeException("Stub!");
    }

    public void onLowMemory() {
        throw new RuntimeException("Stub!");
    }

    public void onTrimMemory(int level) {
        throw new RuntimeException("Stub!");
    }

    public android.app.FragmentManager getFragmentManager() {
        throw new RuntimeException("Stub!");
    }

    public void onAttachFragment(android.app.Fragment fragment) {
        throw new RuntimeException("Stub!");
    }

    public void startManagingCursor(android.database.Cursor c) {
        throw new RuntimeException("Stub!");
    }

    public void stopManagingCursor(android.database.Cursor c) {
        throw new RuntimeException("Stub!");
    }

    public android.app.ActionBar getActionBar() {
        throw new RuntimeException("Stub!");
    }

    public void setContentView(int layoutResID) {
        throw new RuntimeException("Stub!");
    }

    public void setContentView(android.view.View view) {
        throw new RuntimeException("Stub!");
    }

    public void setContentView(android.view.View view, android.view.ViewGroup.LayoutParams params) {
        throw new RuntimeException("Stub!");
    }

    public void addContentView(android.view.View view, android.view.ViewGroup.LayoutParams params) {
        throw new RuntimeException("Stub!");
    }

    public void setFinishOnTouchOutside(boolean finish) {
        throw new RuntimeException("Stub!");
    }

    public final void setDefaultKeyMode(int mode) {
        throw new RuntimeException("Stub!");
    }

    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onKeyLongPress(int keyCode, android.view.KeyEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, android.view.KeyEvent event) {
        throw new RuntimeException("Stub!");
    }

    public void onBackPressed() {
        throw new RuntimeException("Stub!");
    }

    public boolean onKeyShortcut(int keyCode, android.view.KeyEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onTouchEvent(android.view.MotionEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onTrackballEvent(android.view.MotionEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean onGenericMotionEvent(android.view.MotionEvent event) {
        throw new RuntimeException("Stub!");
    }

    public void onUserInteraction() {
        throw new RuntimeException("Stub!");
    }

    public void onWindowAttributesChanged(android.view.WindowManager.LayoutParams params) {
        throw new RuntimeException("Stub!");
    }

    public void onContentChanged() {
        throw new RuntimeException("Stub!");
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        throw new RuntimeException("Stub!");
    }

    public void onAttachedToWindow() {
        throw new RuntimeException("Stub!");
    }

    public void onDetachedFromWindow() {
        throw new RuntimeException("Stub!");
    }

    public boolean hasWindowFocus() {
        throw new RuntimeException("Stub!");
    }

    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean dispatchKeyShortcutEvent(android.view.KeyEvent event) {
        throw new RuntimeException("Stub!");
    }

    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        throw new RuntimeException("Stub!");
    }

    public boolean dispatchTrackballEvent(android.view.MotionEvent ev) {
        throw new RuntimeException("Stub!");
    }

    public boolean dispatchGenericMotionEvent(android.view.MotionEvent ev) {
        throw new RuntimeException("Stub!");
    }

    public boolean dispatchPopulateAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) {
        throw new RuntimeException("Stub!");
    }

    public android.view.View onCreatePanelView(int featureId) {
        throw new RuntimeException("Stub!");
    }

    public boolean onCreatePanelMenu(int featureId, android.view.Menu menu) {
        throw new RuntimeException("Stub!");
    }

    public boolean onPreparePanel(int featureId, android.view.View view, android.view.Menu menu) {
        throw new RuntimeException("Stub!");
    }

    public boolean onMenuOpened(int featureId, android.view.Menu menu) {
        throw new RuntimeException("Stub!");
    }

    public boolean onMenuItemSelected(int featureId, android.view.MenuItem item) {
        throw new RuntimeException("Stub!");
    }

    public void onPanelClosed(int featureId, android.view.Menu menu) {
        throw new RuntimeException("Stub!");
    }

    public void invalidateOptionsMenu() {
        throw new RuntimeException("Stub!");
    }

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        throw new RuntimeException("Stub!");
    }

    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        throw new RuntimeException("Stub!");
    }

    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        throw new RuntimeException("Stub!");
    }

    public void onOptionsMenuClosed(android.view.Menu menu) {
        throw new RuntimeException("Stub!");
    }

    public void openOptionsMenu() {
        throw new RuntimeException("Stub!");
    }

    public void closeOptionsMenu() {
        throw new RuntimeException("Stub!");
    }

    public void onCreateContextMenu(android.view.ContextMenu menu, android.view.View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
        throw new RuntimeException("Stub!");
    }

    public void registerForContextMenu(android.view.View view) {
        throw new RuntimeException("Stub!");
    }

    public void unregisterForContextMenu(android.view.View view) {
        throw new RuntimeException("Stub!");
    }

    public void openContextMenu(android.view.View view) {
        throw new RuntimeException("Stub!");
    }

    public void closeContextMenu() {
        throw new RuntimeException("Stub!");
    }

    public boolean onContextItemSelected(android.view.MenuItem item) {
        throw new RuntimeException("Stub!");
    }

    public void onContextMenuClosed(android.view.Menu menu) {
        throw new RuntimeException("Stub!");
    }

    protected android.app.Dialog onCreateDialog(int id) {
        throw new RuntimeException("Stub!");
    }

    protected android.app.Dialog onCreateDialog(int id, android.os.Bundle args) {
        throw new RuntimeException("Stub!");
    }

    protected void onPrepareDialog(int id, android.app.Dialog dialog) {
        throw new RuntimeException("Stub!");
    }

    protected void onPrepareDialog(int id, android.app.Dialog dialog, android.os.Bundle args) {
        throw new RuntimeException("Stub!");
    }

    public final boolean showDialog(int id, android.os.Bundle args) {
        throw new RuntimeException("Stub!");
    }

    public final void dismissDialog(int id) {
        throw new RuntimeException("Stub!");
    }

    public final void removeDialog(int id) {
        throw new RuntimeException("Stub!");
    }

    public boolean onSearchRequested() {
        throw new RuntimeException("Stub!");
    }

    public void startSearch(java.lang.String initialQuery, boolean selectInitialQuery, android.os.Bundle appSearchData, boolean globalSearch) {
        throw new RuntimeException("Stub!");
    }

    public void triggerSearch(java.lang.String query, android.os.Bundle appSearchData) {
        throw new RuntimeException("Stub!");
    }

    public void takeKeyEvents(boolean get) {
        throw new RuntimeException("Stub!");
    }

    public final boolean requestWindowFeature(int featureId) {
        throw new RuntimeException("Stub!");
    }

    public final void setFeatureDrawableResource(int featureId, int resId) {
        throw new RuntimeException("Stub!");
    }

    public final void setFeatureDrawableUri(int featureId, android.net.Uri uri) {
        throw new RuntimeException("Stub!");
    }

    public final void setFeatureDrawable(int featureId, android.graphics.drawable.Drawable drawable) {
        throw new RuntimeException("Stub!");
    }

    public final void setFeatureDrawableAlpha(int featureId, int alpha) {
        throw new RuntimeException("Stub!");
    }

    public android.view.LayoutInflater getLayoutInflater() {
        throw new RuntimeException("Stub!");
    }

    public android.view.MenuInflater getMenuInflater() {
        throw new RuntimeException("Stub!");
    }

    protected void onApplyThemeResource(android.content.res.Resources.Theme theme, int resid, boolean first) {
        throw new RuntimeException("Stub!");
    }

    public void startIntentSenderForResult(android.content.IntentSender intent, int requestCode, android.content.Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws android.content.IntentSender.SendIntentException {
        throw new RuntimeException("Stub!");
    }

    public void startActivity(android.content.Intent intent) {
        throw new RuntimeException("Stub!");
    }

    public void startActivities(android.content.Intent[] intents) {
        throw new RuntimeException("Stub!");
    }

    public void startIntentSender(android.content.IntentSender intent, android.content.Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws android.content.IntentSender.SendIntentException {
        throw new RuntimeException("Stub!");
    }

    public boolean startActivityIfNeeded(android.content.Intent intent, int requestCode) {
        throw new RuntimeException("Stub!");
    }

    public boolean startNextMatchingActivity(android.content.Intent intent) {
        throw new RuntimeException("Stub!");
    }

    public void startActivityFromChild(android.app.Activity child, android.content.Intent intent, int requestCode) {
        throw new RuntimeException("Stub!");
    }

    public void startActivityFromFragment(android.app.Fragment fragment, android.content.Intent intent, int requestCode) {
        throw new RuntimeException("Stub!");
    }

    public void startIntentSenderFromChild(android.app.Activity child, android.content.IntentSender intent, int requestCode, android.content.Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws android.content.IntentSender.SendIntentException {
        throw new RuntimeException("Stub!");
    }

    public void overridePendingTransition(int enterAnim, int exitAnim) {
        throw new RuntimeException("Stub!");
    }

    public final void setResult(int resultCode) {
        throw new RuntimeException("Stub!");
    }

    public final void setResult(int resultCode, android.content.Intent data) {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getCallingPackage() {
        throw new RuntimeException("Stub!");
    }

    public android.content.ComponentName getCallingActivity() {
        throw new RuntimeException("Stub!");
    }

    public void setVisible(boolean visible) {
        throw new RuntimeException("Stub!");
    }

    public boolean isFinishing() {
        throw new RuntimeException("Stub!");
    }

    public boolean isChangingConfigurations() {
        throw new RuntimeException("Stub!");
    }

    public void recreate() {
        throw new RuntimeException("Stub!");
    }

    public void finish() {
        throw new RuntimeException("Stub!");
    }

    public void finishFromChild(android.app.Activity child) {
        throw new RuntimeException("Stub!");
    }

    public void finishActivity(int requestCode) {
        throw new RuntimeException("Stub!");
    }

    public void finishActivityFromChild(android.app.Activity child, int requestCode) {
        throw new RuntimeException("Stub!");
    }

    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        throw new RuntimeException("Stub!");
    }

    public android.app.PendingIntent createPendingResult(int requestCode, android.content.Intent data, int flags) {
        throw new RuntimeException("Stub!");
    }

    public void setRequestedOrientation(int requestedOrientation) {
        throw new RuntimeException("Stub!");
    }

    public int getRequestedOrientation() {
        throw new RuntimeException("Stub!");
    }

    public int getTaskId() {
        throw new RuntimeException("Stub!");
    }

    public boolean isTaskRoot() {
        throw new RuntimeException("Stub!");
    }

    public boolean moveTaskToBack(boolean nonRoot) {
        throw new RuntimeException("Stub!");
    }

    public java.lang.String getLocalClassName() {
        throw new RuntimeException("Stub!");
    }

    public android.content.ComponentName getComponentName() {
        throw new RuntimeException("Stub!");
    }

    public android.content.SharedPreferences getPreferences(int mode) {
        throw new RuntimeException("Stub!");
    }

    public void setTitle(java.lang.CharSequence title) {
        throw new RuntimeException("Stub!");
    }

    public void setTitle(int titleId) {
        throw new RuntimeException("Stub!");
    }

    public void setTitleColor(int textColor) {
        throw new RuntimeException("Stub!");
    }

    public final java.lang.CharSequence getTitle() {
        throw new RuntimeException("Stub!");
    }

    public final int getTitleColor() {
        throw new RuntimeException("Stub!");
    }

    protected void onTitleChanged(java.lang.CharSequence title, int color) {
        throw new RuntimeException("Stub!");
    }

    protected void onChildTitleChanged(android.app.Activity childActivity, java.lang.CharSequence title) {
        throw new RuntimeException("Stub!");
    }

    public final void setProgressBarVisibility(boolean visible) {
        throw new RuntimeException("Stub!");
    }

    public final void setProgressBarIndeterminateVisibility(boolean visible) {
        throw new RuntimeException("Stub!");
    }

    public final void setProgressBarIndeterminate(boolean indeterminate) {
        throw new RuntimeException("Stub!");
    }

    public final void setProgress(int progress) {
        throw new RuntimeException("Stub!");
    }

    public final void setSecondaryProgress(int secondaryProgress) {
        throw new RuntimeException("Stub!");
    }

    public final void setVolumeControlStream(int streamType) {
        throw new RuntimeException("Stub!");
    }

    public final int getVolumeControlStream() {
        throw new RuntimeException("Stub!");
    }

    public final void runOnUiThread(java.lang.Runnable action) {
        throw new RuntimeException("Stub!");
    }

    public android.view.View onCreateView(java.lang.String name, android.content.Context context, android.util.AttributeSet attrs) {
        throw new RuntimeException("Stub!");
    }

    public android.view.View onCreateView(android.view.View parent, java.lang.String name, android.content.Context context, android.util.AttributeSet attrs) {
        throw new RuntimeException("Stub!");
    }

    public void dump(java.lang.String prefix, java.io.FileDescriptor fd, java.io.PrintWriter writer, java.lang.String[] args) {
        throw new RuntimeException("Stub!");
    }

    public android.view.ActionMode startActionMode(android.view.ActionMode.Callback callback) {
        throw new RuntimeException("Stub!");
    }

    public android.view.ActionMode onWindowStartingActionMode(android.view.ActionMode.Callback callback) {
        throw new RuntimeException("Stub!");
    }

    public void onActionModeStarted(android.view.ActionMode mode) {
        throw new RuntimeException("Stub!");
    }

    public void onActionModeFinished(android.view.ActionMode mode) {
        throw new RuntimeException("Stub!");
    }

    public static final int RESULT_CANCELED = 0;

    public static final int RESULT_OK = -1;

    public static final int RESULT_FIRST_USER = 1;

    protected static final int[] FOCUSED_STATE_SET = null;

    public static final int DEFAULT_KEYS_DISABLE = 0;

    public static final int DEFAULT_KEYS_DIALER = 1;

    public static final int DEFAULT_KEYS_SHORTCUT = 2;

    public static final int DEFAULT_KEYS_SEARCH_LOCAL = 3;

    public static final int DEFAULT_KEYS_SEARCH_GLOBAL = 4;

    public Activity() {
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onCreate(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onStart();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onRestart();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onPause();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onResume();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onPostResume();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onStop();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onDestroy();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onRestoreInstanceState(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onPostCreate(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onNewIntent(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onSaveInstanceState(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onUserLeaveHint();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onCreateThumbnail(null, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onCreateDescription();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onConfigurationChanged(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onRetainNonConfigurationInstance();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onLowMemory();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onKeyDown(0, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onKeyLongPress(0, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onKeyUp(0, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onKeyMultiple(0, 0, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onBackPressed();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onTouchEvent(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onTrackballEvent(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onUserInteraction();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onWindowAttributesChanged(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onContentChanged();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onWindowFocusChanged(false);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onAttachedToWindow();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onDetachedFromWindow();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onCreatePanelMenu(0, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onPreparePanel(0, null, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onMenuOpened(0, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onMenuItemSelected(0, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onPanelClosed(0, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onCreateOptionsMenu(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onPrepareOptionsMenu(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onOptionsItemSelected(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onOptionsMenuClosed(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onCreateContextMenu(null, null, null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.dispatchKeyEvent(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.dispatchKeyShortcutEvent(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.dispatchTouchEvent(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.dispatchTrackballEvent(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.dispatchGenericMotionEvent(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.dispatchPopulateAccessibilityEvent(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onCreatePanelView(0);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onSearchRequested();
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onWindowStartingActionMode(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onActionModeStarted(null);
            }
        });
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onActionModeFinished(null);
            }
        });
    }

    public final android.database.Cursor managedQuery(android.net.Uri uri, java.lang.String[] projection, java.lang.String selection, java.lang.String[] selectionArgs, java.lang.String sortOrder) {
        return getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public android.view.View findViewById(int id) {
        return edu.stanford.stamp.harness.ViewFactory.findViewById(null, id);
    }

    public java.lang.Object getSystemService(java.lang.String name) {
        if (name.equals(TELEPHONY_SERVICE)) return TelephonyManager.getInstance(); else if (name.equals(LOCATION_SERVICE)) return LocationManager.getInstance(); else return null;
    }

    public void startActivityForResult(android.content.Intent intent, int requestCode) {
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onActivityResult(0, 0, new android.content.Intent());
            }
        });
    }

    public void startActivityForResult(android.content.Intent intent, int requestCode, android.os.Bundle options) {
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                Activity.this.onActivityResult(0, 0, new android.content.Intent());
            }
        });
    }

    public final void showDialog(int id) {
        this.onCreateDialog(id);
    }
}

