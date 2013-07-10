package android.content;

class Intent
{
	//add by yu.
	private android.os.Bundle extras = new android.os.Bundle();
	
	public  Intent(java.lang.String action, android.net.Uri uri) 
	{ 
		setData(uri);
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
	public  android.os.Bundle getExtras() 
	{ 
		return extras;
	}
	
	@STAMP(flows = {@Flow(from="data",to="!INTENT")})
	public  android.content.Intent setData(android.net.Uri data) 
	{ 
		return this;
	}

	public  android.content.Intent setDataAndType(android.net.Uri data, java.lang.String type) 
	{ 
		setData(data);
		return this;
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
	public  java.util.ArrayList<java.lang.Integer> getIntegerArrayListExtra(java.lang.String name) 
	{ 
		java.util.ArrayList<java.lang.Integer> ret = new java.util.ArrayList();
		ret.add(new Integer(0));
		return ret;
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
	public  java.util.ArrayList<java.lang.String> getStringArrayListExtra(java.lang.String name) 
	{ 
		java.util.ArrayList<java.lang.String> ret = new java.util.ArrayList();
		ret.add(new String());
		return ret;
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
	public  java.util.ArrayList<java.lang.CharSequence> getCharSequenceArrayListExtra(java.lang.String name) 
	{ 
		java.util.ArrayList<java.lang.CharSequence> ret = new java.util.ArrayList();
		ret.add(new String());
		return ret;
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
	public  boolean[] getBooleanArrayExtra(java.lang.String name) 
	{ 
		return new boolean[0];
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
	public  byte[] getByteArrayExtra(java.lang.String name) 
	{ 
		return new byte[0];
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
	public  short[] getShortArrayExtra(java.lang.String name) 
	{ 
		return new short[0];
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
	public  char[] getCharArrayExtra(java.lang.String name) 
	{ 
		return new char[0];
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
	public  int[] getIntArrayExtra(java.lang.String name) 
	{ 
		return new int[0];
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
	public  long[] getLongArrayExtra(java.lang.String name) 
	{ 
		return new long[0];
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
 	public  float[] getFloatArrayExtra(java.lang.String name) 
	{ 
		return new float[0];
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
 	public  double[] getDoubleArrayExtra(java.lang.String name) 
	{ 
		return new double[0];
	}

	@STAMP(flows={@Flow(from="$getExtras",to="@return")})
 	public  java.lang.String[] getStringArrayExtra(java.lang.String name) 
	{ 
		java.lang.String[] ret = new java.lang.String[0];
		ret[0] = name;
		return ret;
	}

	@STAMP(flows = {@Flow(from="extras",to="!INTENT")})
	public  android.content.Intent putExtras(android.os.Bundle extras) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, boolean value) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, byte value) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, char value) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, short value) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, int value) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, long value) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, float value) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, double value) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, java.lang.String value) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, java.lang.CharSequence value) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, android.os.Parcelable value) 
	{ 
		return this;
	}

	@STAMP(flows = {@Flow(from="value",to="!INTENT")})
	public  android.content.Intent putExtra(java.lang.String name, android.os.Parcelable[] value) 
	{ 
		return this;
	}


}
