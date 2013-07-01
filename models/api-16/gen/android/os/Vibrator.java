package android.os;
public abstract class Vibrator
{
Vibrator() { throw new RuntimeException("Stub!"); }
public abstract  boolean hasVibrator();
public abstract  void vibrate(long milliseconds);
public abstract  void vibrate(long[] pattern, int repeat);
public abstract  void cancel();
}
