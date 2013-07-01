package java.util;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class ArrayList<E> extends java.util.AbstractList<E> implements java.lang.Cloneable, java.io.Serializable, java.util.RandomAccess {

    public ArrayList(int capacity) {
        throw new RuntimeException("Stub!");
    }

    public ArrayList() {
        throw new RuntimeException("Stub!");
    }

    public ArrayList(java.util.Collection<? extends E> collection) {
        throw new RuntimeException("Stub!");
    }

    public boolean addAll(java.util.Collection<? extends E> collection) {
        throw new RuntimeException("Stub!");
    }

    public boolean addAll(int index, java.util.Collection<? extends E> collection) {
        throw new RuntimeException("Stub!");
    }

    public void clear() {
        throw new RuntimeException("Stub!");
    }

    public java.lang.Object clone() {
        throw new RuntimeException("Stub!");
    }

    public void ensureCapacity(int minimumCapacity) {
        throw new RuntimeException("Stub!");
    }

    public int size() {
        throw new RuntimeException("Stub!");
    }

    public boolean isEmpty() {
        throw new RuntimeException("Stub!");
    }

    public boolean contains(java.lang.Object object) {
        throw new RuntimeException("Stub!");
    }

    public int indexOf(java.lang.Object object) {
        throw new RuntimeException("Stub!");
    }

    public int lastIndexOf(java.lang.Object object) {
        throw new RuntimeException("Stub!");
    }

    public boolean remove(java.lang.Object object) {
        throw new RuntimeException("Stub!");
    }

    protected void removeRange(int fromIndex, int toIndex) {
        throw new RuntimeException("Stub!");
    }

    public E set(int index, E object) {
        throw new RuntimeException("Stub!");
    }

    public <T> T[] toArray(T[] contents) {
        throw new RuntimeException("Stub!");
    }

    public void trimToSize() {
        throw new RuntimeException("Stub!");
    }

    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    public boolean equals(java.lang.Object o) {
        throw new RuntimeException("Stub!");
    }

    private E f;

    @STAMP(flows = { @Flow(from = "object", to = "this") })
    public boolean add(E object) {
        this.f = object;
        return true;
    }

    @STAMP(flows = { @Flow(from = "object", to = "this") })
    public void add(int index, E object) {
        add(object);
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public E get(int index) {
        return f;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public E remove(int index) {
        return f;
    }

    public java.util.Iterator<E> iterator() {
        return new ArrayListIterator();
    }

    public java.lang.Object[] toArray() {
        java.lang.Object[] res = { f };
        return res;
    }

    private class ArrayListIterator implements Iterator<E> {

        public boolean hasNext() {
            throw new RuntimeException("Stub!");
        }

        public E next() {
            return ArrayList.this.f;
        }

        public void remove() {
            throw new RuntimeException("Stub!");
        }
    }
}

