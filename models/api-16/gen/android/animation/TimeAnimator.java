package android.animation;
public class TimeAnimator
  extends android.animation.ValueAnimator
{
public static interface TimeListener
{
public abstract  void onTimeUpdate(android.animation.TimeAnimator animation, long totalTime, long deltaTime);
}
public  TimeAnimator() { throw new RuntimeException("Stub!"); }
public  void setTimeListener(android.animation.TimeAnimator.TimeListener listener) { throw new RuntimeException("Stub!"); }
}
