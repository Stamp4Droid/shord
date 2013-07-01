package java.lang;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public final class StringBuffer extends java.lang.AbstractStringBuilder implements java.lang.Appendable, java.io.Serializable, java.lang.CharSequence {

    public StringBuffer() {
        throw new RuntimeException("Stub!");
    }

    public StringBuffer(int capacity) {
        throw new RuntimeException("Stub!");
    }

    public synchronized char charAt(int index) {
        throw new RuntimeException("Stub!");
    }

    public synchronized int codePointAt(int index) {
        throw new RuntimeException("Stub!");
    }

    public synchronized int codePointBefore(int index) {
        throw new RuntimeException("Stub!");
    }

    public synchronized int codePointCount(int beginIndex, int endIndex) {
        throw new RuntimeException("Stub!");
    }

    public synchronized void ensureCapacity(int min) {
        throw new RuntimeException("Stub!");
    }

    public synchronized int indexOf(java.lang.String subString, int start) {
        throw new RuntimeException("Stub!");
    }

    public synchronized int lastIndexOf(java.lang.String subString, int start) {
        throw new RuntimeException("Stub!");
    }

    public synchronized int offsetByCodePoints(int index, int codePointOffset) {
        throw new RuntimeException("Stub!");
    }

    public synchronized void setCharAt(int index, char ch) {
        throw new RuntimeException("Stub!");
    }

    public synchronized void setLength(int length) {
        throw new RuntimeException("Stub!");
    }

    public synchronized void trimToSize() {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "string", to = "this") })
    public StringBuffer(java.lang.String string) {
    }

    @STAMP(flows = { @Flow(from = "cs", to = "this") })
    public StringBuffer(java.lang.CharSequence cs) {
    }

    public java.lang.StringBuffer append(boolean b) {
        return this;
    }

    public synchronized java.lang.StringBuffer append(char ch) {
        return this;
    }

    public java.lang.StringBuffer append(double d) {
        return this;
    }

    public java.lang.StringBuffer append(float f) {
        return this;
    }

    public java.lang.StringBuffer append(int i) {
        return this;
    }

    public java.lang.StringBuffer append(long l) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "obj", to = "this") })
    public synchronized java.lang.StringBuffer append(java.lang.Object obj) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "string", to = "this") })
    public synchronized java.lang.StringBuffer append(java.lang.String string) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "sb", to = "this") })
    public synchronized java.lang.StringBuffer append(java.lang.StringBuffer sb) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "chars", to = "this") })
    public synchronized java.lang.StringBuffer append(char[] chars) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "chars", to = "this") })
    public synchronized java.lang.StringBuffer append(char[] chars, int start, int length) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "s", to = "this") })
    public synchronized java.lang.StringBuffer append(java.lang.CharSequence s) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "s", to = "this") })
    public synchronized java.lang.StringBuffer append(java.lang.CharSequence s, int start, int end) {
        return this;
    }

    public java.lang.StringBuffer appendCodePoint(int codePoint) {
        return this;
    }

    public synchronized java.lang.StringBuffer delete(int start, int end) {
        return this;
    }

    public synchronized java.lang.StringBuffer deleteCharAt(int location) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "this", to = "buffer") })
    public synchronized void getChars(int start, int end, char[] buffer, int idx) {
    }

    public synchronized java.lang.StringBuffer insert(int index, char ch) {
        return this;
    }

    public java.lang.StringBuffer insert(int index, boolean b) {
        return this;
    }

    public java.lang.StringBuffer insert(int index, int i) {
        return this;
    }

    public java.lang.StringBuffer insert(int index, long l) {
        return this;
    }

    public java.lang.StringBuffer insert(int index, double d) {
        return this;
    }

    public java.lang.StringBuffer insert(int index, float f) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "obj", to = "this") })
    public java.lang.StringBuffer insert(int index, java.lang.Object obj) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "string", to = "this") })
    public synchronized java.lang.StringBuffer insert(int index, java.lang.String string) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "chars", to = "this") })
    public synchronized java.lang.StringBuffer insert(int index, char[] chars) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "chars", to = "this") })
    public synchronized java.lang.StringBuffer insert(int index, char[] chars, int start, int length) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "s", to = "this") })
    public synchronized java.lang.StringBuffer insert(int index, java.lang.CharSequence s) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "s", to = "this") })
    public synchronized java.lang.StringBuffer insert(int index, java.lang.CharSequence s, int start, int end) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "string", to = "this") })
    public synchronized java.lang.StringBuffer replace(int start, int end, java.lang.String string) {
        return this;
    }

    public synchronized java.lang.StringBuffer reverse() {
        return this;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public synchronized java.lang.CharSequence subSequence(int start, int end) {
        return this;
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public synchronized java.lang.String substring(int start) {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public synchronized java.lang.String substring(int start, int end) {
        return new String();
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public synchronized java.lang.String toString() {
        return new String();
    }
}

