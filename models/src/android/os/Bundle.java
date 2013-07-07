//import java.lang.CharSequence;

class Bundle
{
	@STAMP(flows={@Flow(from="this",to="@return")})
	public  java.lang.String getString(java.lang.String key) 
	{ 
		return new String();
	}
	
	@STAMP(flows={@Flow(from="this",to="@return")})
	public  int getInt(java.lang.String key) 
	{ 
		return 0;
	}

	@STAMP(flows={@Flow(from="this",to="@return")})
	public  int getInt(java.lang.String key, int defaultValue) 
	{ 
		return 0;
	}

	@STAMP(flows={@Flow(from="this",to="@return")})
    public long getLong(java.lang.String key) {
        return 0L;
    }

	@STAMP(flows={@Flow(from="this",to="@return")})
	public java.lang.Object get(java.lang.String key) {
        return new Object();
    }

	// Patrick
	@STAMP(flows={@Flow(from="this",to="@return")})
	public java.util.ArrayList<java.lang.String> getStringArrayList(java.lang.String key)
	{
		return new java.util.ArrayList<java.lang.String>();
	}

	@STAMP(flows={@Flow(from="value",to="this")})
	public  void putBoolean(java.lang.String key, boolean value) 
	{ 
	}

	@STAMP(flows={@Flow(from="value",to="this")})
	public  void putByte(java.lang.String key, byte value) 
	{ 
	}

	@STAMP(flows={@Flow(from="value",to="this")})
	public  void putChar(java.lang.String key, char value) 
	{ 
	}

	@STAMP(flows={@Flow(from="value",to="this")})
	public  void putShort(java.lang.String key, short value) 
	{
	}

	@STAMP(flows={@Flow(from="value",to="this")})
	public  void putInt(java.lang.String key, int value) 
	{
	}

	@STAMP(flows={@Flow(from="value",to="this")})
	public  void putLong(java.lang.String key, long value) 
	{
	}

	@STAMP(flows={@Flow(from="value",to="this")})
	public  void putFloat(java.lang.String key, float value) 
	{
	}

	@STAMP(flows={@Flow(from="value",to="this")})
	public  void putDouble(java.lang.String key, double value) 
	{
	}

	@STAMP(flows={@Flow(from="value",to="this")})
	public  void putString(java.lang.String key, java.lang.String value) 
	{ 
	}

}
