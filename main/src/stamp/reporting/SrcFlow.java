package stamp.reporting;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import chord.util.tuple.object.Pair;

import soot.SootMethod;

public class SrcFlow extends XMLReport
{
	public SrcFlow()
	{
		super("Sources");
	}

    public void generate()
	{
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt("out_taintSrc");
		rel.load();

		Iterable<Pair<String,SootMethod>> res = rel.getAry2ValTuples();
		for(Pair<String,SootMethod> pair : res) {
			SootMethod srcMethod = pair.val1;
			makeOrGetPkgCat(srcMethod.getDeclaringClass()).newTuple()
				.addValue(srcMethod)
				.addValue("Label: "+pair.val0);
		}

		rel.close();
	}
}
