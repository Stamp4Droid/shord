package stamp.reporting;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import java.sql.*;

@WebServlet(name="DBReader", urlPatterns={"/dbreader"})
public class DBReader extends HttpServlet
{
	public DBReader() 
	{
		super();
	}

	/**
	 * GET requests expected include:
	 *   > Request for list of known srcs or sinks
	 *  
	 */
	protected void doGet (HttpServletRequest request,
		HttpServletResponse response) 
	throws ServletException, IOException
	{
		String dbPath = request.getParameter("dbpath");
		System.out.println("dbPath: "+dbPath);
		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:"+dbPath);
			System.out.println("Opened database successfully");
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT appName FROM flows;" );
			StringBuilder builder = new StringBuilder("[");
			boolean first = true;
			while (rs.next()) {
				//int id = rs.getInt("flowKey");
				String appName = rs.getString("appName");
				System.out.println(appName);
				//String sourceLabel = rs.getString("sourceLabel");
				//String sinkLabel = rs.getString("sinkLabel");
				//System.out.println( appName + " " + sourceLabel +" " +sinkLabel);
				//writer.println(appName + " " + sourceLabel +" " +sinkLabel);
				if(first)
					first = false;
				else
					builder.append(", ");
				builder.append("{\"app\": \""+appName+"\", \"flows\": []"+"}");
			}
			builder.append("]");
			System.out.println(builder.toString());
			PrintWriter writer = response.getWriter();
			writer.println(builder.toString());
			writer.flush();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
}