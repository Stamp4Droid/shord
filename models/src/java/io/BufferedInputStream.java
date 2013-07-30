package java.io;

class BufferedInputStream {

	/*@STAMP(flows={@Flow(from="in",to="this")})	
    public BufferedInputStream(java.io.InputStream in) {   }

	@STAMP(flows={@Flow(from="in",to="this")})		
    public BufferedInputStream(java.io.InputStream in, int size) {}*/

    @STAMP(flows = {@Flow(from="this",to="@return")})
    private byte taintByte() { return (byte) 0; }

    public  int read(byte[] buffer, int offset, int length) throws java.io.IOException 
    { 
        buffer[0] = taintByte(); 
        return 0; 
    }

}
