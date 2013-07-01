package java.util;
public interface Queue<E>
  extends java.util.Collection<E>
{
public abstract  boolean add(E e);
public abstract  boolean offer(E e);
public abstract  E remove();
public abstract  E poll();
public abstract  E element();
public abstract  E peek();
}
