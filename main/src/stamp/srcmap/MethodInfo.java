package stamp.srcmap;

import java.util.*;
import org.w3c.dom.*;

/**
 * @author Saswat Anand 
 */
public class MethodInfo
{
	private Map<Integer,List<Marker>> lineToMarkers = new HashMap();

	MethodInfo(String chordMethSig, ClassInfo classInfo)
	{
		//System.out.println(">>begin building methodinfo for "+chordMethSig);
		Element classElem = classInfo.classElem();
		readInfo(classElem, chordMethSig);
		//System.out.println(">>end building methodinfo for "+chordMethSig);
	}
	
	private Marker invokeExpr(Element ieElem)
	{
		InvkMarker ie = new InvkMarker();
		ie.line = Integer.parseInt(ieElem.getAttribute("line"));
		ie.chordSig = ieElem.getAttribute("chordsig");		
		ie.markerType = ieElem.getAttribute("type");
		ie.text = ClassInfo.getChildrenByTagName(ieElem, "expr").get(0).getFirstChild().getNodeValue();

		List<Element> params = ClassInfo.getChildrenByTagName(ieElem, "param");
		//System.out.println("params.size() = " + params.size());
		if(params.size() > 0)
			ie.params = new ArrayList();
			
		for(Element paramElem : params){
			Expr pExpr = expr(paramElem);
			ie.params.add(pExpr);
		}
		//System.out.println(ie.toString());
		
		return ie;
	}
		
	protected void readInfo(Element classElem, String chordMethSig)
	{
		for(Element methElem : ClassInfo.getChildrenByTagName(classElem, "method")){
			if(chordMethSig.equals(methElem.getAttribute("chordsig"))){
				process(methElem);
				return;
			}
		}
	}
	
	private void process(Element methElem)
	{
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
	
	public List<Marker> markers(int line, String markerType, String sig)
	{
		List<Marker> ms = lineToMarkers.get(line);
		if(ms == null)
			return Collections.EMPTY_LIST;
		List<Marker> ret = new ArrayList();
		if(sig == null){
			for(Marker m : ms){
				if(m.chordSig == null && m.markerType.equals(markerType))
					ret.add(m);
			}
		} else{
			for(Marker m : ms){
				if(sig.equals(m.chordSig) && m.markerType.equals(markerType))
					ret.add(m);
			}
		}
		return ret;
	}

	private void addMarker(Marker marker)
	{
		int line = marker.line;
		List<Marker> ms = lineToMarkers.get(line);
		if(ms == null){
			ms = new ArrayList();
			lineToMarkers.put(line, ms);
		}
		ms.add(marker);
	}

	private Marker readSimpleMarker(Element elem)
	{
		SimpleMarker marker = new SimpleMarker();
		marker.line = Integer.parseInt(elem.getAttribute("line"));
		marker.markerType = elem.getAttribute("type");
		if(elem.hasAttribute("chordsig"))
			marker.chordSig = elem.getAttribute("chordsig");	
		Element operandElem = ClassInfo.getChildrenByTagName(elem, "operand").get(0);
		marker.operand = expr(operandElem);
		return marker;
	}

	private Expr expr(Element elem)
	{
		Expr expr = new Expr();
		expr.start = Integer.parseInt(elem.getAttribute("start"));
		expr.length = Integer.parseInt(elem.getAttribute("length"));
		expr.line = Integer.parseInt(elem.getAttribute("line"));
		expr.text = ClassInfo.getChildrenByTagName(elem, "expr").get(0).getFirstChild().getNodeValue();
		String type = elem.getAttribute("type");
		if(!type.equals(""))
			expr.type = type;
		return expr;
	}
}	
