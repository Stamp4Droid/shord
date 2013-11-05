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

@WebServlet(name="PolicyServlet", urlPatterns={"/policyServlet"})
public class PolicyServlet extends HttpServlet 
{

	private static Logger logger = Logger.getLogger(PolicyServlet.class.getName());

	public PolicyServlet() 
	{
		super();
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
		} 
	}

	/**
	 *
	 */
	protected void doPost (HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/plain");
	}
}
