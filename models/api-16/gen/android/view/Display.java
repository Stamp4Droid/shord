package android.view;
public class Display
{
Display() { throw new RuntimeException("Stub!"); }
public  int getDisplayId() { throw new RuntimeException("Stub!"); }
public  void getSize(android.graphics.Point outSize) { throw new RuntimeException("Stub!"); }
public  void getRectSize(android.graphics.Rect outSize) { throw new RuntimeException("Stub!"); }
public  void getCurrentSizeRange(android.graphics.Point outSmallestSize, android.graphics.Point outLargestSize) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  int getWidth() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  int getHeight() { throw new RuntimeException("Stub!"); }
public  int getRotation() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public native  int getOrientation();
public  int getPixelFormat() { throw new RuntimeException("Stub!"); }
public  float getRefreshRate() { throw new RuntimeException("Stub!"); }
public  void getMetrics(android.util.DisplayMetrics outMetrics) { throw new RuntimeException("Stub!"); }
public static final int DEFAULT_DISPLAY = 0;
}
