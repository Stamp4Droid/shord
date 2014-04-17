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
			"/bin/bash",
			PropertyHelper.getProperty("stamp.solvergen.script"),
			PropertyHelper.getProperty("stamp.solvergen.analysis.version"),
			PropertyHelper.getProperty("stamp.solvergen.analysis.class"),
			PropertyHelper.getProperty("stamp.solvergen.analysis.pri"),
			PropertyHelper.getProperty("stamp.solvergen.analysis.sec")
		};
		ShellProcessRunner.run(cmdLine, workDir, true);
	}
}
