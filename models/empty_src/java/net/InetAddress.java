class InetAddress
{
	@STAMP(flows={@Flow(from="this",to="@return")})
	public  byte[] getAddress() 
	{ 
		return new byte[0];
	}

	public static  java.net.InetAddress[] getAllByName(java.lang.String host) throws java.net.UnknownHostException 
	{
	    return null;
	}

	@STAMP(flows={@Flow(from="host",to="@return")})
	public static  java.net.InetAddress getByName(java.lang.String host) throws java.net.UnknownHostException 
	{ 
		return new InetAddress();
	}

	@STAMP(flows={@Flow(from="this",to="@return")})
	public  java.lang.String getHostAddress() 
	{ 
		return new String();
	}

	@STAMP(flows={@Flow(from="this",to="@return")})
	public  java.lang.String getHostName() 
	{ 
		return new String();
	}

	@STAMP(flows={@Flow(from="this",to="@return")})
    public java.lang.String toString() {
		return new String();
    }
}
