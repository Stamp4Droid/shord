import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

class ContentResolver
{
    private android.content.ContentProvider provider = new android.test.mock.MockContentProvider(null);

    public final  android.database.Cursor query(android.net.Uri uri, java.lang.String[] projection, java.lang.String selection, java.lang.String[] selectionArgs, java.lang.String sortOrder)
    {
		return provider.query(uri, projection, selection, selectionArgs, sortOrder);
    }

	public final  void registerContentObserver(android.net.Uri uri, boolean notifyForDescendents, final android.database.ContentObserver observer) { 
		observer.onChange(true);
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						observer.onChange(true);
					}
				}); 		
	}
	
	@STAMP(flows={@Flow(from="uri",to="@return")})
	public final java.io.InputStream openInputStream(android.net.Uri uri) throws java.io.FileNotFoundException {
		return new java.io.StampInputStream();
    }

	@STAMP(flows={@Flow(from="uri",to="@return")})
    public final java.io.OutputStream openOutputStream(android.net.Uri uri) throws java.io.FileNotFoundException {
		return new java.io.StampOutputStream();
    }

	@STAMP(flows={@Flow(from="uri",to="@return")})
    public final java.io.OutputStream openOutputStream(android.net.Uri uri, java.lang.String mode) throws java.io.FileNotFoundException {
		return new java.io.StampOutputStream();
    }

	@STAMP(flows={@Flow(from="url",to="!Content.Delete")})
    public final int delete(android.net.Uri url, java.lang.String where, java.lang.String[] selectionArgs) {
        throw new RuntimeException("Stub!");
    }

	@STAMP(flows={@Flow(from="uri",to="!Content.Update")})
    public final int update(android.net.Uri uri, android.content.ContentValues values, java.lang.String where, java.lang.String[] selectionArgs) {
        throw new RuntimeException("Stub!");
    }

	@STAMP(flows={@Flow(from="url",to="!Content.Insert")})
    public final int bulkInsert(android.net.Uri url, android.content.ContentValues[] values) {
        throw new RuntimeException("Stub!");
    }

	@STAMP(flows={@Flow(from="url",to="!Content.Insert")})
    public final android.net.Uri insert(android.net.Uri url, android.content.ContentValues values) {
        throw new RuntimeException("Stub!");
    }


}
