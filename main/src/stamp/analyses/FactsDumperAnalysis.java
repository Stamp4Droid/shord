package stamp.analyses;

import stamp.util.FactsDumper;
import stamp.util.FileHelper;
import stamp.util.PropertyHelper;

import java.io.File;
import java.util.Set;

import chord.project.Chord;
import shord.project.analyses.JavaAnalysis;

@Chord(
	name = "facts-dumper-java"
)
public class FactsDumperAnalysis extends JavaAnalysis {
	private void processDir(File inDir, File outDir) {
		try {
			for (String fInName : FileHelper.listRegularFiles(inDir, "fdt")) {
				String templateName =
					FileHelper.basename(FileHelper.stripExtension(fInName));
				File fOut = new File(outDir, templateName);
				System.out.println("Processing template file " + fInName);
				// TODO: Should have a single dumper make one pass over all
				// templates, then a second full one.
				FactsDumper dumper = new FactsDumper();
				dumper.run(fInName, fOut.getAbsolutePath());
			}
		} catch(Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	@Override
	public void run() {
		File templatesDir =
			new File(PropertyHelper.getProperty("stamp.dumper.templates.dir"));
		File outDir =
			new File(PropertyHelper.getProperty("stamp.dumper.outdir"));
		outDir.mkdir();
		processDir(templatesDir, outDir);
	}
}
