package stamp.analyses;

import stamp.paths.Path;
import stamp.paths.PathsAdapter;
import stamp.paths.Step;

import chord.project.Chord;
import shord.project.analyses.JavaAnalysis;

@Chord(
	name = "paths-printer-java"
)
public class PathsPrinterAnalysis extends JavaAnalysis {
	@Override
	public void run() {
		PrintWriter pw = new PrintWriter(new File("solvergenpaths.out"));
		for (Path p : PathsAdapter.getPaths()) {
			pw.println(p.start + " --> " + p.end);
			for (Step s : p.steps) {
				pw.println((s.reverse ? "<-" : "--" ) + s.symbol +
								   (s.reverse ? "-- " : "-> " ) + s.target);
			}
			pw.println();
		}
	}
}
