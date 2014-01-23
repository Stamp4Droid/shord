package stamp.summaryreport;

import shord.analyses.AllocNode;
import shord.analyses.StringConstNode;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

import chord.util.tuple.object.Trio;

import java.util.*;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class HttpParamsReport
{
	private final Map<Stmt,Map<Integer,Set<String>>> data = new HashMap();
	private final Map<SootMethod,Set<Stmt>> methToCallsites = new HashMap();
	private final Main main;

	HttpParamsReport(Main main)
	{
		this.main = main;
	}

	public void generate()
	{
		final ProgramRel rel = (ProgramRel) ClassicProject.g().getTrgt("httpParam");
		rel.load();

		Iterable<Trio<Unit,Integer,AllocNode>> it = rel.getAry3ValTuples();
		for(Trio<Unit,Integer,AllocNode> trio : it){
			Stmt stmt = (Stmt) trio.val0;
			int paramIndex = trio.val1;
			AllocNode an = trio.val2;

			if(!(an instanceof StringConstNode))
				continue;
			
			String arg = ((StringConstNode) an).value;

			SootMethod callee = stmt.getInvokeExpr().getMethod();
			Set<Stmt> callsites = methToCallsites.get(callee);
			if(callsites == null){
				callsites = new HashSet();
				methToCallsites.put(callee, callsites);
			}
			callsites.add(stmt);

			Map<Integer,Set<String>> d = data.get(stmt);
			if(d == null){
				d = new HashMap();
				data.put(stmt, d);
			}
			
			Set<String> s = d.get(paramIndex);
			if(s == null){
				s = new HashSet();
				d.put(paramIndex, s);
			}
			
			s.add(arg);
			System.out.println("XYZ "+stmt + " "+paramIndex+" "+arg);
		}

		rel.close();
		
		writeReport();
	}
	

	private void writeReport()
	{
		main.startPanel("HTTP Parameters");
		main.println("<div class=\"list-group\">");

		for(Map.Entry<SootMethod,Set<Stmt>> e : methToCallsites.entrySet()){
			SootMethod apiMethod = e.getKey();
			Set<Stmt> callsites = e.getValue();
			main.println("<a href=\"#\" class=\"list-group-item\">");
			main.println(String.format("<h4 class=\"list-group-item-heading\">%s</h4>", escapeHtml4(apiMethod.getSignature())));
			main.println("<p class=\"list-group-item-text\">");
			
			main.println("<ul>");
			for(Stmt callsite : callsites){
				Map<Integer,Set<String>> args = data.get(callsite);
				List<Integer> paramIndices = new ArrayList(args.keySet());
				Collections.sort(paramIndices);

				StringBuilder sb = new StringBuilder();
				boolean outerFirst = true;
				for(Integer paramIndex : paramIndices){
					Set<String> as = args.get(paramIndex);
					boolean first = true;
					sb.append("[");
					for(String a : as){
						if(!first)
							sb.append(", ");
						else
							first = false;
						sb.append(String.format("\"%s\"", escapeHtml4(a)));
					}
					sb.append("]");
					if(!outerFirst)
						sb.append(", ");
					else
						outerFirst = false;
				}
				
				main.println(String.format("<li>%s</li>", sb.toString()));
			}
			main.println("</ul>");

			main.println("</p>");
			main.println("</a>");
		}
		main.println("</div>");
		main.endPanel();
	}

		
		
		
		
		
		
		

}