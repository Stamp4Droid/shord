package stamp.reporting;

import java.util.*;
import java.io.*;

import javax.xml.*;
import javax.xml.xpath.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.codec.binary.Base64;

import edu.stanford.droidrecord.logreader.CoverageReport;
import edu.stanford.droidrecord.logreader.analysis.CallArgumentValueAnalysis;
import edu.stanford.droidrecord.logreader.events.info.ParamInfo;
import edu.stanford.droidrecord.logreader.events.info.MethodInfo;
import stamp.droidrecordweb.DroidrecordProxyWeb;

/* 
   @author Osbert Bastani
   @author Saswat Anand
   @author Lazaro Clapp
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
        private DroidrecordProxyWeb droidrecord;
        private String methodSig;

		public LineBegin(int position, DroidrecordProxyWeb droidrecord,
		                 String methodSig, int lineNum) {
			super(position, -8);
            this.droidrecord = droidrecord;
			this.methodSig = methodSig;
			this.lineNum = lineNum;
		}

	    @Override public String toString() {
            String coveredClass = "src-ln-not-covered";
            if(droidrecord.isAvailable() && methodSig != null) {
                CoverageReport coverage = droidrecord.getCoverage();
                if(coverage != null && coverage.isCoveredLocation(methodSig, lineNum)) {
                    coveredClass = "src-ln-covered";
                }
            }
	        return "<span id='"+lineNum+"' class='"+coveredClass+"' name='"+lineNum+"'>";
	    }
    }

    public static class LineEnd extends Insertion {
	    public LineEnd(int position) {
	        super(position, 8);
	    }

	    @Override public String toString() {
	        return "</span>";
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
			return "<span data-chordsig='"+StringEscapeUtils.escapeHtml4(chordSig)+"' data-reachable='"+reachable+"' reached='"+reached+"' reached='"+reached+"' name='MethodName'>";
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
    public static class InvocationExpressionBegin extends Insertion {
        private DroidrecordProxyWeb droidrecord;
        private String methodSig;
        private int lineNum;
        private String calleeMethodSig;
        
		public InvocationExpressionBegin(int position, DroidrecordProxyWeb droidrecord,
		                 String methodSig, int lineNum, String calleeMethodSig) {
			super(position, -8);
            this.droidrecord = droidrecord;
			this.methodSig = methodSig;
			this.lineNum = lineNum;
			this.calleeMethodSig = calleeMethodSig;
		}
		
		private String escape(String s) {
            s = s.replace("\\","\\\\");
            s = s.replace("\"","\\\"");
            s = s.replace("\'","\\\'");
            return s;
		}

	    @Override public String toString() {
	        StringBuilder paramsData = new StringBuilder();
	        String paramsDataStr = "";
            if(droidrecord.isAvailable() && methodSig != null) {
                CallArgumentValueAnalysis cava = droidrecord.getCallArgumentValueAnalysis();
                if(cava.isReady()) {
                    List<List<ParamInfo>> params = cava.queryArgumentValues(calleeMethodSig, methodSig, lineNum);
                    MethodInfo method = MethodInfo.parse(calleeMethodSig);
                    paramsData.append("{");
                    paramsData.append("\"methodName\":\"");
                    paramsData.append(method.getName());
                    paramsData.append("\",");
                    paramsData.append("\"parameterTypes\":[");
                    for(int i = 0; i < method.getArguments().size()-1; i++) {
                        paramsData.append("\"");
                        paramsData.append(method.getArguments().get(i));
                        paramsData.append("\",");
                    }
                    if(method.getArguments().size() > 0) {
                        String ptype = method.getArguments().get(method.getArguments().size()-1);
                        if(!ptype.equals("")) {
                            paramsData.append("\"");
                            paramsData.append(ptype);
                            paramsData.append("\"");
                        }
                    }
                    paramsData.append("],");
                    paramsData.append("\"parameterValues\":[");
                    boolean addComa = false;
                    Set<String> seenParamChoices = new HashSet<String>();
                    for(List<ParamInfo> l : params) {
                        String jsonInvkParams = "[";
                        for(int i = 0; i < l.size()-1; i++) {
                            jsonInvkParams += "\"" + escape(l.get(i).toSimpleString()) + "\",";
                        }
                        if(l.size() > 0) {
                            jsonInvkParams += "\"" + escape(l.get(l.size()-1).toSimpleString()) + "\"";
                        }
                        jsonInvkParams += "]";
                        if(seenParamChoices.contains(jsonInvkParams)) continue;
                        if(addComa) paramsData.append(",");
                        addComa = true;
                        paramsData.append(jsonInvkParams);
                    }
                    paramsData.append("]");
                    paramsData.append("}");
                }
                paramsDataStr = paramsData.toString();
            }
            try {
                paramsDataStr = new String(Base64.encodeBase64(paramsDataStr.getBytes("UTF-8")));
            } catch(UnsupportedEncodingException e) {
                throw new Error(e);
            }
	        String entity = "<span class='invocationExpression' ";
	        entity += "data-droidrecord-params='"+paramsDataStr;
	        entity += "' data-droidrecord-callee='"+StringEscapeUtils.escapeHtml4(calleeMethodSig);
	        entity += "' data-droidrecord-caller='"+StringEscapeUtils.escapeHtml4(methodSig);
	        entity += "' data-droidrecord-line='"+lineNum;
	        entity += "'>";
	        return entity;
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

    public static class InvocationExpressionEnd extends Insertion {
	    public InvocationExpressionEnd(int position) {
	        super(position, 8);
	    }

	    @Override public String toString() {
	        return "</span>";
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
    
    private Map<Integer, String> lineToMethodMap;
    public String getContainingMethodForLine(int lineNum)
    {
        return lineToMethodMap.get(lineNum);
    }
    
    private void populateLineToMethodMap(File srcMapFile) throws Exception {
        System.out.println("srcMapFile : " + srcMapFile);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(srcMapFile);
		XPath xpath = XPathFactory.newInstance().newXPath();
		String query = "//method";
		System.out.println("SourceProcessor.methodNodes: "+ query);
		NodeList nodes = (NodeList) xpath.evaluate(query, document, XPathConstants.NODESET);
		int n = nodes.getLength();
		Map<Integer, String> startLnToMethod = new HashMap<Integer, String>();
		Map<Integer, String> endLnToMethod = new HashMap<Integer, String>();
		int lastLn = 0;
		for(int i = 0; i < n; i++){
		    String methodSig = ((Element) nodes.item(i)).getAttribute("chordsig");
		    methodSig = DroidrecordProxyWeb.chordToSootMethodSignature(methodSig);
		    Integer bodyStartLn = new Integer(((Element) nodes.item(i)).getAttribute("bodyStartLn"));
			startLnToMethod.put(bodyStartLn, methodSig);
			Integer bodyEndLn = new Integer(((Element) nodes.item(i)).getAttribute("bodyEndLn"));
			endLnToMethod.put(bodyEndLn, methodSig);
			if(bodyEndLn > lastLn) lastLn = bodyEndLn;
		    //System.out.println(String.format("Detected method: %s [%d,%d]", methodSig, bodyStartLn, bodyEndLn));
		}
		Stack<String> currentMethodStack = new Stack<String>();
		String currentMethod = null;
		for(int i = 0; i < lastLn; i++) {
		    if(startLnToMethod.get(i) != null) {
		        currentMethodStack.push(currentMethod);
		        currentMethod = startLnToMethod.get(i);
		    }
		    if(endLnToMethod.get(i) != null) {
		        assert currentMethod.equals(endLnToMethod.get(i));
		        currentMethod = currentMethodStack.pop();
		    }
		    if(currentMethod != null) {
		        lineToMethodMap.put(i, currentMethod);
		    }
		    //System.out.println(String.format("Line: %d ===> Method: %s", i, currentMethod));
		}
    }

	public SourceProcessor(File sourceFile, DroidrecordProxyWeb droidrecord, File srcMapFile) throws Exception
	{
	    lineToMethodMap = new HashMap<Integer, String>();
	    if(srcMapFile != null) {
	        populateLineToMethodMap(srcMapFile);
	    }
	    
		// read file and add line insertions
		BufferedReader br = new BufferedReader(new FileReader(sourceFile));
		StringBuilder sourceBuilder = new StringBuilder();
		String line;
		int lineNum = 1;
		int pos=0;
		while((line = br.readLine()) != null) {
		    String methodSig = getContainingMethodForLine(lineNum);
			insertions.add(new LineBegin(pos, droidrecord, methodSig, lineNum));
			
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
			
			insertions.add(new LineEnd(pos+line.length()));
			
			sourceBuilder.append(line+"\n");
			
			lineNum++;
			pos+=line.length()+1;
		}
		br.close();
		this.source = sourceBuilder.toString();
	}
	
	public SourceProcessor(File sourceFile, DroidrecordProxyWeb droidrecord) throws Exception
	{
	    this(sourceFile, droidrecord, null);
	}

    public SourceProcessor(File sourceFile, DroidrecordProxyWeb droidrecord, 
                           File srcMapFile, File taintedInfo, File allReachableInfo,
						   File runtimeReachedMethods, String filePath) throws Exception 
	{
		this(sourceFile, droidrecord, srcMapFile);
		
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
				String methodSig = getContainingMethodForLine(invocationLineNum);
				String calleeMethodSig = DroidrecordProxyWeb.chordToSootMethodSignature(chordSig);
				insertions.add(new InvocationExpressionBegin(start, droidrecord, methodSig, invocationLineNum, calleeMethodSig));
				insertions.add(new PreInvocation(start+length, chordSig, filePath, invocationLineNum));
				insertions.add(new InvocationExpressionEnd(start+length));
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
