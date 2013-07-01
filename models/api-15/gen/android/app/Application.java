package android.app;
public class Application
  extends android.content.ContextWrapper
  implements android.content.ComponentCallbacks2
{
public static interface ActivityLifecycleCallbacks
{
public abstract  void onActivityCreated(android.app.Activity activity, android.os.Bundle savedInstanceState);
public abstract  void onActivityStarted(android.app.Activity activity);
public abstract  void onActivityResumed(android.app.Activity activity);
public abstract  void onActivityPaused(android.app.Activity activity);
public abstract  void onActivityStopped(android.app.Activity activity);
public abstract  void onActivitySaveInstanceState(android.app.Activity activity, android.os.Bundle outState);
public abstract  void onActivityDestroyed(android.app.Activity activity);
}
public  Application() { super((android.content.Context)null); throw new RuntimeException("Stub!"); }
public  void onCreate() { throw new RuntimeException("Stub!"); }
public  void onTerminate() { throw new RuntimeException("Stub!"); }
public  void onConfigurationChanged(android.content.res.Configuration newConfig) { throw new RuntimeException("Stub!"); }
public  void onLowMemory() { throw new RuntimeException("Stub!"); }
public  void onTrimMemory(int level) { throw new RuntimeException("Stub!"); }
public  void registerComponentCallbacks(android.content.ComponentCallbacks callback) { throw new RuntimeException("Stub!"); }
public  void unregisterComponentCallbacks(android.content.ComponentCallbacks callback) { throw new RuntimeException("Stub!"); }
public  void registerActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks callback) { throw new RuntimeException("Stub!"); }
public  void unregisterActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks callback) { throw new RuntimeException("Stub!"); }
}
