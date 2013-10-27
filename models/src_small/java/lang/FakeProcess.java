package java.lang;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class FakeProcess extends Process
{
	public FakeProcess() {}

	public void destroy() {}

	public int exitValue() { return 0; }

	public java.io.InputStream getErrorStream() 
	{ 
		return new java.io.StampInputStream();
	}

	public java.io.InputStream getInputStream() 
	{ 
		return new java.io.StampInputStream();
	}

	public java.io.OutputStream getOutputStream() 
	{ 
		return new java.io.StampOutputStream(); 
	}

	public int waitFor() throws java.lang.InterruptedException { return 0; }
}
