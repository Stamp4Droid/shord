package stamp.srcmap;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Type;
import soot.AbstractJasminClass;
import soot.jimple.Stmt;
import soot.tagkit.Tag;
import soot.tagkit.SourceFileTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;

import shord.analyses.ContainerTag;
import shord.program.Program;

/**
 * @author Saswat Anand 
 */
public class SourceInfo
{
	private static File frameworkSrcDir;
	private static List<File> srcMapDirs = new ArrayList();
	private static Map<String, ClassInfo> classInfos = new HashMap();

	static {
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
		
		AnonymousClassMap.init();
	}
	
	public static String filePath(SootClass klass)
	{		
		for(Tag tag : klass.getTags()){
			if(tag instanceof SourceFileTag){
				String fileName = ((SourceFileTag) tag).getSourceFile();
				return klass.getPackageName().replace('.','/')+"/"+fileName;
			}
		}
		return null;
	}

	public static String javaLocStr(Stmt stmt)
	{		
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
	
	public static boolean isFrameworkClass(SootClass klass)
	{
		String srcFileName = filePath(klass);
		if(srcFileName == null){
			//System.out.println("srcFileName null for "+klass);
			return true;
		}
		boolean result = new File(frameworkSrcDir, srcFileName).exists();
		//System.out.println("isFrameworkClass " + srcFileName + " " + klass + " " + result);
		return result;
	}
	
	public static int classLineNum(SootClass klass)
    {
		ClassInfo ci = classInfo(klass);
		return ci == null ? -1 : ci.lineNum();
	}

    public static int methodLineNum(SootMethod meth)
    {
		ClassInfo ci = classInfo(meth.getDeclaringClass());
		return ci == null ? -1 : ci.lineNum(chordSigFor(meth));
    }

	public static int stmtLineNum(Stmt s)
	{
		for(Tag tag : s.getTags()){
			if(tag instanceof SourceLineNumberTag){
				return ((SourceLineNumberTag) tag).getLineNumber();
			} else if(tag instanceof LineNumberTag){
				return ((LineNumberTag) tag).getLineNumber();
			}
		}
		return 0;
	}

    public static RegisterMap buildRegMapFor(SootMethod meth)
    {
		return new RegisterMap(meth, methodInfo(meth));
    }	

	public static String chordSigFor(SootMethod m)
	{
		String className = srcClassName(m.getDeclaringClass());
		return m.getName()
			+":"
			+AbstractJasminClass.jasminDescriptorOf(m.makeRef())
			+"@"+className;
	}

	public static String chordSigFor(SootField f)
	{
		String className = srcClassName(f.getDeclaringClass());
		return f.getName()
			+":"
			+AbstractJasminClass.jasminDescriptorOf(f.getType())
			+"@"+className;
	}
	
	public static String chordTypeFor(Type type)
	{
		return AbstractJasminClass.jasminDescriptorOf(type);
	}
	
	public static boolean hasSrcFile(String srcFileName)
	{
		File file = srcMapFile(srcFileName);
		return file != null;
	}
		
	public static Map<String,List<String>> allAliasSigs(SootClass klass)
	{
		ClassInfo ci = classInfo(klass);
		return ci == null ? Collections.EMPTY_MAP : ci.allAliasSigs();		
	}	
	
	private static ClassInfo classInfo(SootClass klass)
	{
		String klassName = srcClassName(klass);
		String srcFileName = filePath(klass);
		ClassInfo ci = classInfos.get(klassName);
		if(ci == null){
			File file = srcMapFile(srcFileName);
			//System.out.println("klass: "+klass+" srcFileName: "+srcFileName + " " + (file == null));
			if(file == null)
				return null;
			ci = ClassInfo.get(klassName, file);
			if(ci == null)
				return null;
			classInfos.put(klassName, ci);
		}
		return ci;
	}

	private static String srcClassName(SootClass declKlass)
	{
		String srcClsName = AnonymousClassMap.srcClassName(declKlass);
		if(srcClsName != null)
			return srcClsName;
		else
			return declKlass.getName();
	}

	private static MethodInfo methodInfo(SootMethod meth)
	{
		String methodSig = chordSigFor(meth);
		ClassInfo ci = classInfo(meth.getDeclaringClass());
		//System.out.println("methodInfo " + methodSig + " " + (ci == null));
		MethodInfo mi = ci == null ? null : ci.methodInfo(methodSig);
		return mi;
	}
	
	public static File srcMapFile(String srcFileName)
	{
		if(srcFileName != null){
			for(File dir : srcMapDirs){
				File f = new File(dir, srcFileName.replace(".java", ".xml"));
				if(f.exists())
					return f;
			}
		}
		return null;
	}
	
	public static String srcInvkExprFor(Stmt invkQuad)
	{
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
