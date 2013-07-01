package java.nio;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public abstract class CharBuffer extends java.nio.Buffer implements java.lang.Comparable<java.nio.CharBuffer>, java.lang.CharSequence, java.lang.Appendable, java.lang.Readable {

    public static java.nio.CharBuffer allocate(int capacity) {
        throw new RuntimeException("Stub!");
    }

    public static java.nio.CharBuffer wrap(char[] array) {
        throw new RuntimeException("Stub!");
    }

    public static java.nio.CharBuffer wrap(char[] array, int start, int charCount) {
        throw new RuntimeException("Stub!");
    }

    public static java.nio.CharBuffer wrap(java.lang.CharSequence chseq) {
        throw new RuntimeException("Stub!");
    }

    public static java.nio.CharBuffer wrap(java.lang.CharSequence cs, int start, int end) {
        throw new RuntimeException("Stub!");
    }

    public final int arrayOffset() {
        throw new RuntimeException("Stub!");
    }

    public abstract java.nio.CharBuffer asReadOnlyBuffer();

    public abstract java.nio.CharBuffer compact();

    public int compareTo(java.nio.CharBuffer otherBuffer) {
        throw new RuntimeException("Stub!");
    }

    public abstract java.nio.CharBuffer duplicate();

    public boolean equals(java.lang.Object other) {
        throw new RuntimeException("Stub!");
    }

    public abstract char get();

    public abstract char get(int index);

    public final boolean hasArray() {
        throw new RuntimeException("Stub!");
    }

    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    public abstract boolean isDirect();

    public final int length() {
        throw new RuntimeException("Stub!");
    }

    public abstract java.nio.ByteOrder order();

    public abstract java.nio.CharBuffer put(char c);

    public abstract java.nio.CharBuffer put(int index, char c);

    public abstract java.nio.CharBuffer slice();

    public abstract java.lang.CharSequence subSequence(int start, int end);

    private char f;

    public CharBuffer() {
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public final char[] array() {
        char[] c = new char[0];
        c[0] = this.f;
        return c;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public final char charAt(int index) {
        return this.f;
    }

    @STAMP(flows = { @Flow(from = "this", to = "dst") })
    public java.nio.CharBuffer get(char[] dst) {
        dst[0] = this.f;
        return this;
    }

    @STAMP(flows = { @Flow(from = "this", to = "dst") })
    public java.nio.CharBuffer get(char[] dst, int dstOffset, int charCount) {
        dst[0] = this.f;
        return this;
    }

    @STAMP(flows = { @Flow(from = "src", to = "this") })
    public final java.nio.CharBuffer put(char[] src) {
        this.f = src[0];
        return this;
    }

    @STAMP(flows = { @Flow(from = "src", to = "this") })
    public java.nio.CharBuffer put(char[] src, int srcOffset, int charCount) {
        this.f = src[0];
        return this;
    }

    @STAMP(flows = { @Flow(from = "src", to = "this") })
    public java.nio.CharBuffer put(java.nio.CharBuffer src) {
        this.f = src.charAt(0);
        return this;
    }

    @STAMP(flows = { @Flow(from = "str", to = "this") })
    public final java.nio.CharBuffer put(java.lang.String str) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "str", to = "this") })
    public java.nio.CharBuffer put(java.lang.String str, int start, int end) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public java.lang.String toString() {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "c", to = "this") })
    public java.nio.CharBuffer append(char c) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "csq", to = "this") })
    public java.nio.CharBuffer append(java.lang.CharSequence csq) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "csq", to = "this") })
    public java.nio.CharBuffer append(java.lang.CharSequence csq, int start, int end) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "this", to = "target") })
    public int read(java.nio.CharBuffer target) throws java.io.IOException {
        return 0;
    }
}

