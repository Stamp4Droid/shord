package android.view;
public class DragEvent
  implements android.os.Parcelable
{
DragEvent() { throw new RuntimeException("Stub!"); }
public  int getAction() { throw new RuntimeException("Stub!"); }
public  float getX() { throw new RuntimeException("Stub!"); }
public  float getY() { throw new RuntimeException("Stub!"); }
public  android.content.ClipData getClipData() { throw new RuntimeException("Stub!"); }
public  android.content.ClipDescription getClipDescription() { throw new RuntimeException("Stub!"); }
public  java.lang.Object getLocalState() { throw new RuntimeException("Stub!"); }
public  boolean getResult() { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public static final int ACTION_DRAG_STARTED = 1;
public static final int ACTION_DRAG_LOCATION = 2;
public static final int ACTION_DROP = 3;
public static final int ACTION_DRAG_ENDED = 4;
public static final int ACTION_DRAG_ENTERED = 5;
public static final int ACTION_DRAG_EXITED = 6;
public static final android.os.Parcelable.Creator<android.view.DragEvent> CREATOR;
static { CREATOR = null; }
}
