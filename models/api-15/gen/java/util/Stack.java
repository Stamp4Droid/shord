package java.util;

public class Stack<E> extends java.util.Vector<E> {

    public Stack() {
        throw new RuntimeException("Stub!");
    }

    public boolean empty() {
        throw new RuntimeException("Stub!");
    }

    public synchronized int search(java.lang.Object o) {
        throw new RuntimeException("Stub!");
    }

    private E f;

    public synchronized E peek() {
        return this.f;
    }

    public synchronized E pop() {
        return this.f;
    }

    public E push(E object) {
        this.f = object;
        return object;
    }
}

