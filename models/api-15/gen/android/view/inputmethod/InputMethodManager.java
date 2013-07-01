package android.view.inputmethod;
public final class InputMethodManager
{
InputMethodManager() { throw new RuntimeException("Stub!"); }
public  java.util.List<android.view.inputmethod.InputMethodInfo> getInputMethodList() { throw new RuntimeException("Stub!"); }
public  java.util.List<android.view.inputmethod.InputMethodInfo> getEnabledInputMethodList() { throw new RuntimeException("Stub!"); }
public  java.util.List<android.view.inputmethod.InputMethodSubtype> getEnabledInputMethodSubtypeList(android.view.inputmethod.InputMethodInfo imi, boolean allowsImplicitlySelectedSubtypes) { throw new RuntimeException("Stub!"); }
public  void showStatusIcon(android.os.IBinder imeToken, java.lang.String packageName, int iconId) { throw new RuntimeException("Stub!"); }
public  void hideStatusIcon(android.os.IBinder imeToken) { throw new RuntimeException("Stub!"); }
public  boolean isFullscreenMode() { throw new RuntimeException("Stub!"); }
public  boolean isActive(android.view.View view) { throw new RuntimeException("Stub!"); }
public  boolean isActive() { throw new RuntimeException("Stub!"); }
public  boolean isAcceptingText() { throw new RuntimeException("Stub!"); }
public  void displayCompletions(android.view.View view, android.view.inputmethod.CompletionInfo[] completions) { throw new RuntimeException("Stub!"); }
public  void updateExtractedText(android.view.View view, int token, android.view.inputmethod.ExtractedText text) { throw new RuntimeException("Stub!"); }
public  boolean showSoftInput(android.view.View view, int flags) { throw new RuntimeException("Stub!"); }
public  boolean showSoftInput(android.view.View view, int flags, android.os.ResultReceiver resultReceiver) { throw new RuntimeException("Stub!"); }
public  boolean hideSoftInputFromWindow(android.os.IBinder windowToken, int flags) { throw new RuntimeException("Stub!"); }
public  boolean hideSoftInputFromWindow(android.os.IBinder windowToken, int flags, android.os.ResultReceiver resultReceiver) { throw new RuntimeException("Stub!"); }
public  void toggleSoftInputFromWindow(android.os.IBinder windowToken, int showFlags, int hideFlags) { throw new RuntimeException("Stub!"); }
public  void toggleSoftInput(int showFlags, int hideFlags) { throw new RuntimeException("Stub!"); }
public  void restartInput(android.view.View view) { throw new RuntimeException("Stub!"); }
public  void updateSelection(android.view.View view, int selStart, int selEnd, int candidatesStart, int candidatesEnd) { throw new RuntimeException("Stub!"); }
public  void viewClicked(android.view.View view) { throw new RuntimeException("Stub!"); }
public  boolean isWatchingCursor(android.view.View view) { throw new RuntimeException("Stub!"); }
public  void updateCursor(android.view.View view, int left, int top, int right, int bottom) { throw new RuntimeException("Stub!"); }
public  void sendAppPrivateCommand(android.view.View view, java.lang.String action, android.os.Bundle data) { throw new RuntimeException("Stub!"); }
public  void setInputMethod(android.os.IBinder token, java.lang.String id) { throw new RuntimeException("Stub!"); }
public  void setInputMethodAndSubtype(android.os.IBinder token, java.lang.String id, android.view.inputmethod.InputMethodSubtype subtype) { throw new RuntimeException("Stub!"); }
public  void hideSoftInputFromInputMethod(android.os.IBinder token, int flags) { throw new RuntimeException("Stub!"); }
public  void showSoftInputFromInputMethod(android.os.IBinder token, int flags) { throw new RuntimeException("Stub!"); }
public  void showInputMethodPicker() { throw new RuntimeException("Stub!"); }
public  void showInputMethodAndSubtypeEnabler(java.lang.String imiId) { throw new RuntimeException("Stub!"); }
public  android.view.inputmethod.InputMethodSubtype getCurrentInputMethodSubtype() { throw new RuntimeException("Stub!"); }
public  boolean setCurrentInputMethodSubtype(android.view.inputmethod.InputMethodSubtype subtype) { throw new RuntimeException("Stub!"); }
public  java.util.Map<android.view.inputmethod.InputMethodInfo, java.util.List<android.view.inputmethod.InputMethodSubtype>> getShortcutInputMethodsAndSubtypes() { throw new RuntimeException("Stub!"); }
public  boolean switchToLastInputMethod(android.os.IBinder imeToken) { throw new RuntimeException("Stub!"); }
public  void setAdditionalInputMethodSubtypes(java.lang.String imiId, android.view.inputmethod.InputMethodSubtype[] subtypes) { throw new RuntimeException("Stub!"); }
public  android.view.inputmethod.InputMethodSubtype getLastInputMethodSubtype() { throw new RuntimeException("Stub!"); }
public static final int SHOW_IMPLICIT = 1;
public static final int SHOW_FORCED = 2;
public static final int RESULT_UNCHANGED_SHOWN = 0;
public static final int RESULT_UNCHANGED_HIDDEN = 1;
public static final int RESULT_SHOWN = 2;
public static final int RESULT_HIDDEN = 3;
public static final int HIDE_IMPLICIT_ONLY = 1;
public static final int HIDE_NOT_ALWAYS = 2;
}
