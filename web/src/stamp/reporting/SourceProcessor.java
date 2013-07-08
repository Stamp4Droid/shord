package stamp.reporting;

import java.util.*;
import java.io.*;

import javax.xml.*;
import javax.xml.xpath.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.commons.lang3.StringEscapeUtils;

/* 
   @author Osbert Bastani
   @author Saswat Anand
*/
public class SourceProcessor 
{
    public static abstract class Insertion implements Comparable<Insertion> {
		// -1: beginning, 0: middle, 1: ending
		private int position;
		private int order;
		
		public Insertion(int position) {
			this.position = position;
			this.order = 0;
		}
		
		public Insertion(int position, int order) {
			this.position = position;
			this.order = order;
		}
		
		public int getPosition() {
			return position;
		}
		
		public int getOrder() {
			return order;
		}
		
		@Override public int compareTo(Insertion i) {
			if(this.position < i.getPosition()) {
				return 1;
			} else if(this.position == i.getPosition()) {
				if(this.order < i.getOrder()) {
					return 1;
				} else if(this.order == i.getOrder()) {
					return 0;
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		}
		
		public abstract String toString();
    }
	
    public static class LineBegin extends Insertion {
		private int lineNum;

		public LineBegin(int position, int lineNum) {
			super(position, -8);
			this.lineNum = lineNum;
		}

	@Override public String toString() {
	    return "<a id='"+lineNum+"' name='"+lineNum+"'>";
	}
    }

    public static class LineEnd extends Insertion {
	public LineEnd(int position) {
	    super(position, 8);
	}

	@Override public String toString() {
	    return "</a>";
	}
    }

    public static class PreMethodName extends Insertion 
	{
		private String chordSig;
		
		public PreMethodName(int position, String chordSig, boolean isReachable) {
			super(position, 4);
			this.chordSig = chordSig;
		}
		
		@Override public String toString() {
			return "<span data-chordsig='"+StringEscapeUtils.escapeHtml4(chordSig)+"' name='PreMethodName'></span>";
			//return "<span id='PreMethodName"++"' name='PreMethodName'></span>";
		}
    }
	    
    public static class MethodNameStart extends Insertion 
	{
		private String chordSig;
		private boolean reachable;
		private boolean reached;

		public MethodNameStart(int position, String chordSig, boolean isReachable, boolean reached) {
			super(position, 6);
			this.reachable = isReachable;
			this.chordSig = chordSig;
			this.reached  = reached;
		}
		
		@Override public String toString() {
			return "<span data-chordsig='"+StringEscapeUtils.escapeHtml4(chordSig)+"' data-reachable='"+reachable+"' reached='"+reached+"' name='MethodName'>";
		}
    }


    public static class TypeRefStart extends Insertion 
	{
		private String chordSig;

		public TypeRefStart(int position, String chordSig) {
			super(position, 6);
			this.chordSig = chordSig;
		}
		
		@Override public String toString() {
			return "<span data-chordsig='"+StringEscapeUtils.escapeHtml4(chordSig)+"' name='TypeRef'>";
		}
    }

    public static class SpanEnd extends Insertion {
		public SpanEnd(int position) {
			super(position, -6);
		}
		
		@Override public String toString() {
			return "</span>";
		}
    }

    // this is actually post invocation now
    public static class PreInvocation extends Insertion {
	private String chordSig;
	private String filePath;
	private int lineNum;
	
	public PreInvocation(int position, String chordSig, String filePath, int lineNum) {
	    super(position, 4);
	    this.chordSig = chordSig;
	    this.filePath = filePath;
	    this.lineNum = lineNum;
	}

	@Override public String toString() {
	    return "<span data-chordsig='"+StringEscapeUtils.escapeHtml4(chordSig)+"' name='PreInvocation' data-filePath='"+this.filePath+"' data-lineNum='"+this.lineNum+"'></span>";
	    //return "<span id='PreInvocation"+StringEscapeUtils.escapeHTML(chordSig)+"' name='PreInvocation'></span>";
	}
    }
    
    public static class KeySpanStart extends Insertion {
	private String key;

	public KeySpanStart(int position, String key) {
	    super(position, 6);
	    this.key = key;
	}

	@Override public String toString() {
	    return "<span id='"+key+getPosition()+"' name='"+key+"'>";
	}
    }

    public static class EscapeStart extends Insertion {
	private String sequence;

	public EscapeStart(int position, String sequence) {
	    super(position, -12);
	    this.sequence = sequence;
	}

	@Override public String toString() {
	    return this.sequence;
	}
    }

    private String source;
    private List<Insertion> insertions = new ArrayList<Insertion>();

	public SourceProcessor(File sourceFile) throws Exception
	{
		// read file and add line insertions
		BufferedReader br = new BufferedReader(new FileReader(sourceFile));
		StringBuilder sourceBuilder = new StringBuilder();
		String line;
		int lineNum = 1;
		int pos=0;
		while((line = br.readLine()) != null) {
			//insertions.add(new LineBegin(pos, lineNum));
			
			for(int i=0; i<line.length(); i++) {
				switch(line.charAt(i)) {
				case '<':
					insertions.add(new EscapeStart(pos+i+1, "#60;"));
					line = line.substring(0, i) + "&" + line.substring(i+1);
					break;
				case '>':
					insertions.add(new EscapeStart(pos+i+1, "#62;"));
					line = line.substring(0, i) + "&" + line.substring(i+1);
					break;
				case '&':
					insertions.add(new EscapeStart(pos+i+1, "#38;"));
					break;
				}
			}
			
			//insertions.add(new LineEnd(pos+line.length()));
			
			sourceBuilder.append(line+"\n");
			
			lineNum++;
			pos+=line.length()+1;
		}
		br.close();
		this.source = sourceBuilder.toString();
	}

    public SourceProcessor(File sourceFile, File srcMapFile, File taintedInfo, File allReachableInfo,
						   File runtimeReachedMethods, String filePath) throws Exception 
	{
		this(sourceFile);
		
		Set<String> reachableSigs = new HashSet<String>();
		// find reachable methods defined in this source file
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(allReachableInfo);
			XPath xpath = XPathFactory.newInstance().newXPath();

			String query = "//tuple/value[@type=\"method\" and @srcFile=\""+filePath+"\"]";
			System.out.println("SourceProcessor.reachableM: "+ query);
			NodeList nodes = (NodeList) xpath.evaluate(query, document, XPathConstants.NODESET);
			int n = nodes.getLength();
			for(int i = 0; i < n; i++){
				String chordSig = ((Element) nodes.item(i)).getAttribute("chordsig");
				chordSig = StringEscapeUtils.unescapeXml(chordSig); System.out.println("reachableMethod: "+chordSig);
				reachableSigs.add(chordSig);
			}
		}

		Set<String> reachedSigs = new HashSet<String>();  
		// Find the set of reached methods from runtime observations
		if(runtimeReachedMethods != null && runtimeReachedMethods.exists()){
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(runtimeReachedMethods);
			XPath xpath = XPathFactory.newInstance().newXPath();

			String query = "//chord_sig";
			NodeList nodes = (NodeList) xpath.evaluate(query, document, XPathConstants.NODESET);
			int n = nodes.getLength();
			for(int i = 0; i < n; i++){
				Element element = (Element) nodes.item(i);
				Node node = element.getFirstChild();
				
				if(node == null)
					continue;

				String chordSig = node.getNodeValue();
				reachedSigs.add(chordSig);
			}
		}
		

		
		// add invocation insertions
		{
			//System.out.println("srcMapFile: "+srcMapFile);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(srcMapFile);
			XPath xpath = XPathFactory.newInstance().newXPath();

			//It is quite complicated to show call targets for the "new X(..)" statements
			//when X is a nested class. So disabling this feature altogether. Instead of this
			//feature the plan is to hyperlink all types such as X in the new statement to
			//the code of the respective class.
			/*
			NodeList nodes = (NodeList)xpath.evaluate("//newexpr", document, XPathConstants.NODESET);
			for(int i=0; i<nodes.getLength(); i++) {
				Element node = (Element)nodes.item(i);
				int start = Integer.valueOf(node.getAttribute("start"));
				int length = Integer.valueOf(node.getAttribute("length"));
				String chordSig = node.getAttribute("chordsig");
				int invocationLineNum = Integer.valueOf(node.getAttribute("line"));
				insertions.add(new PreInvocation(start+length, chordSig, filePath, invocationLineNum));
				}
			*/
			
			NodeList nodes = (NodeList)xpath.evaluate("//invkexpr", document, XPathConstants.NODESET);
			for(int i=0; i<nodes.getLength(); i++) {
				Element node = (Element)nodes.item(i);
				int start = Integer.valueOf(node.getAttribute("start"));
				int length = Integer.valueOf(node.getAttribute("length"));
				String chordSig = node.getAttribute("chordsig");
				int invocationLineNum = Integer.valueOf(node.getAttribute("line"));
				insertions.add(new PreInvocation(start+length, chordSig, filePath, invocationLineNum));
			}

			nodes = (NodeList)xpath.evaluate("//method", document, XPathConstants.NODESET);
			for(int i=0; i<nodes.getLength(); i++) {
				Element node = (Element)nodes.item(i);
				String chordSig = node.getAttribute("chordsig");
				if(chordSig.startsWith("<clinit>:"))
					continue;
				int start = Integer.valueOf(node.getAttribute("startpos"));
				int end = Integer.valueOf(node.getAttribute("endpos"));
				boolean reachable = reachableSigs.contains(chordSig);
				boolean reached = reachedSigs.contains(chordSig);

				insertions.add(new MethodNameStart(start, chordSig, reachable, reached));
				insertions.add(new SpanEnd(end));	
			}
			
			nodes = (NodeList)xpath.evaluate("//type", document, XPathConstants.NODESET);
			for(int i=0; i<nodes.getLength(); i++) {
				Element node = (Element)nodes.item(i);
				String chordSig = node.getAttribute("chordsig");
				int start = Integer.valueOf(node.getAttribute("start"));
				int end = Integer.valueOf(node.getAttribute("length"))+start;
				insertions.add(new TypeRefStart(start, chordSig));
				insertions.add(new SpanEnd(end));					
			}
		}
		
		// add taint insertions
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(taintedInfo);
			XPath xpath = XPathFactory.newInstance().newXPath();

			String query = "//category[@type=\"method\"]/tuple/value[@srcFile=\""+filePath+"\"]/highlight";			
			System.out.println("Tainted vars query: "+query);
			NodeList nodes = (NodeList)xpath.evaluate(query, document, XPathConstants.NODESET);
			
			for(int i=0; i<nodes.getLength(); i++) {
				Element node = (Element)nodes.item(i);
				int start = Integer.valueOf(node.getAttribute("startpos"));
				int end = start+Integer.valueOf(node.getAttribute("length"));
				String key = node.getAttribute("key");
				insertions.add(new KeySpanStart(start, key));
				insertions.add(new SpanEnd(end));
			}
		}
    }

    public String getSource() 
	{
		return this.source;
    }

    public String getSourceWithInsertions() {
	return getSourceWithAnnotations();
    }

    public String getSourceWithAnnotations() {
	String newSource = this.source;
	Collections.sort(this.insertions);
	for(Insertion i : this.insertions) {
	    if(i.getPosition() <= newSource.length()) {
		newSource = newSource.substring(0, i.getPosition()) + i.toString() + newSource.substring(i.getPosition());
	    }
	}
	return newSource;
    }

}