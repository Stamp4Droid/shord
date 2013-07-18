class Geocodeer
{
	@STAMP(flows={@Flow(from="latitude",to="@return"),@Flow(from="longitude",to="@return")})
	public  java.util.List<android.location.Address> getFromLocation(double latitude, double longitude, int maxResults) throws java.io.IOException 
	{ 
		java.util.List<android.location.Address> ret = new java.util.ArrayList<android.location.Address>();
		ret.add(new Address((java.util.Locale) null));
		return ret;
	} 
}