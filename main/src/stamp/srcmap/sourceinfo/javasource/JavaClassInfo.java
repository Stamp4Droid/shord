package stamp.srcmap.sourceinfo.javasource;

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

import stamp.srcmap.sourceinfo.ClassInfo;

/**
 * @author Saswat Anand 
 */
public class JavaClassInfo implements ClassInfo {
	private Map<String, BasicMethodInfo> methInfos = new HashMap<String, BasicMethodInfo>();	
	private String className;
	private File file;
	private int lineNum;

	public static JavaClassInfo get(String className, File f) {
		Element classElem = classElem(className, f);
		if(classElem == null)
			return null;
		return new JavaClassInfo(className, f, classElem);
	}

	private JavaClassInfo(String className, File f, Element classElem) {
		//System.out.println("reading class info " + className + " " + f);
		this.file = f;
		this.className = className;
		readInfo(classElem);
	}

	/*
	public Map<String,MethodInfo> allMethodInfos()
	{
		Map<String,MethodInfo> ret = new HashMap();
		for(String chordSig : methInfos.keySet()){
			ret.put(chordSig, new MethodInfo(chordSig, this));
		}
		return ret;
	}
	*/

	public int lineNum() {
		return lineNum;
	}

	public JavaMethodInfo methodInfo(String chordSig) {
		BasicMethodInfo bmi = methInfos.get(chordSig);
		if(bmi == null)
			return null;
		return new JavaMethodInfo(chordSig, this);
	}

	public int lineNum(String chordMethSig) { 
		BasicMethodInfo bmi = methInfos.get(chordMethSig);
		return bmi == null ? -1 : bmi.lineNum;
	}
	
	public List<String> aliasSigs(String chordMethSig) {
		List<String> ret = methInfos.get(chordMethSig).aliasChordSigs;
		return ret == null ? Collections.EMPTY_LIST : ret;
	}
	
	public Map<String,List<String>> allAliasSigs() {
		Map<String,List<String>> ret = new HashMap();
		for(Map.Entry<String,BasicMethodInfo> bmiEntry : methInfos.entrySet()){
			String chordSig = bmiEntry.getKey();
			List<String> aliases = bmiEntry.getValue().aliasChordSigs;
			if(aliases != null){
				if(ret == null)
					ret = new HashMap();
				ret.put(chordSig, aliases);
			}
		}
		return ret == null ? Collections.EMPTY_MAP : ret;
	}

	public static List<Element> getChildrenByTagName(Element parent, String name)  {
		List<Element> nodeList = new ArrayList<Element>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && 
				name.equals(child.getNodeName())) {
				nodeList.add((Element) child);
			}
		}
		return nodeList;
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
	
	protected Element classElem() {
		return classElem(className, file);
	}

	private void readInfo(Element classElem) {
		String clsLineNum = classElem.getAttribute("line");
		if(!clsLineNum.equals(""))
			lineNum = Integer.parseInt(clsLineNum);
		else
			lineNum = -1;

		for(Element methElem : getChildrenByTagName(classElem, "method")){
			String chordSig = methElem.getAttribute("chordsig");
			BasicMethodInfo bmi = new BasicMethodInfo();
			methInfos.put(chordSig, bmi);

			//line num
			String lineNum = methElem.getAttribute("line");
			bmi.lineNum = !lineNum.equals("") ? Integer.parseInt(lineNum) : -1;
			
			for(Element aliasElem : getChildrenByTagName(methElem, "alias")){
				String aliasSig = aliasElem.getFirstChild().getNodeValue();
				bmi.addAliasChordSig(aliasSig);
			}
		}
	}

	private static class BasicMethodInfo {
		int lineNum = -1;
		List<String> aliasChordSigs;
		
		void addAliasChordSig(String chordSig) {
			if(aliasChordSigs == null)
				aliasChordSigs = new ArrayList();
			aliasChordSigs.add(chordSig);
		}
	}
}