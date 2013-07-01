package android.app;
public abstract class FragmentTransaction
{
public  FragmentTransaction() { throw new RuntimeException("Stub!"); }
public abstract  android.app.FragmentTransaction add(android.app.Fragment fragment, java.lang.String tag);
public abstract  android.app.FragmentTransaction add(int containerViewId, android.app.Fragment fragment);
public abstract  android.app.FragmentTransaction add(int containerViewId, android.app.Fragment fragment, java.lang.String tag);
public abstract  android.app.FragmentTransaction replace(int containerViewId, android.app.Fragment fragment);
public abstract  android.app.FragmentTransaction replace(int containerViewId, android.app.Fragment fragment, java.lang.String tag);
public abstract  android.app.FragmentTransaction remove(android.app.Fragment fragment);
public abstract  android.app.FragmentTransaction hide(android.app.Fragment fragment);
public abstract  android.app.FragmentTransaction show(android.app.Fragment fragment);
public abstract  android.app.FragmentTransaction detach(android.app.Fragment fragment);
public abstract  android.app.FragmentTransaction attach(android.app.Fragment fragment);
public abstract  boolean isEmpty();
public abstract  android.app.FragmentTransaction setCustomAnimations(int enter, int exit);
public abstract  android.app.FragmentTransaction setCustomAnimations(int enter, int exit, int popEnter, int popExit);
public abstract  android.app.FragmentTransaction setTransition(int transit);
public abstract  android.app.FragmentTransaction setTransitionStyle(int styleRes);
public abstract  android.app.FragmentTransaction addToBackStack(java.lang.String name);
public abstract  boolean isAddToBackStackAllowed();
public abstract  android.app.FragmentTransaction disallowAddToBackStack();
public abstract  android.app.FragmentTransaction setBreadCrumbTitle(int res);
public abstract  android.app.FragmentTransaction setBreadCrumbTitle(java.lang.CharSequence text);
public abstract  android.app.FragmentTransaction setBreadCrumbShortTitle(int res);
public abstract  android.app.FragmentTransaction setBreadCrumbShortTitle(java.lang.CharSequence text);
public abstract  int commit();
public abstract  int commitAllowingStateLoss();
public static final int TRANSIT_ENTER_MASK = 4096;
public static final int TRANSIT_EXIT_MASK = 8192;
public static final int TRANSIT_UNSET = -1;
public static final int TRANSIT_NONE = 0;
public static final int TRANSIT_FRAGMENT_OPEN = 4097;
public static final int TRANSIT_FRAGMENT_CLOSE = 8194;
public static final int TRANSIT_FRAGMENT_FADE = 4099;
}
