class URI
{
	public java.lang.String f550;
	public java.lang.String f233;
	@STAMP(flows={@Flow(from="spec",to="this")})
	public  URI(java.lang.String spec) throws java.net.URISyntaxException 
	{ 
	}

	@STAMP(flows={@Flow(from="fragment",to="this")})
    public URI(java.lang.String scheme, java.lang.String schemeSpecificPart, java.lang.String fragment) throws java.net.URISyntaxException {
    }

	@STAMP(flows={@Flow(from="fragment",to="this"), @Flow(from="path",to="this")})
    public URI(java.lang.String scheme, java.lang.String host, java.lang.String path, java.lang.String fragment) throws java.net.URISyntaxException {
    }

	@STAMP(flows={@Flow(from="fragment",to="this"), @Flow(from="path",to="this"), @Flow(from="authority",to="this"), @Flow(from="query",to="this")})
    public URI(java.lang.String scheme, java.lang.String authority, java.lang.String path, java.lang.String query, java.lang.String fragment) throws java.net.URISyntaxException {
    }

	@STAMP(flows={@Flow(from="this",to="@return")})
    public java.lang.String toASCIIString() {
		java.lang.String r = null;
		r = new java.lang.String();
		r = (java.lang.String)((java.net.URI)this).f233;
		return (java.lang.String)r;
    }

	@STAMP(flows={@Flow(from="this",to="@return")})
    public java.lang.String toString() {
		java.lang.String r = null;
		r = new java.lang.String();
		r = (java.lang.String)((java.net.URI)this).f233;
		return (java.lang.String)r;
    }

	@STAMP(flows={@Flow(from="p0",to="@return")})
    public static java.net.URI create(java.lang.String p0) {
		java.net.URI r = null;
		try{
		    r = new java.net.URI(null);
		    r = new URI(p0);
		    ((java.net.URI)r).f233 = (java.lang.String)p0;
		    return r;
		}catch(URISyntaxException e){
		    return null;
		}
    }

	@STAMP(flows={@Flow(from="this",to="@return")})
    public java.net.URL toURL() throws java.net.MalformedURLException {
		return new URL((String) null);
    }

	public java.net.URI normalize() {
		java.net.URI r = null;
		r = (java.net.URI)this;
		return (java.net.URI)r;
	}
	public java.lang.String getPath() {
		java.lang.String r = null;
		r = new java.lang.String();
		r = (java.lang.String)((java.net.URI)this).f233;
		return (java.lang.String)r;
	}
	public java.net.URI relativize(java.net.URI p0) {
		java.net.URI r = null;
		try {
		    r = new java.net.URI(null);
		    ((java.net.URI)r).f550 = (java.lang.String)((java.net.URI)p0).f233;
		    return (java.net.URI)r;
		} catch(Exception e) {
		    return null;
		}
	}
	public java.net.URI parseServerAuthority() {
		java.net.URI r = null;
		r = (java.net.URI)this;
		return (java.net.URI)r;
	}
	public java.lang.String getRawSchemeSpecificPart() {
		java.lang.String r = null;
		r = new java.lang.String();
		r = (java.lang.String)((java.net.URI)this).f233;
		return (java.lang.String)r;
	}
	public java.lang.String getSchemeSpecificPart() {
		java.lang.String r = null;
		r = new java.lang.String();
		r = (java.lang.String)((java.net.URI)this).f233;
		return (java.lang.String)r;
	}
	public java.lang.String getRawPath() {
		java.lang.String r = null;
		r = new java.lang.String();
		r = (java.lang.String)((java.net.URI)this).f233;
		r = (java.lang.String)((java.net.URI)this).f550;
		return (java.lang.String)r;
	}
}
