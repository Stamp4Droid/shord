class URLEncoder
{
	@STAMP(flows = { @Flow(from = "s", to = "@return") })
	public static  java.lang.String encode(java.lang.String s) {
	    java.lang.String r = s;
	    r = new String();
	    return r; }

	@STAMP(flows = { @Flow(from = "s", to = "@return") })
	public static  java.lang.String encode(java.lang.String s, java.lang.String charsetName) throws java.io.UnsupportedEncodingException { return new String(); }

}
