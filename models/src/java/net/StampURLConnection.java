package java.net;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;

public class StampURLConnection extends HttpURLConnection
{
	@STAMP(flows={@Flow(from="url",to="this")})
	public StampURLConnection(URL url)
	{
		super(url);
	}

	public void connect() throws java.io.IOException
	{
	}

	public void disconnect()
	{
	}

	public boolean usingProxy()
	{
		return false;
	}

	@STAMP(flows={@Flow(from="this",to="@return")})
	public  java.io.OutputStream getOutputStream() throws java.io.IOException 
	{ 
		return new java.io.StampOutputStream();
	}
}