package java.io;


class BufferedOutputStream {

	//@STAMP(flows={@Flow(from="out",to="this")})	
    //public BufferedOutputStream(java.io.OutputStream out) {}
	
	//@STAMP(flows={@Flow(from="out",to="this")})	
    //public BufferedOutputStream(java.io.OutputStream out, int size) {}
	
	@STAMP(flows={@Flow(from="oneByte",to="!this")})	
    public synchronized void write(int oneByte) throws java.io.IOException {}
}
