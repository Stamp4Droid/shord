package stamp.srcmap;

import java.io.*;
import java.util.*;
 
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

public class Main 
{ 
	private static String[] classpathEntries;
	private static String[] srcpathEntries;
	private static File srcMapDir;

	public static void process(String srcRootPath, File javaFile) throws IOException
	{
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
		parser.setCompilerOptions(options);

		parser.setEnvironment(classpathEntries, srcpathEntries, null, true);

		String canonicalPath = javaFile.getCanonicalPath();
		System.out.println(canonicalPath);
		parser.setUnitName(canonicalPath);		
		parser.setSource(toCharArray(canonicalPath));

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		String relSrcFilePath = canonicalPath.substring(srcRootPath.length()+1);
		File infoFile = new File(srcMapDir, relSrcFilePath.replace(".java", ".xml"));
		infoFile.getParentFile().mkdirs();

		ChordAdapter visitor = new ChordAdapter(cu, infoFile);
		try{
			cu.accept(visitor);
			visitor.finish();
		}catch(Exception e){
			System.out.println("Failed to generate srcmap file for "+relSrcFilePath);
			infoFile.delete();
		}
	}

	public static char[] toCharArray(String filePath) throws IOException
	{
		File file = new File(filePath);
		int length = (int) file.length();
		char[] array = new char[length+1];
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int offset = 0;
		while(true){
			int count = reader.read(array, offset, length-offset+1);
			if(count == -1)
				break;
			offset += count;
		}
		reader.close();
		char[] ret = new char[offset];
		System.arraycopy(array, 0, ret, 0, offset);
		return ret;
	}
 
 
    public Main(File javaFile, File infoFile) throws IOException {
	ASTParser parser = ASTParser.newParser(AST.JLS4);
	parser.setResolveBindings(true);
	parser.setKind(ASTParser.K_COMPILATION_UNIT);

	Map options = JavaCore.getOptions();
	options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
	options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
	options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
	parser.setCompilerOptions(options);

	parser.setEnvironment(classpathEntries, srcpathEntries, null, true);

	String canonicalPath = javaFile.getCanonicalPath();
	System.out.println(canonicalPath);
	parser.setUnitName(canonicalPath);
	parser.setSource(toCharArray(canonicalPath));

	CompilationUnit cu = (CompilationUnit) parser.createAST(null);

	infoFile.getParentFile().mkdirs();
	ChordAdapter visitor = new ChordAdapter(cu, infoFile);
	cu.accept(visitor);
	visitor.finish();
    }

	/*
	   args[0] - ":" separated directories containing Java source code
	   args[1] - ":" separated jars files (third-party libs, android.jar)
	   args[2] - path of the srcmap directory
	*/
	public static void main(String[] args) throws Exception 
	{
		String srcPath = args[0];
		String[] paths = srcPath.split(":");
		srcpathEntries = new String[paths.length];
		int i = 0;
		for(String sp : paths){
			srcpathEntries[i++] = new File(sp).getCanonicalPath();
		}

		String classPath = args[1];
		classpathEntries = classPath.split(":");

		srcMapDir = new File(args[2]); //System.out.println("DEBUG "+ args[2]);
		srcMapDir.mkdirs();

		for(String p : srcpathEntries)
			System.out.println("srcpath: " + p);
		for(String p : classpathEntries)
			System.out.println("classpath: " + p);
		
		if(args.length > 3){
			//fourth arg is for testing purpose only
			File f = new File(args[3]);
			if(!f.getName().endsWith(".java"))
				throw new RuntimeException("expected a Java file. Got " + f);
			process(srcpathEntries[0], f);
		} else {
			try{
				for(String srcRootPath : srcpathEntries)
					processDir(srcRootPath, new File(srcRootPath));
			}catch(Exception e){
				e.printStackTrace();
				throw new Error(e);
			}
		}
	}	

	private static void processDir(String srcRootPath, File dir) throws Exception
	{
		//System.out.println("** " + dir);
        for(File f : dir.listFiles()){
            if(f.isDirectory()){
                processDir(srcRootPath, f);
            }
            else{
				String name = f.getName();
				if(name.endsWith(".java") && name.indexOf('#') < 0)
					process(srcRootPath, f);
			}
		}
	}

}
