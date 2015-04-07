package stamp.analyses;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

/**
 * 
 * Copied from stamp.reporting.PotentialCallbacks
 * @author obastani
 *
 */
@Chord(name = "reflect-analysis")
public class ReflectAnalysis extends JavaAnalysis {
	@Override
	public void run() {
		ProgramRel relReflect = (ProgramRel)ClassicProject.g().getTrgt("ReflectMatch");
		IOUtils.printRelation("ReflectArgObj");
		IOUtils.printRelation("ReflectArgMatch");
		//IOUtils.printRelation("ReflectArgMismatch");
		//IOUtils.printRelation("ReflectAnyArgMismatch");
		IOUtils.printRelation("ReflectAnyArgMatch");
	}
}
