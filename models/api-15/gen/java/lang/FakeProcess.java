package java.lang;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class FakeProcess extends Process {

    public FakeProcess() {
    }

    public void destroy() {
    }

    @STAMP(flows = { @Flow(from = "$PROCESS.ExitValue", to = "@return") })
    public int exitValue() {
        return 0;
    }

    @STAMP(flows = { @Flow(from = "$PROCESS.ErrorStream", to = "@return") })
    public java.io.InputStream getErrorStream() {
        try {
            return new java.io.FileInputStream("");
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @STAMP(flows = { @Flow(from = "$PROCESS.InputStream", to = "@return") })
    public java.io.InputStream getInputStream() {
        try {
            return new java.io.FileInputStream("");
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public java.io.OutputStream getOutputStream() {
        try {
            return new java.io.FileOutputStream("");
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public int waitFor() throws java.lang.InterruptedException {
        return 0;
    }
}

