class Location
{
	@STAMP(flows={@Flow(from="$getLatitude",to="@return")})
	public  double getLatitude() { return 13.0; }
	
	@STAMP(flows={@Flow(from="$getLongitude",to="@return")})
	public  double getLongitude() { return 13.0; }

	@STAMP(flows={@Flow(from="this",to="@return")})
	public  java.lang.String toString() 
	{ 
		return new String();
	}
}
