package android.view;
public final class Choreographer
{
public static interface FrameCallback
{
public abstract  void doFrame(long frameTimeNanos);
}
Choreographer() { throw new RuntimeException("Stub!"); }
public static  android.view.Choreographer getInstance() { throw new RuntimeException("Stub!"); }
public  void postFrameCallback(android.view.Choreographer.FrameCallback callback) { throw new RuntimeException("Stub!"); }
public  void postFrameCallbackDelayed(android.view.Choreographer.FrameCallback callback, long delayMillis) { throw new RuntimeException("Stub!"); }
public  void removeFrameCallback(android.view.Choreographer.FrameCallback callback) { throw new RuntimeException("Stub!"); }
}
