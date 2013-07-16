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
		for (Path p : PathsAdapter.getPaths()) {
			System.out.println(p.start + " --> " + p.end);
			for (Step s : p.steps) {
				System.out.println((s.reverse ? "<-" : "--" ) + s.symbol +
								   (s.reverse ? "-- " : "-> " ) + s.target);
			}
			System.out.println();
		}
	}
}
