package android.app;
public class DialogFragment
  extends android.app.Fragment
  implements android.content.DialogInterface.OnCancelListener, android.content.DialogInterface.OnDismissListener
{
public  DialogFragment() { throw new RuntimeException("Stub!"); }
public  void setStyle(int style, int theme) { throw new RuntimeException("Stub!"); }
public  void show(android.app.FragmentManager manager, java.lang.String tag) { throw new RuntimeException("Stub!"); }
public  int show(android.app.FragmentTransaction transaction, java.lang.String tag) { throw new RuntimeException("Stub!"); }
public  void dismiss() { throw new RuntimeException("Stub!"); }
public  void dismissAllowingStateLoss() { throw new RuntimeException("Stub!"); }
public  android.app.Dialog getDialog() { throw new RuntimeException("Stub!"); }
public  int getTheme() { throw new RuntimeException("Stub!"); }
public  void setCancelable(boolean cancelable) { throw new RuntimeException("Stub!"); }
public  boolean isCancelable() { throw new RuntimeException("Stub!"); }
public  void setShowsDialog(boolean showsDialog) { throw new RuntimeException("Stub!"); }
public  boolean getShowsDialog() { throw new RuntimeException("Stub!"); }
public  void onAttach(android.app.Activity activity) { throw new RuntimeException("Stub!"); }
public  void onDetach() { throw new RuntimeException("Stub!"); }
public  void onCreate(android.os.Bundle savedInstanceState) { throw new RuntimeException("Stub!"); }
public  android.app.Dialog onCreateDialog(android.os.Bundle savedInstanceState) { throw new RuntimeException("Stub!"); }
public  void onCancel(android.content.DialogInterface dialog) { throw new RuntimeException("Stub!"); }
public  void onDismiss(android.content.DialogInterface dialog) { throw new RuntimeException("Stub!"); }
public  void onActivityCreated(android.os.Bundle savedInstanceState) { throw new RuntimeException("Stub!"); }
public  void onStart() { throw new RuntimeException("Stub!"); }
public  void onSaveInstanceState(android.os.Bundle outState) { throw new RuntimeException("Stub!"); }
public  void onStop() { throw new RuntimeException("Stub!"); }
public  void onDestroyView() { throw new RuntimeException("Stub!"); }
public  void dump(java.lang.String prefix, java.io.FileDescriptor fd, java.io.PrintWriter writer, java.lang.String[] args) { throw new RuntimeException("Stub!"); }
public static final int STYLE_NORMAL = 0;
public static final int STYLE_NO_TITLE = 1;
public static final int STYLE_NO_FRAME = 2;
public static final int STYLE_NO_INPUT = 3;
}
