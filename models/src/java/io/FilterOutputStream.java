class FilterOutputStream
{
    public FilterOutputStream(java.io.OutputStream out) {
		this.out = out;
    }

	public  void write(byte[] buffer, int offset, int length) throws java.io.IOException 
	{ 
		out.write(buffer, offset, length);
	}

	public  void write(int oneByte) throws java.io.IOException 
	{ 
		out.write(oneByte);
	}
	
	public  void write(byte[] buffer) throws java.io.IOException 
	{ 
		out.write(buffer);
	}
}