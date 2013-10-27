class ExifInterface
{
	public  java.lang.String getAttribute(java.lang.String tag) { 
		return new String();
	}
	
	public  double getAttributeDouble(java.lang.String tag, double defaultValue) { 
		return 0.0;
	}

	public  int getAttributeInt(java.lang.String tag, int defaultValue) { 
		return 0;
	}

	public  double getAltitude(double defaultValue) { 
		return 0.0;
	}
	
	public  boolean getLatLong(float[] output) { 
		output[0] = taintedFloat();
		return true;
	}
	
	private static float taintedFloat(){
		return 0.0f;
	}

	public  byte[] getThumbnail() { 
		return new byte[0];
	}
}