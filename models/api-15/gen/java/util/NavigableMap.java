package java.util;
public interface NavigableMap<K, V>
  extends java.util.SortedMap<K, V>
{
public abstract  java.util.Map.Entry<K, V> lowerEntry(K key);
public abstract  K lowerKey(K key);
public abstract  java.util.Map.Entry<K, V> floorEntry(K key);
public abstract  K floorKey(K key);
public abstract  java.util.Map.Entry<K, V> ceilingEntry(K key);
public abstract  K ceilingKey(K key);
public abstract  java.util.Map.Entry<K, V> higherEntry(K key);
public abstract  K higherKey(K key);
public abstract  java.util.Map.Entry<K, V> firstEntry();
public abstract  java.util.Map.Entry<K, V> lastEntry();
public abstract  java.util.Map.Entry<K, V> pollFirstEntry();
public abstract  java.util.Map.Entry<K, V> pollLastEntry();
public abstract  java.util.NavigableMap<K, V> descendingMap();
public abstract  java.util.NavigableSet<K> navigableKeySet();
public abstract  java.util.NavigableSet<K> descendingKeySet();
public abstract  java.util.NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);
public abstract  java.util.NavigableMap<K, V> headMap(K toKey, boolean inclusive);
public abstract  java.util.NavigableMap<K, V> tailMap(K fromKey, boolean inclusive);
public abstract  java.util.SortedMap<K, V> subMap(K fromKey, K toKey);
public abstract  java.util.SortedMap<K, V> headMap(K toKey);
public abstract  java.util.SortedMap<K, V> tailMap(K fromKey);
}
