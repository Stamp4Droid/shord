package android.util;
public abstract class Property<T, V>
{
public  Property(java.lang.Class<V> type, java.lang.String name) { throw new RuntimeException("Stub!"); }
public static <T, V> android.util.Property<T, V> of(java.lang.Class<T> hostType, java.lang.Class<V> valueType, java.lang.String name) { throw new RuntimeException("Stub!"); }
public  boolean isReadOnly() { throw new RuntimeException("Stub!"); }
public  void set(T object, V value) { throw new RuntimeException("Stub!"); }
public abstract  V get(T object);
public  java.lang.String getName() { throw new RuntimeException("Stub!"); }
public  java.lang.Class<V> getType() { throw new RuntimeException("Stub!"); }
}
