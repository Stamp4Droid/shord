package java.util;
public interface Deque<E>
  extends java.util.Queue<E>
{
public abstract  void addFirst(E e);
public abstract  void addLast(E e);
public abstract  boolean offerFirst(E e);
public abstract  boolean offerLast(E e);
public abstract  E removeFirst();
public abstract  E removeLast();
public abstract  E pollFirst();
public abstract  E pollLast();
public abstract  E getFirst();
public abstract  E getLast();
public abstract  E peekFirst();
public abstract  E peekLast();
public abstract  boolean removeFirstOccurrence(java.lang.Object o);
public abstract  boolean removeLastOccurrence(java.lang.Object o);
public abstract  boolean add(E e);
public abstract  boolean offer(E e);
public abstract  E remove();
public abstract  E poll();
public abstract  E element();
public abstract  E peek();
public abstract  void push(E e);
public abstract  E pop();
public abstract  boolean remove(java.lang.Object o);
public abstract  boolean contains(java.lang.Object o);
public abstract  int size();
public abstract  java.util.Iterator<E> iterator();
public abstract  java.util.Iterator<E> descendingIterator();
}
