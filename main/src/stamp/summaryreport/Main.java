package stamp.summaryreport;

import stamp.app.App;
import stamp.app.Component;
import stamp.app.IntentFilter;
import stamp.util.SHAFileChecksum;
import stamp.analyses.DynamicFeaturesAnalysis;
import stamp.analyses.StringAnalysis;

import shord.project.analyses.JavaAnalysis;
import shord.program.Program;

import chord.project.Chord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			File reportDir = new File(outDir, "report");
			reportDir.mkdir();

			writer = new PrintWriter(new FileWriter(new File(reportDir, "report.html")));
			writer.println("<!DOCTYPE html>");
			writer.println("<html>");
			writer.println("<head>");
			writer.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
			writer.println("<link rel=\"stylesheet\" media=\"all\" href=\"http://netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css\">");
			writer.println("</head>");
			writer.println("<body>");

			writer.println("<div class=\"container-fluid\">");
			writer.println("<div class=\"row-fluid\">");

            writer.println("<div class=\"col-md-2\"></div>");

            writer.println("<div class=\"col-md-8\">");
			basicInfo();
			permissions();
			systemEvents();
			strings();
			reflection();
			writer.println("</div>");

			writer.println("<div class=\"col-md-2\"></div>");

			writer.println("</div>");
			writer.println("</div>");

			writer.println("<script src=\"https://code.jquery.com/jquery.js\"></script>");
			writer.println("<script src=\"http://netdna.bootstrapcdn.com/bootstrap/3.0.3/js/bootstrap.min.js\"></script>");
			writer.println("</body>");
			writer.println("</html>");
		
			writer.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}

	private void strings()
	{
		StringAnalysis sa = new StringAnalysis();
		sa.analyze();

		List<String> urls = new ArrayList();
		List<String> uris = new ArrayList();
		
		IPAddressValidator ipv = new IPAddressValidator();
		URIValidator uriv = new URIValidator();

		for(String s : sa.scs){
			if(s.startsWith("http://") || s.startsWith("https://") || s.startsWith("ftp://") || ipv.validate(s))
				urls.add(s);
			else if(uriv.validate(s))
				uris.add(s);
		}

		Collections.sort(urls);
		Collections.sort(uris);

		startPanel("URLs and IP Addresses");

		writer.println("<ul>");
		for(String s : urls)
			writer.println(String.format("<li>%s</li>", s));
		writer.println("</ul>");
		
		endPanel();

		startPanel("URIs");
		writer.println("<ul>");
		for(String s : uris)
			writer.println(String.format("<li>%s</li>", s));
		writer.println("</ul>");

		endPanel();
	}


	private void reflection()
	{
		DynamicFeaturesAnalysis dfa = new DynamicFeaturesAnalysis();
		dfa.measureReflection();

		startPanel("Reflection");
		writer.println("<ul>");
		
		//writer.println(String.format("<li><b>Code size:</b> %d</li>", dfa.stmtCount));
		writer.println(String.format("<li><b>Number of reflective method calls:</b> %f%%</li>", (dfa.invokeCount*100.0)/dfa.stmtCount));
		writer.println(String.format("<li><b>Number of reflective field accesses:</b> %f%%</li>", (dfa.readFieldCount*100.0)/dfa.stmtCount));

		writer.println("</ul>");
		endPanel();
	}
	
	private void basicInfo()
	{
		writer.println(String.format("<center><h1>%s</h1></center>", app.getPackageName()));
		
		String icon = app.getIconPath();
		if(icon != null)
			writer.println(String.format("<center><img src=\"file://%s\"></center>", icon));

		startPanel("Basic Info");
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
		endPanel();
	}
	
	private void permissions()
	{
		startPanel("Permissions");
		writer.println("<ul class=\"list-group\">");
		for(String perm : app.permissions())
			writer.println(String.format("<li class=\"list-group-item\">%s</li>", perm));
		writer.println("</ul>");
		endPanel();
	}
	
	private void systemEvents()
	{
		Map<String,Set<Integer>> events = new HashMap();

		for(Component comp : app.components()){
			if(comp.type != Component.Type.receiver)
				continue;
			for(IntentFilter ifilter : comp.intentFilters){
				int priority = ifilter.getPriority();
				for(String action : ifilter.actions){
					Set<Integer> ps = events.get(action);
					if(ps == null){
						ps = new HashSet();
						events.put(action, ps);
					}
					if(priority > 0)
						ps.add(priority);
				}
			}
		}
		
		startPanel("System Events");
		writer.println("<table class=\"table table-striped .table-condensed\">");
		//writer.println("<tr><th>Event name</th><th>Priority</th></tr>");
		//writer.println("<tr><th>Event name</th><th>Priority</th></tr>");
		for(Map.Entry<String,Set<Integer>> e : events.entrySet()){
			String event = e.getKey();
			Set<Integer> ps = e.getValue();

			String pr;
			if(ps.size() > 0){
				StringBuilder builder = new StringBuilder();
				boolean first = true;
				for(Integer p : ps){
					if(!first)
						builder.append(", ");
					else
						first = false;
					builder.append(p);
				}
				pr = builder.toString();
			} else
				pr = "default";
			
			//writer.println(String.format("<tr><td>%s</td><td>%s</td></tr>", event, pr));
			writer.println(String.format("<tr><td>%s <span class=\"badge\">%s</span></td></tr>", event, pr));
		}
		writer.println("</table>");
		endPanel();
	}
	
	private void startPanel(String heading)
	{
		writer.println("<div class=\"panel panel-default\">"+
					   "<div class=\"panel-heading\">"+
					   "<h3 class=\"panel-title\">"+heading+"</h3>"+
					   "</div>"+
					   "<div class=\"panel-body\">");
	}
	
	private void endPanel()
	{
		writer.println("</div></div>");
	}
}


class IPAddressValidator
{ 
    private Pattern pattern;
 
    private static final String IPADDRESS_PATTERN = 
		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
 
    public IPAddressValidator(){
		pattern = Pattern.compile(IPADDRESS_PATTERN);
    }
 
    public boolean validate(final String ip){  
		return pattern.matcher(ip).matches();        
    }
}

class URIValidator
{ 
    private Pattern pattern;
 
    private static final String URI_PATTERN = "[a-zA-Z]*://.*";
 
    public URIValidator(){
		pattern = Pattern.compile(URI_PATTERN);
    }
 
    public boolean validate(final String ip){  
		return pattern.matcher(ip).matches();        
    }
}