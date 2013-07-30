package java.io;

class BufferedWriter {

	@STAMP(flows={@Flow(from="out",to="this")})	
    public BufferedWriter(java.io.Writer out) {

    }
	
	@STAMP(flows={@Flow(from="out",to="this")})	
    public BufferedWriter(java.io.Writer out, int size) {
	
    }
	
	@STAMP(flows={@Flow(from="cbuf",to="!this")})	
    public void write(char[] cbuf, int offset, int count) throws IOException {
	
	}
	
	@STAMP(flows={@Flow(from="oneChar",to="!this")})	
    public void write(int oneChar) throws IOException {
	
	}
	
	@STAMP(flows={@Flow(from="str",to="!this")})	
    public void write(java.lang.String str, int offset, int count) throws IOException {
	
	}
}
