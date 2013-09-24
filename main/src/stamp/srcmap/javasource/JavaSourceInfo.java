package stamp.srcmap.javasource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shord.program.Program;
import soot.AbstractJasminClass;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.Tag;
import stamp.srcmap.ClassInfo;
import stamp.srcmap.InvkMarker;
import stamp.srcmap.Marker;
import stamp.srcmap.MethodInfo;
import stamp.srcmap.RegisterMap;
import stamp.srcmap.SourceInfo;

/**
 * @author Saswat Anand 
 */
public class JavaSourceInfo implements SourceInfo {
	private File frameworkSrcDir;
	private List<File> srcMapDirs = new ArrayList<File>();
	private Map<String,ClassInfo> classInfos = new HashMap<String,ClassInfo>();
	
	private AnonymousClassMap anonymousClassMap;

	public JavaSourceInfo() {
		File frameworkDir = new File(System.getProperty("stamp.framework.dir"));

		frameworkSrcDir = new File(frameworkDir, "gen");
		if(!frameworkSrcDir.exists())
			throw new RuntimeException("Framework dir " + frameworkSrcDir + " does not exist");
 		
		File frameworkSrcMapDir = new File(frameworkDir, "srcmap");
	 	if(!frameworkSrcMapDir.exists())
			throw new RuntimeException("Framework dir " + frameworkSrcMapDir + " does not exist");
		srcMapDirs.add(frameworkSrcMapDir);

		String outDir = System.getProperty("stamp.out.dir");
		File appSrcMapDir = new File(outDir+"/srcmap");
		if(!appSrcMapDir.exists())
			throw new RuntimeException("Framework dir " + appSrcMapDir + " does not exist");
		srcMapDirs.add(appSrcMapDir);
		
		this.anonymousClassMap = new AnonymousClassMap(this);
	}
	
	public String filePath(SootClass klass) {		
		for(Tag tag : klass.getTags()){
			if(tag instanceof SourceFileTag){
				String fileName = ((SourceFileTag) tag).getSourceFile();
				return klass.getPackageName().replace('.','/')+"/"+fileName;
			}
		}
		return null;
	}

	public String javaLocStr(Stmt stmt) {		
		SootMethod method = Program.containerMethod(stmt);
		SootClass klass = method.getDeclaringClass();
		for(Tag tag : klass.getTags()){
			if(tag instanceof SourceFileTag){
				String fileName = ((SourceFileTag) tag).getSourceFile();
				int lineNum = stmtLineNum(stmt);
				if(lineNum > 0)
					return fileName+":"+lineNum;
				else
					return fileName;
			}
		}
		return null;
	}

    public String srcClassName(Stmt stmt) {
		SootMethod method = Program.containerMethod(stmt);
		SootClass klass = method.getDeclaringClass();
        return srcClassName(klass);
    }
	
	public boolean isFrameworkClass(SootClass klass) {
		String srcFileName = filePath(klass);
		if(srcFileName == null){
			//System.out.println("srcFileName null for "+klass);
			return true;
		}
		boolean result = new File(frameworkSrcDir, srcFileName).exists();
		//System.out.println("isFrameworkClass " + srcFileName + " " + klass + " " + result);
		return result;
	}
	
	public int classLineNum(SootClass klass) {
		ClassInfo ci = classInfo(klass);
		return ci == null ? -1 : ci.lineNum();
	}

    public int methodLineNum(SootMethod meth) {
		ClassInfo ci = classInfo(meth.getDeclaringClass());
		return ci == null ? -1 : ci.lineNum(chordSigFor(meth));
    }

	public int stmtLineNum(Stmt s) {
		for(Tag tag : s.getTags()){
			if(tag instanceof SourceLineNumberTag){
				return ((SourceLineNumberTag) tag).getLineNumber();
			} else if(tag instanceof LineNumberTag){
				return ((LineNumberTag) tag).getLineNumber();
			}
		}
		return 0;
	}

    public RegisterMap buildRegMapFor(SootMethod meth) {
		return new JavaRegisterMap(this, meth, methodInfo(meth));
    }	

	public String chordSigFor(SootMethod m) {
		String className = srcClassName(m.getDeclaringClass());
		return m.getName()
			+":"
			+AbstractJasminClass.jasminDescriptorOf(m.makeRef())
			+"@"+className;
	}

	public String chordSigFor(SootField f) {
		String className = srcClassName(f.getDeclaringClass());
		return f.getName()
			+":"
			+AbstractJasminClass.jasminDescriptorOf(f.getType())
			+"@"+className;
	}
	
	public String chordTypeFor(Type type) {
		return AbstractJasminClass.jasminDescriptorOf(type);
	}
	
	public boolean hasSrcFile(String srcFileName) {
		File file = srcMapFile(srcFileName);
		return file != null;
	}
		
	public Map<String,List<String>> allAliasSigs(SootClass klass) {
		ClassInfo ci = classInfo(klass);
		return ci == null ? Collections.EMPTY_MAP : ci.allAliasSigs();		
	}	
	
	private ClassInfo classInfo(SootClass klass) {
		String klassName = srcClassName(klass);
		String srcFileName = filePath(klass);
		ClassInfo ci = classInfos.get(klassName);
		if(ci == null){
			File file = srcMapFile(srcFileName);
			//System.out.println("klass: "+klass+" srcFileName: "+srcFileName + " " + (file == null));
			if(file == null)
				return null;
			ci = JavaClassInfo.get(klassName, file);
			if(ci == null)
				return null;
			classInfos.put(klassName, ci);
		}
		return ci;
	}

	private String srcClassName(SootClass declKlass) {
		String srcClsName = this.anonymousClassMap.srcClassName(declKlass);
		if(srcClsName != null)
			return srcClsName;
		else
			return declKlass.getName();
	}

	private MethodInfo methodInfo(SootMethod meth) {
		String methodSig = chordSigFor(meth);
		ClassInfo ci = classInfo(meth.getDeclaringClass());
		//System.out.println("methodInfo " + methodSig + " " + (ci == null));
		MethodInfo mi = ci == null ? null : ci.methodInfo(methodSig);
		return mi;
	}
	
	public File srcMapFile(String srcFileName) {
		if(srcFileName != null){
			for(File dir : srcMapDirs){
				File f = new File(dir, srcFileName.replace(".java", ".xml"));
				if(f.exists())
					return f;
			}
		}
		return null;
	}
	
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
}
