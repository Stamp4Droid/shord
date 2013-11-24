package stamp.analyses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import shord.project.analyses.JavaAnalysis;
import soot.Scene;
import soot.SootClass;
import stamp.missingmodels.jimplesrcmapper.ChordJimpleAdapter;
import stamp.missingmodels.jimplesrcmapper.CodeStructureInfo;
import stamp.missingmodels.jimplesrcmapper.JimpleStructureExtractor;
import stamp.missingmodels.jimplesrcmapper.Printer;
import stamp.missingmodels.util.xml.XMLObject;
import stamp.srcmap.SourceInfoSingleton;
import stamp.srcmap.sourceinfo.jimpleinfo.JimpleSourceInfo;
import chord.project.Chord;

import com.google.common.io.NullOutputStream;

/*
 * An analysis that runs the JCFLSolver to do the taint analysis.
 */

@Chord(name = "jimpleprinter")
public class JimplePrinterAnalysis extends JavaAnalysis {

	@Override public void run() {
		try {
			// SET UP SCRATCH DIRECTORY
			String outDir = System.getProperty("stamp.out.dir");
			//File outputDir = new File(stampDirectory + File.separator + "cfl");
			//File scratchDir = new File(stampDirectory + File.separator + "/../../osbert/scratch/" + outputDir.getParentFile().getName());
			//String outputPath = scratchDir.getCanonicalPath() + "/jimple/";

			// PRINT JIMPLE
			JimpleStructureExtractor jse = new JimpleStructureExtractor();
			Printer printer = new Printer(jse);
			printer.printAll(outDir + "/jimple/");

			PrintWriter pw = new PrintWriter(outDir + "/loc.txt");
			pw.println(printer.getAppLOC() + "\n" + printer.getFrameworkLOC());
			pw.close();

			// GET STRUCTURE AND PRINT
			CodeStructureInfo codeInfo = jse.getCodeStructureInfo();
			/*
			System.out.println("PRINTING CLASS INFO:");
			for(SootClass cl : codeInfo.getClasses()) {
				System.out.println(cl.toString() + ": " + codeInfo.getClassInfo(cl).toString());
			}
			System.out.println("PRINTING METHOD INFO:");
			for(SootMethod m : codeInfo.getMethods()) {
				System.out.println(m.toString() + ": " + codeInfo.getMethodInfo(m).toString());
			}
			*/
			JimpleSourceInfo sourceInfo = SourceInfoSingleton.getJimpleSourceInfo();

			for(SootClass cl : Scene.v().getClasses()) {
				System.out.println("READING: " + cl.getName());

				// GET THE OUTPUT FILE PATH	
				StringBuffer b = new StringBuffer();
				//b.append(outputPath);
				b.append(outDir + "/jimple/" + cl.getPackageName().replace('.', '/') + '/'); 
				b.append(cl.getName());	
				b.append(".xml");
				String xmlOutputPath = b.toString();

				// CREATE THE OBJECT
				ChordJimpleAdapter cja = new ChordJimpleAdapter(sourceInfo);
				printer = new Printer(cja.toJimpleVisitor(codeInfo));
				printer.printTo(cl, new NullOutputStream());
				XMLObject object = cja.getResults().get(cl);

				// WRITE THE XML OBJECT
				File objectOutputFile = new File(xmlOutputPath);
				objectOutputFile.getParentFile().mkdirs();
				System.out.println("PRINTING TO: " + objectOutputFile.getCanonicalPath());
				pw = new PrintWriter(new FileOutputStream(objectOutputFile));
				pw.println(object.toString());
				pw.close();
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
