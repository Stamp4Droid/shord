package stamp.srcmap.sourceinfo.javainfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import stamp.srcmap.sourceinfo.abstractinfo.AbstractClassInfo;

/**
 * @author Saswat Anand 
 */
public class JavaClassInfo extends AbstractClassInfo {
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
		if(!super.hasMethodInfoFor(chordSig)) {
			return null;
		}
		return new JavaMethodInfo(chordSig, className, file);
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

	protected static Element classElem(String className, File file) {
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

			//line num
			String lineNumStr = methElem.getAttribute("line");
			int lineNum = !lineNumStr.equals("") ? Integer.parseInt(lineNumStr) : -1;
			
			BasicMethodInfo bmi = new BasicMethodInfo(lineNum);
			
			for(Element aliasElem : getChildrenByTagName(methElem, "alias")){
				String aliasSig = aliasElem.getFirstChild().getNodeValue();
				bmi.addAliasChordSig(aliasSig);
			}

			super.addMethodInfo(chordSig, bmi);
		}
	}
}