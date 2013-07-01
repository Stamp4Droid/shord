package android.content;
public class ClipDescription
  implements android.os.Parcelable
{
public  ClipDescription(java.lang.CharSequence label, java.lang.String[] mimeTypes) { throw new RuntimeException("Stub!"); }
public  ClipDescription(android.content.ClipDescription o) { throw new RuntimeException("Stub!"); }
public static  boolean compareMimeTypes(java.lang.String concreteType, java.lang.String desiredType) { throw new RuntimeException("Stub!"); }
public  java.lang.CharSequence getLabel() { throw new RuntimeException("Stub!"); }
public  boolean hasMimeType(java.lang.String mimeType) { throw new RuntimeException("Stub!"); }
public  java.lang.String[] filterMimeTypes(java.lang.String mimeType) { throw new RuntimeException("Stub!"); }
public  int getMimeTypeCount() { throw new RuntimeException("Stub!"); }
public  java.lang.String getMimeType(int index) { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public static final java.lang.String MIMETYPE_TEXT_PLAIN = "text/plain";
public static final java.lang.String MIMETYPE_TEXT_HTML = "text/html";
public static final java.lang.String MIMETYPE_TEXT_URILIST = "text/uri-list";
public static final java.lang.String MIMETYPE_TEXT_INTENT = "text/vnd.android.intent";
public static final android.os.Parcelable.Creator<android.content.ClipDescription> CREATOR;
static { CREATOR = null; }
}
