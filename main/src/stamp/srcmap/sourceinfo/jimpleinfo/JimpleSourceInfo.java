package stamp.srcmap.sourceinfo.jimpleinfo;

import java.io.File;

import soot.SootClass;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.Tag;
import stamp.srcmap.sourceinfo.abstractinfo.AbstractSourceInfo;

/**
 * @author Saswat Anand 
 */
public class JimpleSourceInfo extends AbstractSourceInfo {
	private File frameworkSrcDir;
	private File srcMapDir;;

	public JimpleSourceInfo() {
		File frameworkDir = new File(System.getProperty("stamp.framework.dir"));

		frameworkSrcDir = new File(frameworkDir, "gen");
		if(!frameworkSrcDir.exists())
			throw new RuntimeException("Framework dir " + frameworkSrcDir + " does not exist");
 		
		File frameworkSrcMapDir = new File(frameworkDir, "srcmap");
	 	if(!frameworkSrcMapDir.exists())
			throw new RuntimeException("Framework dir " + frameworkSrcMapDir + " does not exist");
		//srcMapDirs.add(frameworkSrcMapDir);

		String outDir = System.getProperty("stamp.out.dir");
		srcMapDir = new File(outDir+"/jimple");
		if(!srcMapDir.exists())
			throw new RuntimeException("Framework dir " + srcMapDir + " does not exist");
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
	
	public File srcMapFile(String srcFileName) {
		if(srcFileName != null) {
			String jimpleFileName = srcFileName.replace(".java", ".jimple").replace("/", ".");
			File f = new File(srcMapDir, jimpleFileName);
			if(f.exists()) {
				return f;
			}
		}
		return null;
	}
}
