package stamp.reporting;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import soot.SootMethod;
import chord.util.tuple.object.Pair;

public class ArgSinkFlow extends XMLReport
{
	public ArgSinkFlow()
	{
		super("Sinks");
	}

    public void generate()
	{
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt("out_taintSink");
		rel.load();

		Iterable<Pair<String,SootMethod>> res = rel.getAry2ValTuples();
		for(Pair<String,SootMethod> pair : res) {
			String label = pair.val0;
			Category labelCat = makeOrGetSubCat(label);

			SootMethod sinkMethod = pair.val1;
			labelCat.newTuple().addValue(sinkMethod);
		}

		rel.close();
	}
}
