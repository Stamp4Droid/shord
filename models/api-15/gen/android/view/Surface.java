package android.view;
public class Surface
  implements android.os.Parcelable
{
public static class OutOfResourcesException
  extends java.lang.Exception
{
public  OutOfResourcesException() { throw new RuntimeException("Stub!"); }
public  OutOfResourcesException(java.lang.String name) { throw new RuntimeException("Stub!"); }
}
public  Surface(android.graphics.SurfaceTexture surfaceTexture) { throw new RuntimeException("Stub!"); }
public native  boolean isValid();
public native  void release();
public  android.graphics.Canvas lockCanvas(android.graphics.Rect dirty) throws android.view.Surface.OutOfResourcesException, java.lang.IllegalArgumentException { throw new RuntimeException("Stub!"); }
public native  void unlockCanvasAndPost(android.graphics.Canvas canvas);
public native  void unlockCanvas(android.graphics.Canvas canvas);
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public native  void readFromParcel(android.os.Parcel source);
public native  void writeToParcel(android.os.Parcel dest, int flags);
protected  void finalize() throws java.lang.Throwable { throw new RuntimeException("Stub!"); }
public static final int ROTATION_0 = 0;
public static final int ROTATION_90 = 1;
public static final int ROTATION_180 = 2;
public static final int ROTATION_270 = 3;
public static final android.os.Parcelable.Creator<android.view.Surface> CREATOR;
static { CREATOR = null; }
}
