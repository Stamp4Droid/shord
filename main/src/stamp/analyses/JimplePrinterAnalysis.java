package stamp.analyses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import shord.project.analyses.JavaAnalysis;
import soot.Scene;
import soot.SootClass;
import stamp.missingmodels.util.jimplesrcmapper.ChordJimpleAdapter;
import stamp.missingmodels.util.jimplesrcmapper.CodeStructureInfo;
import stamp.missingmodels.util.jimplesrcmapper.JimpleStructureExtractor;
import stamp.missingmodels.util.jimplesrcmapper.Printer;
import stamp.srcmap.SourceInfoSingleton;
import stamp.srcmap.XMLObject;
import stamp.srcmap.sourceinfo.jimpleinfo.JimpleSourceInfo;
import stamp.util.PropertyHelper;
import chord.project.Chord;

import com.google.common.io.NullOutputStream;

/*
 * @author Osbert Bastani
 */
@Chord(name = "jimpleprinter")
public class JimplePrinterAnalysis extends JavaAnalysis {

	@Override public void run() {		
		try {

			boolean printClasses =
				PropertyHelper.getBoolProp("stamp.print.allclasses");
			System.out.println("++stamp.print.allclasses = "+printClasses);
			if(!printClasses)
				return;

			// SET UP SCRATCH DIRECTORY
			String outDir = System.getProperty("stamp.out.dir");

			// PRINT JIMPLE
			JimpleStructureExtractor jse = new JimpleStructureExtractor();
			new Printer(jse).printAll(outDir + "/jimple/");

			// GET STRUCTURE AND PRINT
			CodeStructureInfo codeInfo = jse.getCodeStructureInfo();

			JimpleSourceInfo sourceInfo = SourceInfoSingleton.getJimpleSourceInfo();

			for(SootClass cl : Scene.v().getClasses()) {
				//System.out.println("READING: " + cl.getName());

				// GET THE OUTPUT FILE PATH	
				StringBuffer b = new StringBuffer();
				//b.append(outputPath);
				b.append(outDir + "/jimple/" + cl.getPackageName().replace('.', '/') + '/'); 
				b.append(cl.getName());	
				b.append(".xml");
				String xmlOutputPath = b.toString();

				// CREATE THE OBJECT
				ChordJimpleAdapter cja = new ChordJimpleAdapter(sourceInfo);
				Printer printer = new Printer(cja.toJimpleVisitor(codeInfo));
				printer.printTo(cl, new NullOutputStream());
				XMLObject object = cja.getResults().get(cl);

				// WRITE THE XML OBJECT
				File objectOutputFile = new File(xmlOutputPath);
				objectOutputFile.getParentFile().mkdirs();
				//System.out.println("PRINTING TO: " + objectOutputFile.getCanonicalPath());
				PrintWriter pw = new PrintWriter(new FileOutputStream(objectOutputFile));
				pw.println(object.toString());
				pw.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
