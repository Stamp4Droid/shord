package stamp.srcmap.sourceinfo.abstractinfo;

import java.io.File;
import java.util.List;
import java.util.Map;

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
import stamp.srcmap.sourceinfo.ClassInfo;
import stamp.srcmap.sourceinfo.MethodInfo;
import stamp.srcmap.sourceinfo.SourceInfo;

/**
 * @author Saswat Anand
 * @author Osbert Bastani
 */
public abstract class AbstractSourceInfo implements SourceInfo {
	protected abstract ClassInfo classInfo(SootClass klass);

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
	public Map<String, List<String>> allAliasSigs(SootClass klass) {
		// TODO Auto-generated method stub
		return null;
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
}
