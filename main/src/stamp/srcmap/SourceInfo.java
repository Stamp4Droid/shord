package stamp.srcmap;

import java.io.*;
import java.util.*;

import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.tagkit.Tag;
import soot.tagkit.SourceFileTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;

import shord.analyses.ContainerTag;


/**
 * @author Saswat Anand 
 */
public class SourceInfo
{
	private static File frameworkSrcDir;
	private static File stampDir;
	private static int stampDirPathLength;

	static {
		stampDir = new File(System.getProperty("stamp.dir"));
		if(!stampDir.exists())
			throw new RuntimeException("stamp.dir " + stampDir + " does not exists");
		try{
			stampDirPathLength = stampDir.getCanonicalPath().length()+1;
		}catch(IOException e){
			throw new Error(e);
		}

		File frameworkDir = new File(stampDir, "android");
		File appDir = new File(System.getProperty("chord.work.dir"));

		frameworkSrcDir = new File(frameworkDir, "gen");
		if(!frameworkSrcDir.exists())
			throw new RuntimeException("Framework dir " + frameworkSrcDir + " does not exist");
 		
		File frameworkSrcMapDir = new File(frameworkDir, "srcmap");
	 	if(!frameworkSrcMapDir.exists())
			throw new RuntimeException("Framework dir " + frameworkSrcMapDir + " does not exist");
		SrcMapper.addSrcMapDir(frameworkSrcMapDir);

		String outDir = System.getProperty("stamp.out.dir");
		File appSrcMapDir = new File(outDir+"/srcmap");
		if(!appSrcMapDir.exists())
			throw new RuntimeException("Framework dir " + appSrcMapDir + " does not exist");
		SrcMapper.addSrcMapDir(appSrcMapDir);
		
		AnonymousClassMap.init();
	}
	
	public static String filePath(SootClass klass)
	{		
		for(Tag tag : klass.getTags()){
			if(tag instanceof SourceFileTag)
				return ((SourceFileTag) tag).getSourceFile();
		}
		return null;
	}
	
	static boolean isFrameworkClass(SootClass klass)
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
		String srcFileName = filePath(klass);
		String classSig = klass.getName();
		return SrcMapper.classLineNum(srcFileName, classSig);
	}

    public static int methodLineNum(SootMethod meth)
    {
		return SrcMapper.methodLineNum(filePath(meth.getDeclaringClass()), 
									   srcMethodSig(meth));
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

	public static SootMethod containerMethod(Stmt stmt)
	{		
		for(Tag tag : stmt.getTags()){
			if(tag instanceof ContainerTag)
				return ((ContainerTag) tag).method;
		}
		return null;
	}

	/*
	static List<jq_Method> aliasFor(SootMethod meth)
	{
		SootClass klass = meth.getDeclaringClass();
		String srcFileName = filePath(klass);
		String methSig = meth.toString();
		List<String> aliasDescs = SrcMapper.aliasSigs(srcFileName, methSig);
		if(aliasDescs.isEmpty())
			return Collections.EMPTY_LIST;
		List<jq_Method> aliases = new ArrayList();
		String mname = meth.getName().toString();
		for(String desc : aliasDescs)
			aliases.add((jq_Method) klass.getDeclaredMember(mname, desc));
		return aliases;
	}

	static void computeSyntheticToSrcMethodMap(SootClass klass, Map<jq_Method,jq_Method> syntheticToSrcMethod)
	{
		Map<jq_Method,List<jq_Method>> ret = new HashMap();
		String srcFileName = filePath(klass);
		for(Map.Entry<String,List<String>> aliasSigEntry : SrcMapper.allAliasSigs(srcFileName, klass.getName()).entrySet()){
			String chordSig = aliasSigEntry.getKey();
			List<String> aliasDescs = aliasSigEntry.getValue();
			if(aliasDescs.isEmpty())
				continue;
			int index = chordSig.indexOf(':');
			String mname = chordSig.substring(0, index);
			String mdesc = chordSig.substring(index+1, chordSig.indexOf('@'));
			jq_Method meth = (jq_Method) klass.getDeclaredMember(mname, mdesc);
			//System.out.println("meth with alias: " + meth);
			List<jq_Method> aliases = new ArrayList();
			for(String aliasDesc : aliasDescs){
				jq_Method synthMeth = (jq_Method) klass.getDeclaredMember(mname, aliasDesc);
				//System.out.println("synth meth: " + synthMeth);
				if(synthMeth == null){
					//chord seems to throw away synthetic methods that the compiler
					//to deal with covariant return type
					continue;
				}
				
				//for(jq_Method m : klass.getDeclaredInstanceMethods())
				//System.out.println(m.toString());
				//}
				assert synthMeth != null : klass + " " + mname + " " + aliasDesc;
				
				jq_Method prevBinding = syntheticToSrcMethod.put(synthMeth, meth);
				assert prevBinding == null : synthMeth + " " + prevBinding + " " + aliasDesc;
			}
		}
	}

	static boolean isSyntheticMethod(SootMethod meth)
	{
		SootClass klass = meth.getDeclaringClass();
		String srcFileName = filePath(klass);
		return srcFileName != null && (SrcMapper.methodLineNum(srcFileName, meth.toString()) < 0);
	}

	static String srcInvkExprFor(Quad invkQuad)
	{
		int lineNum = invkQuad.getLineNumber();
		jq_Method caller = invkQuad.getMethod();
		jq_Class klass = caller.getDeclaringClass();
		String srcFileName = filePath(klass);
		String callerSig = caller.toString();
		MethodInfo mi = SrcMapper.methodInfo(srcFileName, callerSig);
		jq_Method callee = Operator.Invoke.getMethod(invkQuad).getMethod();
		String calleeSig = callee.toString();
		if(mi == null)
			return null;
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
	*/
    public static RegisterMap buildRegMapFor(SootMethod meth)
    {
		MethodInfo mi = SrcMapper.methodInfo(filePath(meth.getDeclaringClass()), 
											 srcMethodSig(meth));
		RegisterMap regMap = new RegisterMap(meth, mi);
		return regMap;
    }	

	//if meth is defined in an anonymous class then it returns a different sig from the chord sig.
	//the difference is that the name of the class is changed to a name that is given (while 
	//parsing source code) to the corresponding anonymous classes
	//if meth is defined in a named class, it returns the chord sig
	private static String srcMethodSig(SootMethod meth)
	{
		String methSig = meth.getSignature();
		String srcClsName = AnonymousClassMap.srcClassName(meth.getDeclaringClass());
		if(srcClsName != null){
			methSig = "<"+srcClsName+": "+meth.getSubSignature()+">";
			//System.out.println("!! " +methSig);
		}
		return methSig;
	}

	/*
	static Map<jq_Method,RegisterMap> allRegMaps(jq_Class klass)
	{
		String srcFileName = klass.filePath();
		String classSig = klass.getName();
		Map<String,MethodInfo> methodInfos = SrcMapper.allMethodInfos(srcFileName, classSig);
		if(methodInfos == null)
			return null;
		Map<jq_Method,RegisterMap> regMaps = new HashMap();
		for(jq_Method meth : klass.getDeclaredInstanceMethods()){
			MethodInfo mi = methodInfos.get(meth.toString());
			RegisterMap regMap = new RegisterMap(meth, mi);
			regMaps.put(meth, regMap);
		}
		for(jq_Method meth : klass.getDeclaredStaticMethods()){
			MethodInfo mi = methodInfos.get(meth.toString());
			RegisterMap regMap = new RegisterMap(meth, mi);
			regMaps.put(meth, regMap);
		}
		return regMaps;
	}
	*/
}
