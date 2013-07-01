package java.lang;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class Object {

    public Object() {
        throw new RuntimeException("Stub!");
    }

    public boolean equals(java.lang.Object o) {
        throw new RuntimeException("Stub!");
    }

    protected void finalize() throws java.lang.Throwable {
        throw new RuntimeException("Stub!");
    }

    public final native java.lang.Class<?> getClass();

    public native int hashCode();

    public final native void notify();

    public final native void notifyAll();

    public final void wait() throws java.lang.InterruptedException {
        throw new RuntimeException("Stub!");
    }

    public final void wait(long millis) throws java.lang.InterruptedException {
        throw new RuntimeException("Stub!");
    }

    public final native void wait(long millis, int nanos) throws java.lang.InterruptedException;

    protected java.lang.Object clone() throws java.lang.CloneNotSupportedException {
        return this;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String toString() {
        return new String();
    }
}

