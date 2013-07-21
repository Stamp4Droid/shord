class ByteBuffer
{
    public ByteBuffer() {}

	/*
    @STAMP(flows = {@Flow(from="b",to="@return")})
    public static java.nio.ByteBuffer put(byte b) {
     	return new ByteBuffer();
    }

    @STAMP(flows = {@Flow(from="array",to="@return")})
    public static java.nio.ByteBuffer wrap(byte[] array) 
    { 
		return new ByteBuffer();
	}

    @STAMP(flows = {@Flow(from="array",to="@return")})
    public static  java.nio.ByteBuffer wrap(byte[] array, int start, int byteCount) 
    {  
		return new ByteBuffer();
	}
	*/
    @STAMP(flows = {@Flow(from="this",to="@return")})
    public final  byte[] array() { return new byte[0]; }

    @STAMP(flows = {@Flow(from="this",to="dst")})
    public  java.nio.ByteBuffer get(byte[] dst) 
    { 
		return this;  
    }

    @STAMP(flows = {@Flow(from="this",to="dst")})
    public  java.nio.ByteBuffer get(byte[] dst, int dstOffset, int byteCount) 
    { 
		return this;  
    }

    @STAMP(flows = {@Flow(from="src",to="this")})
    public final  java.nio.ByteBuffer put(byte[] src) 
    {
		return this;
    }

    @STAMP(flows = {@Flow(from="src",to="this")})
    public  java.nio.ByteBuffer put(byte[] src, int srcOffset, int byteCount) 
    {
		return this;
    }

    @STAMP(flows = {@Flow(from="src",to="this")})
    public  java.nio.ByteBuffer put(java.nio.ByteBuffer src) 
    {  
		return this;
    }
}
