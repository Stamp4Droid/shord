package stamp.reporting;

import java.util.Map;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.analyses.DomL;
import stamp.analyses.JCFLSolverAnalysis;
import chord.util.tuple.object.Trio;
import chord.util.tuple.object.Pair;

/*
 * @author Saswat Anand
 * @author Osbert Bastani
 **/
public class SrcSinkFlow extends XMLReport {
    public SrcSinkFlow() {
	super("Source-to-sink Flows");
    }

    public void generate() {
	final ProgramRel relSrcSinkFlow = (ProgramRel)ClassicProject.g().getTrgt("flow");
	//final ProgramRel relSrcSinkFlow = (ProgramRel)ClassicProject.g().getTrgt("JSrcSinkFlow");

	relSrcSinkFlow.load();

	/*
	Iterable<Trio<String,String,Integer>> res = relSrcSinkFlow.getAry3ValTuples();
	for(Trio<String,String,Integer> triple : res) {
	    String source = triple.val0;
	    String sink = triple.val1;
	    int weight = triple.val2;
	    newTuple()
		.addValue(source)
		.addValue(sink)
		.addValue(Integer.toString(weight));
	}
	*/

	/*
	Iterable<Pair<String,String>> res = relSrcSinkFlow.getAry2ValTuples();
	for(Pair<String,String> pair : res) {
	    String source = pair.val0;
	    String sink = pair.val1;
	    newTuple()
			.addValue(source)
			.addValue(sink);

	}
	*/

	Map<stamp.jcflsolver.Util.Pair<Integer, Integer>, Integer> src2sink = JCFLSolverAnalysis.getSrc2Sink();
	DomL dom = (DomL)ClassicProject.g().getTrgt("L");
	System.out.println("LENGTH:" + src2sink.entrySet().size());
	for(Map.Entry<stamp.jcflsolver.Util.Pair<Integer, Integer>, Integer> entry : src2sink.entrySet()) {
		//if(entry.getValue() > 0) {
			newTuple()
				.addValue(dom.get(entry.getKey().getX()))
				.addValue(dom.get(entry.getKey().getY()))
				.addValue(Integer.toString(entry.getValue()));
		//}
	}
	
	relSrcSinkFlow.close();
    }


}
