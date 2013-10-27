public class Browser
{
    public static final  void deleteFromHistory(android.content.ContentResolver cr, java.lang.String url) { throw new RuntimeException("Stub!"); }

    public static final  void clearSearches(android.content.ContentResolver cr) { throw new RuntimeException("Stub!"); }
    
    public static final  void deleteHistoryTimeFrame(android.content.ContentResolver cr, long begin, long end) { throw new RuntimeException("Stub!"); }
    
    public static final  void truncateHistory(android.content.ContentResolver cr) { throw new RuntimeException("Stub!"); }

    public static final  void clearHistory(android.content.ContentResolver cr) { throw new RuntimeException("Stub!"); }

    public static final  void updateVisitedHistory(android.content.ContentResolver cr, java.lang.String url, boolean real) { throw new RuntimeException("Stub!"); }

	static {
		BOOKMARKS_URI = taintedBookmarks();
		SEARCHES_URI = taintedSearches();
	}

	private static android.net.Uri taintedBookmarks()
	{
		return new android.net.StampUri("");
	}

	private static android.net.Uri taintedSearches()
	{
		return new android.net.StampUri("");
	}


	public static final  android.database.Cursor getAllBookmarks(android.content.ContentResolver cr) throws java.lang.IllegalStateException 
	{ 
		return new android.test.mock.MockCursor();
	}

	public static final  android.database.Cursor getAllVisitedUrls(android.content.ContentResolver cr) throws java.lang.IllegalStateException 
	{ 
		return new android.test.mock.MockCursor();
	}
	
}