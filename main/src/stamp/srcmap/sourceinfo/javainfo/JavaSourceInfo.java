package stamp.srcmap.sourceinfo.javainfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.Tag;
import stamp.srcmap.sourceinfo.ClassInfo;
import stamp.srcmap.sourceinfo.MethodInfo;
import stamp.srcmap.sourceinfo.RegisterMap;
import stamp.srcmap.sourceinfo.abstractinfo.AbstractSourceInfo;

/**
 * @author Saswat Anand 
 */
public class JavaSourceInfo extends AbstractSourceInfo {
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
		
	public Map<String,List<String>> allAliasSigs(SootClass klass) {
		ClassInfo ci = classInfo(klass);
		return ci == null ? Collections.EMPTY_MAP : ci.allAliasSigs();		
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
			ci = JavaClassInfo.get(klassName, file);
			if(ci == null)
				return null;
			classInfos.put(klassName, ci);
		}
		return ci;
	}

	public String srcClassName(SootClass declKlass) {
		String srcClsName = this.anonymousClassMap.srcClassName(declKlass);
		if(srcClsName != null)
			return srcClsName;
		else
			return declKlass.getName();
	}

	protected MethodInfo methodInfo(SootMethod meth) {
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
}
