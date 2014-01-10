package stamp.summaryreport;

import stamp.app.App;
import stamp.app.Component;
import stamp.app.IntentFilter;
import stamp.util.SHAFileChecksum;

import shord.project.analyses.JavaAnalysis;
import shord.program.Program;

import chord.project.Chord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.*;

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
			systemEvents();

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

		writer.println("<h2>Basic Info</h2>");
		writer.println(String.format("<b>Version:</b> %s", app.getVersion()));

		String apkPath = System.getProperty("stamp.apk.path");
		String sha256;
		try{
			sha256 = SHAFileChecksum.compute(apkPath);
		}catch(Exception e){
			throw new Error(e);
		}			
		writer.println(String.format("<b>SHA256:</b> %s", sha256));
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
	
	private void systemEvents()
	{
		Set<String> events = new HashSet();
		for(Component comp : app.components()){
			if(comp.type != Component.Type.receiver)
				continue;
			for(IntentFilter ifilter : comp.intentFilters){
				for(String action : ifilter.actions)
					events.add(action);
			}
		}
		
		writer.println("<h2>System Events</h2>");
		writer.println("<ul>");
		for(String e : events)
			writer.println(String.format("<li>%s</li>", e));
		writer.println("</ul>");
	}
}