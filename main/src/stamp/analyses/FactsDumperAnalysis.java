package stamp.analyses;

import stamp.util.FactsDumper;
import stamp.util.FileHelper;
import stamp.util.PropertyHelper;

import java.util.Set;

import chord.project.Chord;
import shord.project.analyses.JavaAnalysis;

@Chord(
	name = "facts-dumper-java"
)
public class FactsDumperAnalysis extends JavaAnalysis {
	@Override
	public void run() {
		String inDir = PropertyHelper.getProperty("stamp.dumper.templatesdir");
		Set<String> inFiles = FileHelper.listRegularFiles(inDir, "fdt");
		String outDir = PropertyHelper.getProperty("stamp.dumper.outdir");

		try {
			for (String fIn : inFiles) {
				String fOut =
					FileHelper.changeDir(FileHelper.stripExtension(fIn),
										 outDir);
				// TODO: Should have a single dumper make one pass over all
				// templates, then a second full one.
				FactsDumper dumper = new FactsDumper();
				dumper.run(fIn, fOut);
			}
		} catch(Exception exc) {
			throw new RuntimeException(exc);
		}
	}
}
