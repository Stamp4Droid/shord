package stamp.summaryreport;

import stamp.app.App;
import stamp.app.Component;
import stamp.app.IntentFilter;
import stamp.app.PersonalNamespaceContext;
import stamp.util.SHAFileChecksum;
import stamp.analyses.DynamicFeaturesAnalysis;
import stamp.analyses.StringAnalysis;

import shord.project.analyses.JavaAnalysis;
import shord.program.Program;

import chord.project.Chord;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;

import soot.SootClass;

/**
 * @author Saswat Anand
 */
@Chord(name = "summary-report")
public class Main extends JavaAnalysis
{
	private PrintWriter writer;
	App app;

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

			writer.println("<style>");
			writer.println(".badge-important{background-color:#b94a48;}");
			writer.println("</style>");

			writer.println("</head>");
			writer.println("<body>");

			writer.println("<div class=\"container-fluid\">");
			writer.println("<div class=\"row-fluid\">");

            writer.println("<div class=\"col-md-3\"></div>");

            writer.println("<div class=\"col-md-6\">");
			basicInfo();
			permissions();
			systemEvents();

			new FlowsReport(this).generate();

			new ExportReport(this).generate();

			adLib();			
			strings();
			reflection();
			writer.println("</div>");

			writer.println("<div class=\"col-md-3\"></div>");

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

	private void adLib() throws IOException
	{
		Set<String> pkgs = new HashSet();
		for(SootClass klass : Program.g().scene().getClasses()){
			String pkg = klass.getPackageName();
			pkgs.add(pkg);
		}

		startPanel("Advertisement Network");
		writer.println("<table class=\"table table-striped table-condensed\">"); 
		
		File adFile = new File(System.getProperty("stamp.dir"), "assets/adnetwork.txt");
		BufferedReader reader = new BufferedReader(new FileReader(adFile));
		String line;
		while((line = reader.readLine()) != null){
			String[] tokens = line.split(" ");
			String adPkg = tokens[5];
			String adName = tokens[1];
			for(String p : pkgs){
				if(p.startsWith(adPkg)){
					writer.println(String.format("<tr><td>%s</td><td>%s</td></tr>", adName, adPkg));
					break;
				}
			}
		}
		reader.close();
		
		writer.println("</table>");
		endPanel();
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
			if(s.startsWith("http://") || s.startsWith("https://") || s.startsWith("ftp://") || ipv.validate(s)){
				if(!s.equals("http://") && !s.equals("https://") && !s.equals("ftp://"))
				   urls.add(s);
			}
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
		writer.println("<div class=\"jumbotron\">");
		
		String icon = app.getIconPath();
		if(icon != null)
			writer.println(String.format("<img src=\"file://%s\">", icon));

		writer.println(String.format("<h2>%s</h2>", app.getPackageName()));
		writer.println("</div>");


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
		File manifestFile = new File(System.getProperty("stamp.dir"), "assets/AndroidManifest.xml");

		try{
			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(manifestFile);
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new PersonalNamespaceContext());
			
			startPanel("Permissions");
			writer.println("<table class=\"table table-striped table-condensed\">");
			for(String perm : app.permissions()){
				String query = "/manifest/permission[@name=\""+perm+"\"]";
				//System.out.println(query);
				Node node = (Node)
					xpath.evaluate(query, document, XPathConstants.NODE);
				Node attr = node.getAttributes().getNamedItem("android:protectionLevel");
				String level = "";
				if(attr != null){
					level = attr.getNodeValue();
					if(level.equals("dangerous"))
						level = "<span class=\"label label-danger\">"+level+"</span>";
					else if(level.equals("normal"))
						level = "<span class=\"label label-success\">"+level+"</span>";
					else 
						level = "<span class=\"badge\">"+level+"</span>";
				}
				
				writer.println(String.format("<tr><td>%s</td><td>%s</td></tr>", perm, level));
			}
			writer.println("</table>");
			endPanel();
		} catch(Exception e){
			throw new Error(e);
		}
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
				pr = "<span class=\"label label-danger\">"+builder.toString()+"</span>";
				
			} else
				pr = "<span class=\"label label-success\">"+"default"+"</span>";
			
			//writer.println(String.format("<tr><td>%s</td><td>%s</td></tr>", event, pr));
			writer.println(String.format("<tr><td>%s</td><td>%s</td></tr>", event, pr));
		}
		writer.println("</table>");
		endPanel();
	}
	
	void startPanel(String heading)
	{
		startPanel(heading, "info");
	}

	void startPanel(String heading, String panelType)
	{
		writer.println(String.format("<div class=\"panel panel-%s\">",panelType)+
					   "<div class=\"panel-heading\">"+
					   "<h3 class=\"panel-title\">"+heading+"</h3>"+
					   "</div>"+
					   "<div class=\"panel-body\">");
	}
	
	void endPanel()
	{
		writer.println("</div></div>");
	}
	
	void println(String s)
	{
		writer.println(s);
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