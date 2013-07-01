package android.animation;
public abstract class Keyframe
  implements java.lang.Cloneable
{
public  Keyframe() { throw new RuntimeException("Stub!"); }
public static  android.animation.Keyframe ofInt(float fraction, int value) { throw new RuntimeException("Stub!"); }
public static  android.animation.Keyframe ofInt(float fraction) { throw new RuntimeException("Stub!"); }
public static  android.animation.Keyframe ofFloat(float fraction, float value) { throw new RuntimeException("Stub!"); }
public static  android.animation.Keyframe ofFloat(float fraction) { throw new RuntimeException("Stub!"); }
public static  android.animation.Keyframe ofObject(float fraction, java.lang.Object value) { throw new RuntimeException("Stub!"); }
public static  android.animation.Keyframe ofObject(float fraction) { throw new RuntimeException("Stub!"); }
public  boolean hasValue() { throw new RuntimeException("Stub!"); }
public abstract  java.lang.Object getValue();
public abstract  void setValue(java.lang.Object value);
public  float getFraction() { throw new RuntimeException("Stub!"); }
public  void setFraction(float fraction) { throw new RuntimeException("Stub!"); }
public  android.animation.TimeInterpolator getInterpolator() { throw new RuntimeException("Stub!"); }
public  void setInterpolator(android.animation.TimeInterpolator interpolator) { throw new RuntimeException("Stub!"); }
public  java.lang.Class getType() { throw new RuntimeException("Stub!"); }
public abstract  android.animation.Keyframe clone();
}
