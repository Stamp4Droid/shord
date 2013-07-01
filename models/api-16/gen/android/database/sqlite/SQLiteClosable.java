package android.database.sqlite;
public abstract class SQLiteClosable
  implements java.io.Closeable
{
public  SQLiteClosable() { throw new RuntimeException("Stub!"); }
protected abstract  void onAllReferencesReleased();
@java.lang.Deprecated()
protected  void onAllReferencesReleasedFromContainer() { throw new RuntimeException("Stub!"); }
public  void acquireReference() { throw new RuntimeException("Stub!"); }
public  void releaseReference() { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  void releaseReferenceFromContainer() { throw new RuntimeException("Stub!"); }
public  void close() { throw new RuntimeException("Stub!"); }
}
