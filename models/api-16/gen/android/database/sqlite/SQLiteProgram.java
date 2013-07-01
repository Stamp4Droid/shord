package android.database.sqlite;
public abstract class SQLiteProgram
  extends android.database.sqlite.SQLiteClosable
{
SQLiteProgram() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public final  int getUniqueId() { throw new RuntimeException("Stub!"); }
public  void bindNull(int index) { throw new RuntimeException("Stub!"); }
public  void bindLong(int index, long value) { throw new RuntimeException("Stub!"); }
public  void bindDouble(int index, double value) { throw new RuntimeException("Stub!"); }
public  void bindString(int index, java.lang.String value) { throw new RuntimeException("Stub!"); }
public  void bindBlob(int index, byte[] value) { throw new RuntimeException("Stub!"); }
public  void clearBindings() { throw new RuntimeException("Stub!"); }
public  void bindAllArgsAsStrings(java.lang.String[] bindArgs) { throw new RuntimeException("Stub!"); }
protected  void onAllReferencesReleased() { throw new RuntimeException("Stub!"); }
}
