class System
{
	@STAMP(flows={@Flow(from="src",to="dst")})
	public static void arraycopy(java.lang.Object src, int srcPos, java.lang.Object dst, int dstPos, int length)
	{
	}
	public static java.lang.String setProperty(java.lang.String p0, java.lang.String p1) {
		java.lang.String r = null;
		r = new java.lang.String();
		((java.lang.String)r).f571 = (java.lang.String)p1;
		return (java.lang.String)r;
	}
	public static java.lang.String getProperty(java.lang.String p0) {
		java.lang.String r = null;
		r = (java.lang.String)((java.lang.String)p0).f571;
		return (java.lang.String)r;
	}
	public static java.lang.String getProperty(java.lang.String p0, java.lang.String p1) {
		java.lang.String r = null;
		r = (java.lang.String)((java.lang.String)p0).f571;
		r = (java.lang.String)p1;
		r = (java.lang.String)((java.lang.String)p1).f571;
		return (java.lang.String)r;
	}
}
