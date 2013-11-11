package stamp.submitting;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.sql.*;
import java.util.Set;
import java.util.HashSet;
import java.io.StringReader;

@WebServlet(name="PolicyServlet", urlPatterns={"/policyServlet"})
public class PolicyServlet extends HttpServlet 
{

	private static Logger logger = Logger.getLogger(PolicyServlet.class.getName());

	public PolicyServlet() 
	{
		super();
	}


	protected String select()
	{
		Connection c = null;
		Statement stmt = null;
		try {
			String path = getServletContext().getRealPath("/");
			path += "../../../stamp_output/";
			System.out.println ("PATHIS "+path);
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:"+path+"policy.db");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT DISTINCT policyName FROM policies;" );
			Set<String> policyNames = new HashSet<String>();
			while ( rs.next() ) {
				String policyName = rs.getString("policyName");
				policyNames.add(policyName);
				System.out.println( "POLICYNAME = " + policyName );
			}
			rs.close();
			stmt.close();
			c.close();
			String result = "";
			for (String s : policyNames) {
				result += s+'\n';
			}
			return result;
		} catch ( Exception e ) {
			System.out.println("FAILED database open");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return "";
	}

	protected String policy(String policyName) 
	{
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY WHERE policyName="+policyName );
			while ( rs.next() ) {
				int id = rs.getInt("id");
				String  name = rs.getString("name");
				int age  = rs.getInt("age");
				String  address = rs.getString("address");
				float salary = rs.getFloat("salary");
				System.out.println( "ID = " + id );
				System.out.println( "NAME = " + name );
				System.out.println( "AGE = " + age );
				System.out.println( "ADDRESS = " + address );
				System.out.println( "SALARY = " + salary );
				System.out.println();
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println("Operation done successfully");
	}

	/**
	 * GET requests expected include:
	 *  (1) Request for list of known srcs or sinks
	 */
	protected void doGet (HttpServletRequest request,
		HttpServletResponse response) 
	throws ServletException, IOException
	{
		String path = getServletContext().getRealPath("/scripts/");
		if (request.getParameter("annot") != null) {
			String filename = "";
			if (request.getParameter("annot").equals("Sources")) {
				filename = path+"/srcClass.xml";
			} else if (request.getParameter("annot").equals("Sinks")) {
				filename = path+"/sinkClass.xml";
			} else {
				return;
			}
			FileInputStream in = new FileInputStream(filename);
			OutputStream out = response.getOutputStream();
			byte[] buffer = new byte[4096];
			int length;
			while ((length = in.read(buffer)) > 0){
				out.write(buffer, 0, length);
			}
			in.close();
			out.flush();

		} else if (request.getParameter("policies") != null) {
			if (request.getParameter("policies").equals("all")) {
				response.setContentType("text/plain");
				OutputStream out = response.getOutputStream();
				char[] buffer = new char[4096];
				int length;
				StringReader in = new StringReader(select());
				while ((length = in.read(buffer)) > 0) {
					out.write(new String(buffer).getBytes(), 0, length);
				}
				in.close();
				out.flush();
			} 
		} else if (request.getParameter("policyName") != null) {
			String policyName = request.getParameter("policyName");
		}
	}

	/**
	 *
	 */
	protected void doPost (HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/plain");
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM policies;" );
			while ( rs.next() ) {
				int id = rs.getInt("id");
				String  name = rs.getString("name");
				int age  = rs.getInt("age");
				String  address = rs.getString("address");
				float salary = rs.getFloat("salary");
				System.out.println( "ID = " + id );
				System.out.println( "NAME = " + name );
				System.out.println( "AGE = " + age );
				System.out.println( "ADDRESS = " + address );
				System.out.println( "SALARY = " + salary );
				System.out.println();
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println("Operation done successfully");
	}
}
