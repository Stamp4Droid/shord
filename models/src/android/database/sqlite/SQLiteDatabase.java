class SQLiteDatabase
{
	
    @STAMP(flows = {@Flow(from="$Database",to="@return"), @Flow(from="!Database",to="@return")})
	public static android.database.sqlite.SQLiteDatabase openDatabase(java.lang.String path, android.database.sqlite.SQLiteDatabase.CursorFactory factory, int flags) {
		return new SQLiteDatabase();
    }

    @STAMP(flows = {@Flow(from="$Database",to="@return"), @Flow(from="!Database",to="@return")})
    public static android.database.sqlite.SQLiteDatabase openDatabase(java.lang.String path, android.database.sqlite.SQLiteDatabase.CursorFactory factory, int flags, android.database.DatabaseErrorHandler errorHandler) {
		return new SQLiteDatabase();
    }

    @STAMP(flows = {@Flow(from="$Database",to="@return"), @Flow(from="!Database",to="@return")})
    public static android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.io.File file, android.database.sqlite.SQLiteDatabase.CursorFactory factory) {
		return new SQLiteDatabase();
    }

    @STAMP(flows = {@Flow(from="$Database",to="@return"), @Flow(from="!Database",to="@return")})
    public static android.database.sqlite.SQLiteDatabase openOrCreateDatabase(java.lang.String path, android.database.sqlite.SQLiteDatabase.CursorFactory factory) {
		return new SQLiteDatabase();
    }

	@STAMP(flows = {@Flow(from="values",to="!this")})
    public long insert(java.lang.String table, java.lang.String nullColumnHack, android.content.ContentValues values) {
		return 0L;
    }

	@STAMP(flows = {@Flow(from="values",to="!this")})
    public long insertOrThrow(java.lang.String table, java.lang.String nullColumnHack, android.content.ContentValues values) throws android.database.SQLException {
		return 0L;
    }

	@STAMP(flows = {@Flow(from="initialValues",to="!this")})
    public long insertWithOnConflict(java.lang.String table, java.lang.String nullColumnHack, android.content.ContentValues initialValues, int conflictAlgorithm) {
		return 0L;
    }

}


      
