class CharArrayWriter 
{
	public java.lang.String toString() {
		java.lang.String r = null;
		r = new java.lang.String();
		return (java.lang.String)r;
	}
	public java.io.CharArrayWriter append(java.lang.CharSequence p0, int p1, int p2) {
		java.io.CharArrayWriter r = null;
		r = (java.io.CharArrayWriter)this;
		return (java.io.CharArrayWriter)r;
	}
	public java.io.CharArrayWriter append(char p0) {
		java.io.CharArrayWriter r = null;
		r = (java.io.CharArrayWriter)this;
		return (java.io.CharArrayWriter)r;
	}
	public java.io.CharArrayWriter append(java.lang.CharSequence p0) {
		java.io.CharArrayWriter r = null;
		r = (java.io.CharArrayWriter)this;
		return (java.io.CharArrayWriter)r;
	}
	@STAMP(flows = {@Flow(from="this",to="@return")})
	public char[] toCharArray() {
		char[] r = null;
		r = new char[1];
		return (char[])r;
	}
	public int size() {
		int r = 0;
		return (int)r;
	}

}
