package stamp.reporting;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.analyses.DomL;
import stamp.analyses.JCFLSolverAnalysis;
import shord.analyses.Ctxt;
import shord.program.Program;

import soot.Unit;

import chord.bddbddb.Rel.RelView;
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
	final ProgramRel relFlows = (ProgramRel)ClassicProject.g().getTrgt("out_flow");
	//final ProgramRel relSrcSinkFlow = (ProgramRel)ClassicProject.g().getTrgt("JSrcSinkFlow");

	relFlows.load();

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

	//Note: As of 7.9.2013, the first block below is used for non-jcfl stuff. The second block is
	//required instead for Osbert's JCFL flow stuff. They are mutually exclusive.

	for(Pair<String,String> pair : relFlows.getAry2ValTuples()) {
	    String source = pair.val0;
	    String sink = pair.val1;
		newTuple()
			.addValue(source)
			.addValue(sink);

		// if(Postmortem.processingSrc){
		// 	newTuple()
		// 		.addValue(source)
		// 		.addValue(sink);
		// } else {
		// 	Category flowCat = makeOrGetSubCat(source + " -> " + sink);
		// 	Category ctxtFlowCat = flowCat.makeOrGetSubCat("Flow "+count);
		// 	Tuple srcTuple = ctxtFlowCat.newTuple();//makeOrGetSubCat("context");
		// 	srcTuple.setAttr("source", source);
		// 	for(Unit unit : sourceCtxt.getElems())
		// 		srcTuple.addValue(Program.unitToString(unit));
		// 	Tuple sinkTuple = ctxtFlowCat.newTuple();//makeOrGetSubCat(sink).makeOrGetSubCat("context");
		// 	sinkTuple.setAttr("sink", sink);
		// 	for(Unit unit : sinkCtxt.getElems())
		// 		sinkTuple.addValue(Program.unitToString(unit));
		// }
	}

	/*
	// Get all source-to-sink flows, regardless of context. To achieve this, we
	// project on the two context columns.
	RelView flowsView = relCtxtFlows.getView();
	flowsView.delete(2);
	flowsView.delete(0);
	Iterable<Pair<String,String>> flows = flowsView.getAry2ValTuples();
	Set<Pair<String,String>> seen = new HashSet<Pair<String,String>>();
	for (Pair<String,String> f : flows) {
		// Projection doesn't actually remove duplicates, so we have to skip
		// those manually.
		if (!seen.add(f)) {
			continue;
		}
	    String source = f.val0;
	    String sink = f.val1;
		// Collect all context-aware flows between two specific endpoints.
		RelView ctxtFlowsSelection = relCtxtFlows.getView();
		ctxtFlowsSelection.selectAndDelete(3, sink);
		ctxtFlowsSelection.selectAndDelete(1, source);
		int numFlows = ctxtFlowsSelection.size();
	    newTuple()
			.addValue(source)
			.addValue(sink)
			.addValue(Integer.toString(numFlows));
	}
	*/

	/*
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
    */

	relFlows.close();

    }
}
