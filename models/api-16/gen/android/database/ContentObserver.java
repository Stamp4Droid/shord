package android.database;
public abstract class ContentObserver
{
public  ContentObserver(android.os.Handler handler) { throw new RuntimeException("Stub!"); }
public  boolean deliverSelfNotifications() { throw new RuntimeException("Stub!"); }
public  void onChange(boolean selfChange) { throw new RuntimeException("Stub!"); }
public  void onChange(boolean selfChange, android.net.Uri uri) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public final  void dispatchChange(boolean selfChange) { throw new RuntimeException("Stub!"); }
public final  void dispatchChange(boolean selfChange, android.net.Uri uri) { throw new RuntimeException("Stub!"); }
}
