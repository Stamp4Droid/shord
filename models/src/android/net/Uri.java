class Uri
{
	@STAMP(flows={@Flow(from="uriString",to="@return")})
	public static  android.net.Uri parse(java.lang.String uriString) 
	{ 
		return new StampUri(uriString);
	}
}
