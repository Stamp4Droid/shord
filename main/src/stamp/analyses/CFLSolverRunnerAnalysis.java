package stamp.analyses;

import stamp.util.PropertyHelper;
import stamp.util.ShellProcessRunner;

import chord.project.Chord;
import shord.project.analyses.JavaAnalysis;

@Chord(
	name = "cfl-solver-runner-java"
)
public class CFLSolverRunnerAnalysis extends JavaAnalysis {
	@Override
	public void run() {
		String workDir = PropertyHelper.getProperty("stamp.out.dir");
		String[] cmdLine = new String[]{
			PropertyHelper.getProperty("stamp.solvergen.executable"),
			PropertyHelper.getProperty("stamp.solvergen.indir"),
			PropertyHelper.getProperty("stamp.solvergen.outdir"),
		};
		ShellProcessRunner.run(cmdLine, workDir, true);
	}
}
