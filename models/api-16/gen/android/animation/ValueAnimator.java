package android.animation;
public class ValueAnimator
  extends android.animation.Animator
{
public static interface AnimatorUpdateListener
{
public abstract  void onAnimationUpdate(android.animation.ValueAnimator animation);
}
public  ValueAnimator() { throw new RuntimeException("Stub!"); }
public static  android.animation.ValueAnimator ofInt(int... values) { throw new RuntimeException("Stub!"); }
public static  android.animation.ValueAnimator ofFloat(float... values) { throw new RuntimeException("Stub!"); }
public static  android.animation.ValueAnimator ofPropertyValuesHolder(android.animation.PropertyValuesHolder... values) { throw new RuntimeException("Stub!"); }
public static  android.animation.ValueAnimator ofObject(android.animation.TypeEvaluator evaluator, java.lang.Object... values) { throw new RuntimeException("Stub!"); }
public  void setIntValues(int... values) { throw new RuntimeException("Stub!"); }
public  void setFloatValues(float... values) { throw new RuntimeException("Stub!"); }
public  void setObjectValues(java.lang.Object... values) { throw new RuntimeException("Stub!"); }
public  void setValues(android.animation.PropertyValuesHolder... values) { throw new RuntimeException("Stub!"); }
public  android.animation.PropertyValuesHolder[] getValues() { throw new RuntimeException("Stub!"); }
public  android.animation.ValueAnimator setDuration(long duration) { throw new RuntimeException("Stub!"); }
public  long getDuration() { throw new RuntimeException("Stub!"); }
public  void setCurrentPlayTime(long playTime) { throw new RuntimeException("Stub!"); }
public  long getCurrentPlayTime() { throw new RuntimeException("Stub!"); }
public  long getStartDelay() { throw new RuntimeException("Stub!"); }
public  void setStartDelay(long startDelay) { throw new RuntimeException("Stub!"); }
public static  long getFrameDelay() { throw new RuntimeException("Stub!"); }
public static  void setFrameDelay(long frameDelay) { throw new RuntimeException("Stub!"); }
public  java.lang.Object getAnimatedValue() { throw new RuntimeException("Stub!"); }
public  java.lang.Object getAnimatedValue(java.lang.String propertyName) { throw new RuntimeException("Stub!"); }
public  void setRepeatCount(int value) { throw new RuntimeException("Stub!"); }
public  int getRepeatCount() { throw new RuntimeException("Stub!"); }
public  void setRepeatMode(int value) { throw new RuntimeException("Stub!"); }
public  int getRepeatMode() { throw new RuntimeException("Stub!"); }
public  void addUpdateListener(android.animation.ValueAnimator.AnimatorUpdateListener listener) { throw new RuntimeException("Stub!"); }
public  void removeAllUpdateListeners() { throw new RuntimeException("Stub!"); }
public  void removeUpdateListener(android.animation.ValueAnimator.AnimatorUpdateListener listener) { throw new RuntimeException("Stub!"); }
public  void setInterpolator(android.animation.TimeInterpolator value) { throw new RuntimeException("Stub!"); }
public  android.animation.TimeInterpolator getInterpolator() { throw new RuntimeException("Stub!"); }
public  void setEvaluator(android.animation.TypeEvaluator value) { throw new RuntimeException("Stub!"); }
public  void start() { throw new RuntimeException("Stub!"); }
public  void cancel() { throw new RuntimeException("Stub!"); }
public  void end() { throw new RuntimeException("Stub!"); }
public  boolean isRunning() { throw new RuntimeException("Stub!"); }
public  boolean isStarted() { throw new RuntimeException("Stub!"); }
public  void reverse() { throw new RuntimeException("Stub!"); }
public  float getAnimatedFraction() { throw new RuntimeException("Stub!"); }
public  android.animation.ValueAnimator clone() { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public static final int RESTART = 1;
public static final int REVERSE = 2;
public static final int INFINITE = -1;
}
