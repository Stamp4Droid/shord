package java.util;
public interface NavigableSet<E>
  extends java.util.SortedSet<E>
{
public abstract  E lower(E e);
public abstract  E floor(E e);
public abstract  E ceiling(E e);
public abstract  E higher(E e);
public abstract  E pollFirst();
public abstract  E pollLast();
public abstract  java.util.Iterator<E> iterator();
public abstract  java.util.NavigableSet<E> descendingSet();
public abstract  java.util.Iterator<E> descendingIterator();
public abstract  java.util.NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive);
public abstract  java.util.NavigableSet<E> headSet(E toElement, boolean inclusive);
public abstract  java.util.NavigableSet<E> tailSet(E fromElement, boolean inclusive);
public abstract  java.util.SortedSet<E> subSet(E fromElement, E toElement);
public abstract  java.util.SortedSet<E> headSet(E toElement);
public abstract  java.util.SortedSet<E> tailSet(E fromElement);
}
