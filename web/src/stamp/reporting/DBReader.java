package stamp.reporting;

import java.sql.*;

public class DBReader
{
	public static void main(String args[])
	{
		String dbPath = args[0];

		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:"+dbPath);
			System.out.println("Opened database successfully");
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM FLOWS;" );
			while ( rs.next() ) {
				int id = rs.getInt("flowKey");
				String appName = rs.getString("appName");
				String sourceLabel = rs.getString("sourceLabel");
				String sinkLabel = rs.getString("sinkLabel");
				System.out.println( appName + " " + sourceLabel +" " +sinkLabel);
			}

		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
}