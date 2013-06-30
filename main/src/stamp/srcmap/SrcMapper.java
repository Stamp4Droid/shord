package stamp.srcmap;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;


/**
 * @author Saswat Anand 
 */
public class SrcMapper
{
	private static Map<String, ClassInfo> classInfos = new HashMap();		
	private static List<File> srcMapDirs;

	public static boolean hasSrcFile(String srcFileName)
	{
		File file = srcMapFile(srcFileName);
		return file != null;
	}

	/*
	  @param srcFileName name of the Java file
	  @param nonAnonymousClasses is filled with names of named classes
	  @param anonymousClasses is filled with (lineNum, concocted-name) for each anonoymous class
	 */
	public static void getAllDeclaredClasses(String srcFileName, Set<String> nonAnonymousClasses, Map<Integer,String> anonymousClasses)
	{
		File file = srcMapFile(srcFileName);
		assert file != null;

		try{
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
				if(classElem.getAttribute("anonymous").equals(""))
					nonAnonymousClasses.add(sig);
				else{
					int lineNum = Integer.parseInt(sig.substring(sig.lastIndexOf('#')+1));
					anonymousClasses.put(lineNum, sig);
				}

			}
		}catch(Exception e){
			throw new Error(e);
		}
	}

	public static MethodInfo methodInfo(String srcFileName, String methSig)
	{
		String classSig = methSig.substring(1, methSig.indexOf(':'));
		ClassInfo ci = classInfo(classSig, srcFileName);
		//System.out.println("methodInfo " + classSig + " " + srcFileName);
		return ci == null ? null : ci.methodInfo(methSig);
	}

	/*
	public static Map<String,MethodInfo> allMethodInfos(String srcFileName, String classSig)
	{
		ClassInfo ci = classInfo(classSig, srcFileName);
		//System.out.println("methodInfo " + classSig + " " + srcFileName);
		return ci == null ? null : ci.allMethodInfos();
	}
	*/

	public static int classLineNum(String srcFileName, String classSig)
	{
		ClassInfo ci = classInfo(classSig, srcFileName);
		return ci == null ? -1 : ci.lineNum();
	}	

	public static int methodLineNum(String srcFileName, String methSig)
	{
		String classSig = methSig.substring(methSig.indexOf("@")+1);
		ClassInfo ci = classInfo(classSig, srcFileName);
		return ci == null ? -1 : ci.lineNum(methSig);
	}

	public static List<String> aliasSigs(String srcFileName, String methSig)
	{
		String classSig = methSig.substring(methSig.indexOf("@")+1);
		ClassInfo ci = classInfo(classSig, srcFileName);
		return ci == null ? Collections.EMPTY_LIST : ci.aliasSigs(methSig);		
	}

	public static Map<String,List<String>> allAliasSigs(String srcFileName, String className)
	{
		ClassInfo ci = classInfo(className, srcFileName);
		return ci == null ? Collections.EMPTY_MAP : ci.allAliasSigs();		
	}	

	public static void addSrcMapDir(File dir)
	{
		if(srcMapDirs == null)
			srcMapDirs = new ArrayList();
		srcMapDirs.add(dir);
	}
		
	private static File srcMapFile(String srcFileName)
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

	private static ClassInfo classInfo(String klass, String srcFileName)
	{
		ClassInfo ci = classInfos.get(klass);
		if(ci == null){
			File file = srcMapFile(srcFileName);
			if(file == null)
				return null;
			ci = ClassInfo.get(klass, file);
			if(ci == null)
				return null;
			classInfos.put(klass, ci);
		}
		return ci;
	}
}