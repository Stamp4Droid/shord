package stamp.analyses;

import java.io.PrintWriter;
import java.io.File;

import stamp.paths.Path;
import stamp.paths.PathsAdapter;
import stamp.paths.Step;

import chord.project.Chord;
import shord.project.analyses.JavaAnalysis;

@Chord(
	name = "paths-printer-java"
)
public class PathsPrinterAnalysis extends JavaAnalysis {
	protected static final String SOLVERGEN_DIR = "solvergen";
	protected static final String SOLVERGEN_FILENAME = "solvergenpaths.out";

	@Override
	public void run() {

		try {
			File dir =  new File(SOLVERGEN_DIR);
			dir.mkdir();
			PrintWriter pw = new PrintWriter(new File(SOLVERGEN_DIR + "/" +SOLVERGEN_FILENAME));

			for (Path p : PathsAdapter.getPaths()) {
				pw.println(p.start + " --> " + p.end);
				for (Step s : p.steps) {
					pw.println((s.reverse ? "<-" : "--" ) + s.symbol +
									   (s.reverse ? "-- " : "-> " ) + s.target);
				}
				pw.println();
			}

		} catch (Exception e) {
			System.err.println("problem writing solvergenpaths.out");
			e.printStackTrace();
		}
	}
}
