package android.os;
public final class Parcel
{
Parcel() { throw new RuntimeException("Stub!"); }
public static  android.os.Parcel obtain() { throw new RuntimeException("Stub!"); }
public final  void recycle() { throw new RuntimeException("Stub!"); }
public final  int dataSize() { throw new RuntimeException("Stub!"); }
public final  int dataAvail() { throw new RuntimeException("Stub!"); }
public final  int dataPosition() { throw new RuntimeException("Stub!"); }
public final  int dataCapacity() { throw new RuntimeException("Stub!"); }
public final  void setDataSize(int size) { throw new RuntimeException("Stub!"); }
public final  void setDataPosition(int pos) { throw new RuntimeException("Stub!"); }
public final  void setDataCapacity(int size) { throw new RuntimeException("Stub!"); }
public final  byte[] marshall() { throw new RuntimeException("Stub!"); }
public final  void unmarshall(byte[] data, int offest, int length) { throw new RuntimeException("Stub!"); }
public final  void appendFrom(android.os.Parcel parcel, int offset, int length) { throw new RuntimeException("Stub!"); }
public final  boolean hasFileDescriptors() { throw new RuntimeException("Stub!"); }
public final  void writeInterfaceToken(java.lang.String interfaceName) { throw new RuntimeException("Stub!"); }
public final  void enforceInterface(java.lang.String interfaceName) { throw new RuntimeException("Stub!"); }
public final  void writeByteArray(byte[] b) { throw new RuntimeException("Stub!"); }
public final  void writeByteArray(byte[] b, int offset, int len) { throw new RuntimeException("Stub!"); }
public final  void writeInt(int val) { throw new RuntimeException("Stub!"); }
public final  void writeLong(long val) { throw new RuntimeException("Stub!"); }
public final  void writeFloat(float val) { throw new RuntimeException("Stub!"); }
public final  void writeDouble(double val) { throw new RuntimeException("Stub!"); }
public final  void writeString(java.lang.String val) { throw new RuntimeException("Stub!"); }
public final  void writeStrongBinder(android.os.IBinder val) { throw new RuntimeException("Stub!"); }
public final  void writeStrongInterface(android.os.IInterface val) { throw new RuntimeException("Stub!"); }
public final  void writeFileDescriptor(java.io.FileDescriptor val) { throw new RuntimeException("Stub!"); }
public final  void writeByte(byte val) { throw new RuntimeException("Stub!"); }
public final  void writeMap(java.util.Map val) { throw new RuntimeException("Stub!"); }
public final  void writeBundle(android.os.Bundle val) { throw new RuntimeException("Stub!"); }
public final  void writeList(java.util.List val) { throw new RuntimeException("Stub!"); }
public final  void writeArray(java.lang.Object[] val) { throw new RuntimeException("Stub!"); }
public final  void writeSparseArray(android.util.SparseArray<java.lang.Object> val) { throw new RuntimeException("Stub!"); }
public final  void writeSparseBooleanArray(android.util.SparseBooleanArray val) { throw new RuntimeException("Stub!"); }
public final  void writeBooleanArray(boolean[] val) { throw new RuntimeException("Stub!"); }
public final  boolean[] createBooleanArray() { throw new RuntimeException("Stub!"); }
public final  void readBooleanArray(boolean[] val) { throw new RuntimeException("Stub!"); }
public final  void writeCharArray(char[] val) { throw new RuntimeException("Stub!"); }
public final  char[] createCharArray() { throw new RuntimeException("Stub!"); }
public final  void readCharArray(char[] val) { throw new RuntimeException("Stub!"); }
public final  void writeIntArray(int[] val) { throw new RuntimeException("Stub!"); }
public final  int[] createIntArray() { throw new RuntimeException("Stub!"); }
public final  void readIntArray(int[] val) { throw new RuntimeException("Stub!"); }
public final  void writeLongArray(long[] val) { throw new RuntimeException("Stub!"); }
public final  long[] createLongArray() { throw new RuntimeException("Stub!"); }
public final  void readLongArray(long[] val) { throw new RuntimeException("Stub!"); }
public final  void writeFloatArray(float[] val) { throw new RuntimeException("Stub!"); }
public final  float[] createFloatArray() { throw new RuntimeException("Stub!"); }
public final  void readFloatArray(float[] val) { throw new RuntimeException("Stub!"); }
public final  void writeDoubleArray(double[] val) { throw new RuntimeException("Stub!"); }
public final  double[] createDoubleArray() { throw new RuntimeException("Stub!"); }
public final  void readDoubleArray(double[] val) { throw new RuntimeException("Stub!"); }
public final  void writeStringArray(java.lang.String[] val) { throw new RuntimeException("Stub!"); }
public final  java.lang.String[] createStringArray() { throw new RuntimeException("Stub!"); }
public final  void readStringArray(java.lang.String[] val) { throw new RuntimeException("Stub!"); }
public final  void writeBinderArray(android.os.IBinder[] val) { throw new RuntimeException("Stub!"); }
public final  android.os.IBinder[] createBinderArray() { throw new RuntimeException("Stub!"); }
public final  void readBinderArray(android.os.IBinder[] val) { throw new RuntimeException("Stub!"); }
public final <T extends android.os.Parcelable> void writeTypedList(java.util.List<T> val) { throw new RuntimeException("Stub!"); }
public final  void writeStringList(java.util.List<java.lang.String> val) { throw new RuntimeException("Stub!"); }
public final  void writeBinderList(java.util.List<android.os.IBinder> val) { throw new RuntimeException("Stub!"); }
public final <T extends android.os.Parcelable> void writeTypedArray(T[] val, int parcelableFlags) { throw new RuntimeException("Stub!"); }
public final  void writeValue(java.lang.Object v) { throw new RuntimeException("Stub!"); }
public final  void writeParcelable(android.os.Parcelable p, int parcelableFlags) { throw new RuntimeException("Stub!"); }
public final  void writeSerializable(java.io.Serializable s) { throw new RuntimeException("Stub!"); }
public final  void writeException(java.lang.Exception e) { throw new RuntimeException("Stub!"); }
public final  void writeNoException() { throw new RuntimeException("Stub!"); }
public final  void readException() { throw new RuntimeException("Stub!"); }
public final  void readException(int code, java.lang.String msg) { throw new RuntimeException("Stub!"); }
public final  int readInt() { throw new RuntimeException("Stub!"); }
public final  long readLong() { throw new RuntimeException("Stub!"); }
public final  float readFloat() { throw new RuntimeException("Stub!"); }
public final  double readDouble() { throw new RuntimeException("Stub!"); }
public final  java.lang.String readString() { throw new RuntimeException("Stub!"); }
public final  android.os.IBinder readStrongBinder() { throw new RuntimeException("Stub!"); }
public final  android.os.ParcelFileDescriptor readFileDescriptor() { throw new RuntimeException("Stub!"); }
public final  byte readByte() { throw new RuntimeException("Stub!"); }
public final  void readMap(java.util.Map outVal, java.lang.ClassLoader loader) { throw new RuntimeException("Stub!"); }
public final  void readList(java.util.List outVal, java.lang.ClassLoader loader) { throw new RuntimeException("Stub!"); }
public final  java.util.HashMap readHashMap(java.lang.ClassLoader loader) { throw new RuntimeException("Stub!"); }
public final  android.os.Bundle readBundle() { throw new RuntimeException("Stub!"); }
public final  android.os.Bundle readBundle(java.lang.ClassLoader loader) { throw new RuntimeException("Stub!"); }
public final  byte[] createByteArray() { throw new RuntimeException("Stub!"); }
public final  void readByteArray(byte[] val) { throw new RuntimeException("Stub!"); }
public final  java.util.ArrayList readArrayList(java.lang.ClassLoader loader) { throw new RuntimeException("Stub!"); }
public final  java.lang.Object[] readArray(java.lang.ClassLoader loader) { throw new RuntimeException("Stub!"); }
public final  android.util.SparseArray readSparseArray(java.lang.ClassLoader loader) { throw new RuntimeException("Stub!"); }
public final  android.util.SparseBooleanArray readSparseBooleanArray() { throw new RuntimeException("Stub!"); }
public final <T> java.util.ArrayList<T> createTypedArrayList(android.os.Parcelable.Creator<T> c) { throw new RuntimeException("Stub!"); }
public final <T> void readTypedList(java.util.List<T> list, android.os.Parcelable.Creator<T> c) { throw new RuntimeException("Stub!"); }
public final  java.util.ArrayList<java.lang.String> createStringArrayList() { throw new RuntimeException("Stub!"); }
public final  java.util.ArrayList<android.os.IBinder> createBinderArrayList() { throw new RuntimeException("Stub!"); }
public final  void readStringList(java.util.List<java.lang.String> list) { throw new RuntimeException("Stub!"); }
public final  void readBinderList(java.util.List<android.os.IBinder> list) { throw new RuntimeException("Stub!"); }
public final <T> T[] createTypedArray(android.os.Parcelable.Creator<T> c) { throw new RuntimeException("Stub!"); }
public final <T> void readTypedArray(T[] val, android.os.Parcelable.Creator<T> c) { throw new RuntimeException("Stub!"); }
public final <T extends android.os.Parcelable> void writeParcelableArray(T[] value, int parcelableFlags) { throw new RuntimeException("Stub!"); }
public final  java.lang.Object readValue(java.lang.ClassLoader loader) { throw new RuntimeException("Stub!"); }
public final <T extends android.os.Parcelable> T readParcelable(java.lang.ClassLoader loader) { throw new RuntimeException("Stub!"); }
public final  android.os.Parcelable[] readParcelableArray(java.lang.ClassLoader loader) { throw new RuntimeException("Stub!"); }
public final  java.io.Serializable readSerializable() { throw new RuntimeException("Stub!"); }
protected static final  android.os.Parcel obtain(int obj) { throw new RuntimeException("Stub!"); }
protected  void finalize() throws java.lang.Throwable { throw new RuntimeException("Stub!"); }
public static final android.os.Parcelable.Creator<java.lang.String> STRING_CREATOR;
static { STRING_CREATOR = null; }
}
