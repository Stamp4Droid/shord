class URL
{
	@STAMP(flows={@Flow(from="spec",to="!INTERNET")})
	public  URL(java.lang.String spec) throws java.net.MalformedURLException 
	{
	}

	@STAMP(flows={@Flow(from="spec",to="!INTERNET")})
	public  URL(java.net.URL context, java.lang.String spec) throws java.net.MalformedURLException 
	{ 
	}

	@STAMP(flows={@Flow(from="spec",to="!INTERNET")})
	public  URL(java.net.URL context, java.lang.String spec, java.net.URLStreamHandler handler) throws java.net.MalformedURLException 
	{ 
	}

	@STAMP(flows={@Flow(from="file",to="!INTERNET")})
	public  URL(java.lang.String protocol, java.lang.String host, java.lang.String file) throws java.net.MalformedURLException 
	{
	}

	@STAMP(flows={@Flow(from="file",to="!INTERNET")})
	public  URL(java.lang.String protocol, java.lang.String host, int port, java.lang.String file) throws java.net.MalformedURLException 
	{ 
	}

	@STAMP(flows={@Flow(from="file",to="!INTERNET")})
	public  URL(java.lang.String protocol, java.lang.String host, int port, java.lang.String file, java.net.URLStreamHandler handler) throws java.net.MalformedURLException 
	{ 
	}

}