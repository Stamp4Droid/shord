class StringBuilder
{
	@STAMP(flows = {@Flow(from="seq",to="this")})
    public  StringBuilder(java.lang.CharSequence seq) { }

	@STAMP(flows = {@Flow(from="str",to="this")})
    public StringBuilder(java.lang.String str) { }

	@STAMP(flows = {@Flow(from="b",to="this")})
    public  java.lang.StringBuilder append(boolean b) {  }

	@STAMP(flows = {@Flow(from="c",to="this")})
    public  java.lang.StringBuilder append(char c) {  }

	@STAMP(flows = {@Flow(from="i",to="this")})
    public  java.lang.StringBuilder append(int i) {  }

	@STAMP(flows = {@Flow(from="l",to="this")})
    public  java.lang.StringBuilder append(long l) {  }

	@STAMP(flows = {@Flow(from="f",to="this")})
    public  java.lang.StringBuilder append(float f) {  }

	@STAMP(flows = {@Flow(from="d",to="this")})
    public  java.lang.StringBuilder append(double d) {  }

	@STAMP(flows = {@Flow(from="obj",to="this")})
    public  java.lang.StringBuilder append(java.lang.Object obj) {  }

	@STAMP(flows = {@Flow(from="str",to="this")})
    public  java.lang.StringBuilder append(java.lang.String str) {  }

	@STAMP(flows = {@Flow(from="sb",to="this")})
    public  java.lang.StringBuilder append(java.lang.StringBuffer sb) {  }

	@STAMP(flows = {@Flow(from="chars",to="this")})
    public  java.lang.StringBuilder append(char[] chars) {  }

	@STAMP(flows = {@Flow(from="str",to="this")})
    public  java.lang.StringBuilder append(char[] str, int offset, int len) {  }

	@STAMP(flows = {@Flow(from="csq",to="this")})
    public  java.lang.StringBuilder append(java.lang.CharSequence csq) {  }

	@STAMP(flows = {@Flow(from="csq",to="this")})
    public  java.lang.StringBuilder append(java.lang.CharSequence csq, int start, int end) 	{  }

	@STAMP(flows = {@Flow(from="codePoint",to="this")})
    public  java.lang.StringBuilder appendCodePoint(int codePoint) {  }

	@STAMP(flows = {@Flow(from="b",to="this")})
    public  java.lang.StringBuilder insert(int offset, boolean b) {  }

	@STAMP(flows = {@Flow(from="c",to="this")})
    public  java.lang.StringBuilder insert(int offset, char c) {  }

	@STAMP(flows = {@Flow(from="i",to="this")})
    public  java.lang.StringBuilder insert(int offset, int i) {  }

	@STAMP(flows = {@Flow(from="l",to="this")})
    public  java.lang.StringBuilder insert(int offset, long l) {  }

	@STAMP(flows = {@Flow(from="f",to="this")})
    public  java.lang.StringBuilder insert(int offset, float f) {  }

	@STAMP(flows = {@Flow(from="d",to="this")})
    public  java.lang.StringBuilder insert(int offset, double d) {  }

	@STAMP(flows = {@Flow(from="obj",to="this")})
    public  java.lang.StringBuilder insert(int offset, java.lang.Object obj) {  }

	@STAMP(flows = {@Flow(from="str",to="this")})
    public  java.lang.StringBuilder insert(int offset, java.lang.String str) {  }

	@STAMP(flows = {@Flow(from="ch",to="this")})
    public  java.lang.StringBuilder insert(int offset, char[] ch) {  }

	@STAMP(flows = {@Flow(from="str",to="this")})
    public  java.lang.StringBuilder insert(int offset, char[] str, int strOffset, int strLen) {  }

	@STAMP(flows = {@Flow(from="s",to="this")})
    public  java.lang.StringBuilder insert(int offset, java.lang.CharSequence s) {  }

	@STAMP(flows = {@Flow(from="s",to="this")})
    public  java.lang.StringBuilder insert(int offset, java.lang.CharSequence s, int start, int end) {  }

	@STAMP(flows = {@Flow(from="str",to="this")})
    public  java.lang.StringBuilder replace(int start, int end, java.lang.String str) {  }

    public  java.lang.StringBuilder reverse() {  }

	@STAMP(flows = {@Flow(from="this",to="@return")})
    public  java.lang.String toString() { return new String(); }

    public java.lang.StringBuilder delete(int start, int end) {
		
    }

    public java.lang.StringBuilder deleteCharAt(int index) {
		
    }

}
