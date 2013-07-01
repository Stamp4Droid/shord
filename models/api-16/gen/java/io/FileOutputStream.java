package java.io;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class FileOutputStream extends java.io.OutputStream implements java.io.Closeable {

    public void close() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    protected void finalize() throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public java.nio.channels.FileChannel getChannel() {
        throw new RuntimeException("Stub!");
    }

    @STAMP(flows = { @Flow(from = "file", to = "this") })
    public FileOutputStream(java.io.File file) throws java.io.FileNotFoundException {
    }

    @STAMP(flows = { @Flow(from = "file", to = "this") })
    public FileOutputStream(java.io.File file, boolean append) throws java.io.FileNotFoundException {
    }

    @STAMP(flows = { @Flow(from = "fd", to = "this") })
    public FileOutputStream(java.io.FileDescriptor fd) {
    }

    @STAMP(flows = { @Flow(from = "path", to = "this") })
    public FileOutputStream(java.lang.String path) throws java.io.FileNotFoundException {
    }

    @STAMP(flows = { @Flow(from = "path", to = "this") })
    public FileOutputStream(java.lang.String path, boolean append) throws java.io.FileNotFoundException {
    }

    @STAMP(flows = { @Flow(from = "this", to = "@return") })
    public final java.io.FileDescriptor getFD() throws java.io.IOException {
        return new FileDescriptor();
    }

    @STAMP(flows = { @Flow(from = "b", to = "!FILE") })
    public void write(byte[] b, int off, int len) throws java.io.IOException {
    }

    @STAMP(flows = { @Flow(from = "b", to = "!FILE") })
    public void write(byte[] b) throws java.io.IOException {
    }

    @STAMP(flows = { @Flow(from = "oneByte", to = "!FILE") })
    public void write(int oneByte) throws java.io.IOException {
    }
}

