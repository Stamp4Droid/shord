package android.nfc;
public final class NdefRecord
  implements android.os.Parcelable
{
public  NdefRecord(short tnf, byte[] type, byte[] id, byte[] payload) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  NdefRecord(byte[] data) throws android.nfc.FormatException { throw new RuntimeException("Stub!"); }
public static  android.nfc.NdefRecord createApplicationRecord(java.lang.String packageName) { throw new RuntimeException("Stub!"); }
public static  android.nfc.NdefRecord createUri(android.net.Uri uri) { throw new RuntimeException("Stub!"); }
public static  android.nfc.NdefRecord createUri(java.lang.String uriString) { throw new RuntimeException("Stub!"); }
public static  android.nfc.NdefRecord createMime(java.lang.String mimeType, byte[] mimeData) { throw new RuntimeException("Stub!"); }
public static  android.nfc.NdefRecord createExternal(java.lang.String domain, java.lang.String type, byte[] data) { throw new RuntimeException("Stub!"); }
public  short getTnf() { throw new RuntimeException("Stub!"); }
public  byte[] getType() { throw new RuntimeException("Stub!"); }
public  byte[] getId() { throw new RuntimeException("Stub!"); }
public  byte[] getPayload() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  byte[] toByteArray() { throw new RuntimeException("Stub!"); }
public  java.lang.String toMimeType() { throw new RuntimeException("Stub!"); }
public  android.net.Uri toUri() { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public  int hashCode() { throw new RuntimeException("Stub!"); }
public  boolean equals(java.lang.Object obj) { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public static final short TNF_EMPTY = 0;
public static final short TNF_WELL_KNOWN = 1;
public static final short TNF_MIME_MEDIA = 2;
public static final short TNF_ABSOLUTE_URI = 3;
public static final short TNF_EXTERNAL_TYPE = 4;
public static final short TNF_UNKNOWN = 5;
public static final short TNF_UNCHANGED = 6;
public static final byte[] RTD_TEXT = null;
public static final byte[] RTD_URI = null;
public static final byte[] RTD_SMART_POSTER = null;
public static final byte[] RTD_ALTERNATIVE_CARRIER = null;
public static final byte[] RTD_HANDOVER_CARRIER = null;
public static final byte[] RTD_HANDOVER_REQUEST = null;
public static final byte[] RTD_HANDOVER_SELECT = null;
public static final android.os.Parcelable.Creator<android.nfc.NdefRecord> CREATOR;
static { CREATOR = null; }
}
