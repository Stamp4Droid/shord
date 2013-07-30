package java.io;

class FileWriter {  
	
	//@STAMP(flows = {@Flow(from="file",to="this")})
    //public FileWriter(java.io.File file, boolean append) throws java.io.IOException {}
	
	@STAMP(flows = {@Flow(from="buffer",to="!this")})
    public void write(char[] buffer, int offset, int count) throws java.io.IOException {}
	
	@STAMP(flows = {@Flow(from="str",to="!this")})
    public void write(java.lang.String str, int offset, int count) throws java.io.IOException {}	
	
}
