package stamp.srcmap.sourceinfo;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import shord.program.Program;
import soot.AbstractJasminClass;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Stmt;
import soot.tagkit.SourceFileTag;
import soot.tagkit.Tag;
import stamp.srcmap.InvkMarker;
import stamp.srcmap.Marker;

/**
 * @author Saswat Anand
 * @author Osbert Bastani
 */
public abstract class AbstractSourceInfo implements SourceInfo {
	private Map<String,ClassInfo> classInfos = new HashMap<String,ClassInfo>();
	
	protected MethodInfo methodInfo(SootMethod meth) {
		String methodSig = chordSigFor(meth);
		ClassInfo ci = classInfo(meth.getDeclaringClass());
		//System.out.println("methodInfo " + methodSig + " " + (ci == null));
		MethodInfo mi = ci == null ? null : ci.methodInfo(methodSig);
		return mi;
	}
	
	@Override
	public String filePath(SootClass klass) {		
		for(Tag tag : klass.getTags()) {
			if(tag instanceof SourceFileTag){
				String fileName = ((SourceFileTag) tag).getSourceFile();
				return klass.getPackageName().replace('.','/')+"/"+fileName;
			}
		}
		return null;
	}

	@Override
	public String javaLocStr(Stmt stmt) {	
		SootMethod method = Program.containerMethod(stmt);
		SootClass klass = method.getDeclaringClass();
		for(Tag tag : klass.getTags()) {
			if(tag instanceof SourceFileTag) {
				String fileName = ((SourceFileTag)tag).getSourceFile();
				int lineNum = stmtLineNum(stmt);
				if(lineNum > 0)
					return fileName+":"+lineNum;
				else
					return fileName;
			}
		}
		return null;
	}

	@Override
	public String srcClassName(Stmt stmt) {
		SootMethod method = Program.containerMethod(stmt);
		SootClass klass = method.getDeclaringClass();
        return srcClassName(klass);
	}

	@Override
	public String chordSigFor(SootMethod m) {
		String className = srcClassName(m.getDeclaringClass());
		return m.getName()
			+":"
			+AbstractJasminClass.jasminDescriptorOf(m.makeRef())
			+"@"+className;
	}

	@Override
	public String chordSigFor(SootField f) {
		String className = srcClassName(f.getDeclaringClass());
		return f.getName()
			+":"
			+AbstractJasminClass.jasminDescriptorOf(f.getType())
			+"@"+className;
	}

	@Override
	public String chordTypeFor(Type type) {
		return AbstractJasminClass.jasminDescriptorOf(type);
	}

	@Override
	public boolean hasSrcFile(String srcFileName) {
		File file = srcMapFile(srcFileName);
		return file != null;
	}

	@Override
	public Map<String,List<String>> allAliasSigs(SootClass klass) {
		ClassInfo ci = classInfo(klass);
		return ci == null ? Collections.EMPTY_MAP : ci.allAliasSigs();		
	}	

	@Override
	public String srcInvkExprFor(Stmt invkQuad) {
		SootMethod caller = Program.containerMethod(invkQuad);
		MethodInfo mi = methodInfo(caller);
		if(mi == null)
			return null;
		int lineNum = stmtLineNum(invkQuad);
		SootMethod callee = invkQuad.getInvokeExpr().getMethod();
		String calleeSig = chordSigFor(callee);

		Marker marker = null;
		List<Marker> markers = mi.markers(lineNum, "invoke", calleeSig);
		if(markers == null)
			return null;
		for(Marker m : markers){
				//System.out.println("** " + marker);
			if(marker == null)
				marker = m;
			else{
				//at least two matching markers
				System.out.println("Multiple markers");
				return null;
			}
		}
		if(marker == null)
			return null;
		return ((InvkMarker) marker).text();
	}

	@Override
	public String srcClassName(SootClass declKlass) {
		return null;
	}

	@Override
	public int classLineNum(SootClass klass) {
		ClassInfo ci = classInfo(klass);
		return ci == null ? -1 : ci.lineNum();
	}

	@Override
	public int methodLineNum(SootMethod meth) {
		ClassInfo ci = classInfo(meth.getDeclaringClass());
		return ci == null ? -1 : ci.lineNum(chordSigFor(meth));
	}
	
	protected ClassInfo classInfo(SootClass klass) {
		String klassName = srcClassName(klass);
		String srcFileName = filePath(klass);
		ClassInfo ci = classInfos.get(klassName);
		if(ci == null){
			File file = srcMapFile(srcFileName);
			//System.out.println("klass: "+klass+" srcFileName: "+srcFileName + " " + (file == null));
			if(file == null)
				return null;
			ci = this.getClassInfo(klassName, file);
			if(ci == null)
				return null;
			classInfos.put(klassName, ci);
		}
		return ci;
	}

	private DefaultClassInfo getClassInfo(String className, File f) {
		Element classElem = classElem(className, f);
		if(classElem == null)
			return null;
		return new DefaultClassInfo(className, f, classElem);
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
}
