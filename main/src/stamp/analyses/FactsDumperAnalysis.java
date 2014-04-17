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
		String templatesTopDir =
			PropertyHelper.getProperty("stamp.dumper.templates.dir");
		String outTopDir = PropertyHelper.getProperty("stamp.dumper.outdir");
		for (File inDir : FileHelper.listSubDirs(templatesTopDir)) {
			File outDir = new File(outTopDir, inDir.getName());
			outDir.mkdir();
			processDir(inDir, outDir);
		}
	}
}
