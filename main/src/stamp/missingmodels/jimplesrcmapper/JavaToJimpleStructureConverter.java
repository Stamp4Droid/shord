package stamp.missingmodels.jimplesrcmapper;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import soot.SootClass;
import soot.SootMethod;
import stamp.missingmodels.jimplesrcmapper.CodeStructureInfo.CodeStructure;
import stamp.missingmodels.util.xml.XMLObject;
import stamp.srcmap.sourceinfo.javasource.JavaSourceInfo;

public class JavaToJimpleStructureConverter {
	private JavaSourceInfo sourceInfo;
	private CodeStructureInfo codeInfo;
	
	public JavaToJimpleStructureConverter(JavaSourceInfo sourceInfo, CodeStructureInfo codeInfo) {
		this.sourceInfo = sourceInfo;
		this.codeInfo = codeInfo;
	}
	
	public void convert(XMLObject object) {
		/** Handle classes (linenum) */
		// xml classes
		List<XMLObject> xmlClasses = object.getAllChildrenByName("class");
		/*
		for(XMLObject classObject : xmlClasses) {
			System.out.println("CLASS OBJECT CHORDSIG: " + classObject.getAttribute("chordsig"));
		}
		*/
		for(SootClass cl : this.codeInfo.getClasses()) {
			// chord sig
			String chordSig = StringEscapeUtils.escapeXml(this.sourceInfo.srcClassName(cl));
			//System.out.println("PROCESSING CLASS: " + chordSig);
			// class info
			CodeStructure classInfo = this.codeInfo.getClassInfo(cl);
			// do the replacement
			for(XMLObject classObject : xmlClasses) {
				if(classObject.getAttribute("chordsig").equals(chordSig)) {
					System.out.println("PROCESSING CLASS: " + chordSig);
					classObject.putAttribute("line", Integer.toString(classInfo.declarationLineNum));
				}
			}
		}
		
		/** Handle methods (linenum) */
		// xml methods
		List<XMLObject> xmlMethods = object.getAllChildrenByName("method");
		/*
		for(XMLObject methodObject : xmlMethods) {
			System.out.println("METHOD OBJECT CHORDSIG: " + methodObject.getAttribute("chordsig"));
		}
		*/
		for(SootMethod m : this.codeInfo.getMethods()) {
			// chord sig
			String chordSig = StringEscapeUtils.escapeXml(this.sourceInfo.chordSigFor(m));
			//System.out.println("PROCESSING METHOD: " + chordSig);
			// method info
			CodeStructure methodInfo = this.codeInfo.getMethodInfo(m);
			for(XMLObject methodObject : xmlMethods) {
				if(methodObject.getAttribute("chordsig").equals(chordSig)) {
					System.out.println("PROCESSING METHOD: " + chordSig);
					methodObject.putAttribute("line", Integer.toString(methodInfo.declarationLineNum));
					methodObject.putAttribute("startpos", Integer.toString(methodInfo.declarationStart));
					methodObject.putAttribute("endpos", Integer.toString(methodInfo.declarationEnd));
					methodObject.putAttribute("bodyStartLn", Integer.toString(methodInfo.bodyStartLineNum));
					methodObject.putAttribute("bodyEndLn", Integer.toString(methodInfo.bodyEndLineNum));
				}
			}
		}
	}
}
