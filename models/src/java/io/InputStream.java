class InputStream
{
    @STAMP(flows = {@Flow(from="this",to="@return")})
	private byte taintByte() { return (byte) 0; }

	public  int read(byte[] buffer) throws java.io.IOException 
	{ 
		buffer[0] = taintByte(); 
		return 0; 
	}
	
	public  int read(byte[] buffer, int offset, int length) throws java.io.IOException 
	{ 
		buffer[0] = taintByte(); 
		return 0; 
	}

}