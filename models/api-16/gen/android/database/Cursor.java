package android.database;
public interface Cursor
  extends java.io.Closeable
{
public abstract  int getCount();
public abstract  int getPosition();
public abstract  boolean move(int offset);
public abstract  boolean moveToPosition(int position);
public abstract  boolean moveToFirst();
public abstract  boolean moveToLast();
public abstract  boolean moveToNext();
public abstract  boolean moveToPrevious();
public abstract  boolean isFirst();
public abstract  boolean isLast();
public abstract  boolean isBeforeFirst();
public abstract  boolean isAfterLast();
public abstract  int getColumnIndex(java.lang.String columnName);
public abstract  int getColumnIndexOrThrow(java.lang.String columnName) throws java.lang.IllegalArgumentException;
public abstract  java.lang.String getColumnName(int columnIndex);
public abstract  java.lang.String[] getColumnNames();
public abstract  int getColumnCount();
public abstract  byte[] getBlob(int columnIndex);
public abstract  java.lang.String getString(int columnIndex);
public abstract  void copyStringToBuffer(int columnIndex, android.database.CharArrayBuffer buffer);
public abstract  short getShort(int columnIndex);
public abstract  int getInt(int columnIndex);
public abstract  long getLong(int columnIndex);
public abstract  float getFloat(int columnIndex);
public abstract  double getDouble(int columnIndex);
public abstract  int getType(int columnIndex);
public abstract  boolean isNull(int columnIndex);
@Deprecated
public abstract  void deactivate();
@java.lang.Deprecated()
public abstract  boolean requery();
public abstract  void close();
public abstract  boolean isClosed();
public abstract  void registerContentObserver(android.database.ContentObserver observer);
public abstract  void unregisterContentObserver(android.database.ContentObserver observer);
public abstract  void registerDataSetObserver(android.database.DataSetObserver observer);
public abstract  void unregisterDataSetObserver(android.database.DataSetObserver observer);
public abstract  void setNotificationUri(android.content.ContentResolver cr, android.net.Uri uri);
public abstract  boolean getWantsAllOnMoveCalls();
public abstract  android.os.Bundle getExtras();
public abstract  android.os.Bundle respond(android.os.Bundle extras);
public static final int FIELD_TYPE_NULL = 0;
public static final int FIELD_TYPE_INTEGER = 1;
public static final int FIELD_TYPE_FLOAT = 2;
public static final int FIELD_TYPE_STRING = 3;
public static final int FIELD_TYPE_BLOB = 4;
}
