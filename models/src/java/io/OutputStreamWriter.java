class OutputStreamWriter{

	@STAMP(flows = {@Flow(from="buffer",to="!FILE")})
	public void write(char[] buffer, int offset, int count) throws java.io.IOException 
	{ 
		return; 
	}

	@STAMP(flows = {@Flow(from="oneChar",to="!FILE")})
	public void write(int oneChar) throws java.io.IOException {
		return; 
	}

	@STAMP(flows = {@Flow(from="str",to="!FILE")})
	public void write(java.lang.String str, int offset, int count) throws java.io.IOException {
		return; 
	}
}
