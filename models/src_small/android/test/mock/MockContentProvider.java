class MockContentProvider
{

    public  android.database.Cursor query(android.net.Uri uri, java.lang.String[] projection, java.lang.String selection, java.lang.String[] selectionArgs, java.lang.String sortOrder) 
    { 
		return new MockCursor();
    }
}
