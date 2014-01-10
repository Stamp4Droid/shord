package stamp.summaryreport;

import stamp.app.App;

import shord.project.analyses.JavaAnalysis;
import shord.program.Program;

import chord.project.Chord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Saswat Anand
 */
@Chord(name = "summary-report")
public class Main extends JavaAnalysis
{
	private PrintWriter writer;
	private App app;

	public void run()
	{
		app = Program.g().app();
		
		try{
			String outDir = System.getProperty("stamp.out.dir");
			writer = new PrintWriter(new FileWriter(new File(outDir, "report.html")));
			writer.println("<html>");
			writer.println("<body>");
			
			basicInfo();
			permissions();
			
			writer.println("</body>");
			writer.println("</html>");
		
			writer.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}
	
	private void basicInfo()
	{
		writer.println(String.format("<center><h1>%s</h1></center>", app.getPackageName()));
		writer.println(String.format("Version: %s", app.getVersion()));
	}
	
	private void permissions()
	{
		//writer.println(String.format("<h1>%s</h1>", app.getPackageName()));
		writer.println("<h2>Permissions</h2>");
		writer.println("<ul>");
		for(String perm : app.permissions())
			writer.println(String.format("<li>%s</li>", perm));
		writer.println("</ul>");
	}
}