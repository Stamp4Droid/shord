public class ItemizedOverlay
{
  @STAMP(flows={@Flow(from="param1",to="this")})
  public  ItemizedOverlay(android.graphics.drawable.Drawable param1)
  {
	  this.onTap(null, null);
	  this.onSnapToItem(0, 0, null, null);
	  this.onTrackballEvent(null, null);
	  this.onKeyUp(0, null, null);
	  this.onTouchEvent(null, null);
	  this.onTap(0);
  }

  @STAMP(flows={@Flow(from="param1",to="@return")})
  protected static android.graphics.drawable.Drawable boundCenterBottom(android.graphics.drawable.Drawable param1) { return param1; }

  @STAMP(flows={@Flow(from="param1",to="@return")})
  protected static android.graphics.drawable.Drawable boundCenter(android.graphics.drawable.Drawable param1) { return param1; }

  @STAMP(flows={@Flow(from="this",to="@return")})
  public com.google.android.maps.GeoPoint getCenter() { return new GeoPoint(0, 0); }

  @STAMP(flows={@Flow(from="this",to="param1")})
  public void draw(android.graphics.Canvas param1, com.google.android.maps.MapView param2, boolean param3) {}

  @STAMP(flows={@Flow(from="this",to="@return")})
  public int getLatSpanE6() { return 13000000; }

  @STAMP(flows={@Flow(from="this",to="@return")})
  public int getLonSpanE6() { return 13000000; }

  @STAMP(flows={@Flow(from="this",to="@return")})
  public com.google.android.maps.OverlayItem getFocus() { return null; }

  @STAMP(flows={@Flow(from="this",to="@return")})
  public Item getItem(int param1)  { return null; }

  @STAMP(flows={@Flow(from="this",to="@return")})
  public Item nextFocus(boolean param1)  { return null; }

  public void setOnFocusChangeListener(final com.google.android.maps.ItemizedOverlay.OnFocusChangeListener listener){
		listener.onFocusChanged(ItemizedOverlay.this, null);
  }
}
