package stamp.srcmap.sourceinfo.abstractinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import stamp.srcmap.Expr;
import stamp.srcmap.InvkMarker;
import stamp.srcmap.Marker;
import stamp.srcmap.SimpleMarker;
import stamp.srcmap.sourceinfo.MethodInfo;
import stamp.srcmap.sourceinfo.javainfo.JavaClassInfo;

/**
 * @author Saswat Anand
 * @author Osbert Bastani 
 */
public class AbstractMethodInfo implements MethodInfo {
	private Map<Integer,List<Marker>> lineToMarkers = new HashMap();

	protected AbstractMethodInfo(String chordMethSig, String className, File f) {
		//System.out.println(">>begin building methodinfo for "+chordMethSig);
		Element classElem = classElem(className, f);
		readInfo(classElem, chordMethSig);
		//System.out.println(">>end building methodinfo for "+chordMethSig);
	}

	private static Element classElem(String className, File file) {
		try {
			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(file);
			Element root = doc.getDocumentElement();
			//System.out.println("matching " + className + " " + file);
			NodeList classElems = root.getElementsByTagName("class");
			int numClasses = classElems.getLength();
			for(int i = 0; i < numClasses; i++){
				Element classElem = (Element) classElems.item(i);
				String sig = classElem.getAttribute("chordsig");
				if(sig.equals(className))
					return classElem;
			}
		} catch(Exception e){
			throw new Error(e);
		}
		return null;
	}
	
	private Marker invokeExpr(Element ieElem) {
		InvkMarker ie = new InvkMarker(Integer.parseInt(ieElem.getAttribute("line")),
				ieElem.getAttribute("chordsig"),
				ieElem.getAttribute("type"),
				JavaClassInfo.getChildrenByTagName(ieElem, "expr").get(0).getFirstChild().getNodeValue());

		List<Element> params = JavaClassInfo.getChildrenByTagName(ieElem, "param");
		//System.out.println("params.size() = " + params.size());
		for(Element paramElem : params){
			ie.addParam(this.expr(paramElem));
		}
		//System.out.println(ie.toString());
		
		return ie;
	}
		
	protected void readInfo(Element classElem, String chordMethSig) {
		for(Element methElem : JavaClassInfo.getChildrenByTagName(classElem, "method")){
			if(chordMethSig.equals(methElem.getAttribute("chordsig"))){
				process(methElem);
				return;
			}
		}
	}
	
	private void process(Element methElem) {
		for (Node child = methElem.getFirstChild(); child != null; child = child.getNextSibling()) {
			if(child.getNodeType() != Node.ELEMENT_NODE) 
				continue;
			String nodeName = child.getNodeName();
			Element elem = (Element) child;

			if(nodeName.equals("invkexpr") || nodeName.equals("newexpr")){
				addMarker(invokeExpr(elem));
				continue;
			}
			
			if(nodeName.equals("marker")){
				addMarker(readSimpleMarker(elem));
				continue;
			}
		}
	}
	
	public List<Marker> markers(int line, String markerType, String sig) {
		List<Marker> ms = lineToMarkers.get(line);
		if(ms == null)
			return Collections.EMPTY_LIST;
		List<Marker> ret = new ArrayList();
		if(sig == null){
			for(Marker m : ms){
				if(m.chordSig() == null && m.markerType().equals(markerType))
					ret.add(m);
			}
		} else{
			for(Marker m : ms){
				if(sig.equals(m.chordSig()) && m.markerType().equals(markerType))
					ret.add(m);
			}
		}
		return ret;
	}

	private void addMarker(Marker marker) {
		int line = marker.line();
		List<Marker> ms = this.lineToMarkers.get(line);
		if(ms == null){
			ms = new ArrayList();
			lineToMarkers.put(line, ms);
		}
		ms.add(marker);
	}

	private Marker readSimpleMarker(Element elem) {
		String chordSig = null;
		if(elem.hasAttribute("chordsig")) {
			chordSig = elem.getAttribute("chordsig");
		}
		Element operandElem = JavaClassInfo.getChildrenByTagName(elem, "operand").get(0);
		SimpleMarker marker = new SimpleMarker(Integer.parseInt(elem.getAttribute("line")),
				chordSig,
				elem.getAttribute("type"),
				expr(operandElem));
		return marker;
	}

	private Expr expr(Element elem) {
		String type = elem.getAttribute("type");
		if(type.equals("")) {
			type = null;
		}
		Expr expr = new Expr(Integer.parseInt(elem.getAttribute("start")),
				Integer.parseInt(elem.getAttribute("length")),
				Integer.parseInt(elem.getAttribute("line")),
				JavaClassInfo.getChildrenByTagName(elem, "expr").get(0).getFirstChild().getNodeValue(),
				type);
		return expr;
	}
}
