package java.util;

public class HashMap<K, V> extends java.util.AbstractMap<K, V> implements java.lang.Cloneable, java.io.Serializable {

    @java.lang.SuppressWarnings(value = { "unchecked" })
    public HashMap() {
        throw new RuntimeException("Stub!");
    }

    public HashMap(int capacity) {
        throw new RuntimeException("Stub!");
    }

    public HashMap(int capacity, float loadFactor) {
        throw new RuntimeException("Stub!");
    }

    public HashMap(java.util.Map<? extends K, ? extends V> map) {
        throw new RuntimeException("Stub!");
    }

    @java.lang.SuppressWarnings(value = { "unchecked" })
    public java.lang.Object clone() {
        throw new RuntimeException("Stub!");
    }

    public boolean isEmpty() {
        throw new RuntimeException("Stub!");
    }

    public int size() {
        throw new RuntimeException("Stub!");
    }

    public boolean containsKey(java.lang.Object key) {
        throw new RuntimeException("Stub!");
    }

    public boolean containsValue(java.lang.Object value) {
        throw new RuntimeException("Stub!");
    }

    public void putAll(java.util.Map<? extends K, ? extends V> map) {
        throw new RuntimeException("Stub!");
    }

    public V remove(java.lang.Object key) {
        throw new RuntimeException("Stub!");
    }

    public void clear() {
        throw new RuntimeException("Stub!");
    }

    public java.util.Set<K> keySet() {
        throw new RuntimeException("Stub!");
    }

    public java.util.Set<java.util.Map.Entry<K, V>> entrySet() {
        throw new RuntimeException("Stub!");
    }

    private K key;

    private V value;

    public V get(java.lang.Object k) {
        return value;
    }

    public V put(K k, V v) {
        this.key = k;
        this.value = v;
        return this.value;
    }

    public java.util.Collection<V> values() {
        java.util.ArrayList list = new java.util.ArrayList<V>();
        list.add(value);
        return list;
    }
}

