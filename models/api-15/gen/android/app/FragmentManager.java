package android.app;
public abstract class FragmentManager
{
public static interface BackStackEntry
{
public abstract  int getId();
public abstract  java.lang.String getName();
public abstract  int getBreadCrumbTitleRes();
public abstract  int getBreadCrumbShortTitleRes();
public abstract  java.lang.CharSequence getBreadCrumbTitle();
public abstract  java.lang.CharSequence getBreadCrumbShortTitle();
}
public static interface OnBackStackChangedListener
{
public abstract  void onBackStackChanged();
}
public  FragmentManager() { throw new RuntimeException("Stub!"); }
public abstract  android.app.FragmentTransaction beginTransaction();
public abstract  boolean executePendingTransactions();
public abstract  android.app.Fragment findFragmentById(int id);
public abstract  android.app.Fragment findFragmentByTag(java.lang.String tag);
public abstract  void popBackStack();
public abstract  boolean popBackStackImmediate();
public abstract  void popBackStack(java.lang.String name, int flags);
public abstract  boolean popBackStackImmediate(java.lang.String name, int flags);
public abstract  void popBackStack(int id, int flags);
public abstract  boolean popBackStackImmediate(int id, int flags);
public abstract  int getBackStackEntryCount();
public abstract  android.app.FragmentManager.BackStackEntry getBackStackEntryAt(int index);
public abstract  void addOnBackStackChangedListener(android.app.FragmentManager.OnBackStackChangedListener listener);
public abstract  void removeOnBackStackChangedListener(android.app.FragmentManager.OnBackStackChangedListener listener);
public abstract  void putFragment(android.os.Bundle bundle, java.lang.String key, android.app.Fragment fragment);
public abstract  android.app.Fragment getFragment(android.os.Bundle bundle, java.lang.String key);
public abstract  android.app.Fragment.SavedState saveFragmentInstanceState(android.app.Fragment f);
public abstract  void dump(java.lang.String prefix, java.io.FileDescriptor fd, java.io.PrintWriter writer, java.lang.String[] args);
public static  void enableDebugLogging(boolean enabled) { throw new RuntimeException("Stub!"); }
public  void invalidateOptionsMenu() { throw new RuntimeException("Stub!"); }
public static final int POP_BACK_STACK_INCLUSIVE = 1;
}
