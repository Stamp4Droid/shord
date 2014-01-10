package stamp.summaryreport;

import stamp.app.App;
import stamp.app.Component;
import stamp.app.IntentFilter;
import stamp.util.SHAFileChecksum;
import stamp.analyses.DynamicFeaturesAnalysis;

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
			reflection();

			writer.println("</body>");
			writer.println("</html>");
		
			writer.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}

	private void reflection()
	{
		DynamicFeaturesAnalysis dfa = new DynamicFeaturesAnalysis();
		dfa.measureReflection();

		writer.println("<h2>Reflection</h2>");
		writer.println("<ul>");
		
		writer.println(String.format("<li><b>Code size:</b> %d</li>", dfa.stmtCount));
		writer.println(String.format("<li><b>Number of reflective method calls:</b> %d (%f%%)</li>", dfa.invokeCount, (dfa.invokeCount*100.0)/dfa.stmtCount));
		writer.println(String.format("<li><b>Number of reflective field accesses:</b> %d (%f%%)</li>", dfa.readFieldCount, (dfa.readFieldCount*100.0)/dfa.stmtCount));

		writer.println("</ul>");
	}
	
	private void basicInfo()
	{
		writer.println(String.format("<center><h1>%s</h1></center>", app.getPackageName()));
		
		String icon = app.getIconPath();
		if(icon != null)
			writer.println(String.format("<center><img src=\"file://%s\"></center>", icon));

		writer.println("<h2>Basic Info</h2>");
		writer.println("<ul>");
		writer.println(String.format("<li><b>Version:</b> %s</li>", app.getVersion()));

		String apkPath = System.getProperty("stamp.apk.path");
		String sha256;
		try{
			sha256 = SHAFileChecksum.compute(apkPath);
		}catch(Exception e){
			throw new Error(e);
		}			
		writer.println(String.format("<li><b>SHA256:</b> %s</li>", sha256));

		writer.println("</ul>");
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