import edu.stanford.stamp.annotation.Inline;

class MyLocationOverlay
{
	@STAMP(flows={@Flow(from="$LOCATION",to="@return")})
	public com.google.android.maps.GeoPoint getMyLocation()
	{
		return new GeoPoint(0,0);
	}

    @STAMP(flows={@Flow(from="$LOCATION",to="@return")})
	public android.location.Location getLastFix()
	{
        return new android.location.Location((String) null);
	}

	@Inline
	public synchronized boolean runOnFirstFix(final java.lang.Runnable param1) {
		param1.run();
		return true;
    }
}
