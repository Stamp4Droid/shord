class CharBuffer
{
    private char f;

    public CharBuffer() {}

    // @STAMP(flows = {@Flow(from="array",to="@return")})
    // public static  java.nio.CharBuffer wrap(char[] array) 
    // {
    // 	this.f = array[0];
    // 	return this;
    // }

    // @STAMP(flows = {@Flow(from="array",to="@return")})
    // public static  java.nio.CharBuffer wrap(char[] array, int start, int charCount) 
    // { 
    // 	this.f = array[0];
    // 	return this;
    // }

    // @STAMP(flows = {@Flow(from="chseq",to="@return")})
    // public static  java.nio.CharBuffer wrap(java.lang.CharSequence chseq) 
    // {
    // 	this.f = chseq.charAt(0);
    // 	return this;
    // }

    // @STAMP(flows = {@Flow(from="chseq",to="this")})
    // public static  java.nio.CharBuffer wrap(java.lang.CharSequence cs, int start, int end) 
    // { 
    // 	this.f = chseq.charAt(0);
    // 	return this;
    // }

    @STAMP(flows = {@Flow(from="this",to="@return")})
    public final  char[] array() 
    {
		char[] c = new char[0];
		c[0] = this.f;
		return c;
    }

    @STAMP(flows = {@Flow(from="this",to="@return")})
    public final  char charAt(int index) 
    {
		return this.f;
    }
	
    @STAMP(flows = {@Flow(from="this",to="dst")})
    public  java.nio.CharBuffer get(char[] dst) 
    { 
		dst[0] = this.f;
		return this;
    }

    @STAMP(flows = {@Flow(from="this",to="dst")})
    public  java.nio.CharBuffer get(char[] dst, int dstOffset, int charCount) 
    {  
		dst[0] = this.f;
		return this;
    }

    @STAMP(flows = {@Flow(from="src",to="this")})
    public final  java.nio.CharBuffer put(char[] src) 
    {  
		this.f = src[0];
		return this;
    }
	
    @STAMP(flows = {@Flow(from="src",to="this")})
    public  java.nio.CharBuffer put(char[] src, int srcOffset, int charCount) 
    {
	this.f = src[0];
	return this;
    }

    @STAMP(flows = {@Flow(from="src",to="this")})
    public  java.nio.CharBuffer put(java.nio.CharBuffer src) 
    {  
		this.f = src.charAt(0);
		return this;
    }

    @STAMP(flows = {@Flow(from="str",to="this")})
	public final  java.nio.CharBuffer put(java.lang.String str) 
    {  
		return this;
    }

    @STAMP(flows = {@Flow(from="str",to="this")})
    public  java.nio.CharBuffer put(java.lang.String str, int start, int end) 
    {
	return this;
    }

    @STAMP(flows = {@Flow(from="this",to="@return")})
    public  java.lang.String toString() 
    {
		return new String();
    }

    @STAMP(flows = {@Flow(from="c",to="this")})
    public  java.nio.CharBuffer append(char c) 
    {
		return this;
    }

    @STAMP(flows = {@Flow(from="csq",to="this")})
    public  java.nio.CharBuffer append(java.lang.CharSequence csq) 
    {
		return this;
    }

    @STAMP(flows = {@Flow(from="csq",to="this")})
    public  java.nio.CharBuffer append(java.lang.CharSequence csq, int start, int end) 
    {  
		return this;
    }

    @STAMP(flows = {@Flow(from="this",to="target")})
    public  int read(java.nio.CharBuffer target) throws java.io.IOException 
    {  
    	return 0;
    }
}