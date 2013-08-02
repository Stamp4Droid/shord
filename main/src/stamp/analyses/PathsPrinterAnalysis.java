package stamp.analyses;

import java.io.PrintWriter;
import java.util.List;

import stamp.paths.Path;
import stamp.paths.PathsAdapter;
import stamp.paths.Step;
import stamp.util.PropertyHelper;

import chord.project.Chord;
import shord.project.analyses.JavaAnalysis;

@Chord(
	name = "paths-printer-java"
)
public class PathsPrinterAnalysis extends JavaAnalysis {
	@Override
	public void run() {
		String schemaFile = PropertyHelper.getProperty("stamp.paths.schema");
		String rawPathsFile = PropertyHelper.getProperty("stamp.paths.raw");
		String normalPathsFile =
			PropertyHelper.getProperty("stamp.paths.normal");
		String flatPathsFile = PropertyHelper.getProperty("stamp.paths.flat");

		try {
			PathsAdapter adapter = new PathsAdapter(schemaFile);
			adapter.normalizeRawPaths(rawPathsFile, normalPathsFile);

			List<Path> paths = adapter.getFlatPaths(rawPathsFile);
			PrintWriter pw = new PrintWriter(flatPathsFile);
			pw.println("PATHS: " + paths.size());
			pw.println();
			for (Path p : paths) {
				pw.println(p.start + " --> " + p.end);
				for (Step s : p.steps) {
					pw.println((s.reverse ? "<-" : "--" ) + s.symbol +
							   (s.reverse ? "-- " : "-> " ) + s.target);
				}
				pw.println();
			}
			pw.close();
		} catch(Exception exc) {
			throw new RuntimeException(exc);
		}
	}
}
