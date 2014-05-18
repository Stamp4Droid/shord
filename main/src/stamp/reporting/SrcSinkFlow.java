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
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import soot.Unit;
import soot.jimple.Stmt;
import soot.SootClass;
import soot.SootMethod;

import chord.bddbddb.Rel.RelView;
import chord.util.tuple.object.Trio;
import chord.util.tuple.object.Pair;

import java.util.*;


/*
 * @author Saswat Anand
 * @author Osbert Bastani
 **/
public class SrcSinkFlow extends XMLReport {
	public SrcSinkFlow() {
		super("Source-to-sink Flows");
	}

	public void generate() {
		final ProgramRel relCtxtFlows = (ProgramRel)ClassicProject.g().getTrgt("flow");
		//final ProgramRel relSrcSinkFlow = (ProgramRel)ClassicProject.g().getTrgt("JSrcSinkFlow");

			relCtxtFlows.load();

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

	Set<Pair<String,String>> ciFlows = new HashSet();

	Iterable<Pair<Pair<String,Ctxt>,Pair<String,Ctxt>>> res = relCtxtFlows.getAry2ValTuples();
	int count = 0;
	for(Pair<Pair<String,Ctxt>,Pair<String,Ctxt>> pair : res) {
		count++;
	    String source = pair.val0.val0;
		Ctxt sourceCtxt = pair.val0.val1;
	    String sink = pair.val1.val0;
		Ctxt sinkCtxt = pair.val1.val1;

		if(true/*Postmortem.processingSrc*/){
			if(ciFlows.add(new Pair(source, sink))){
				newTuple()
					.addValue(source)
					.addValue(sink);
			}
		} else {
			assert false; //TODO
			/*
			Category flowCat = makeOrGetSubCat(source + " -> " + sink);
			Category ctxtFlowCat = flowCat.makeOrGetSubCat("Flow "+count);
			Tuple srcTuple = ctxtFlowCat.newTuple();//makeOrGetSubCat("context");
			srcTuple.setAttr("source", source);
			for(Unit unit : sourceCtxt.getElems())
				srcTuple.addValue(Program.unitToString(unit));
			Tuple sinkTuple = ctxtFlowCat.newTuple();//makeOrGetSubCat(sink).makeOrGetSubCat("context");
			sinkTuple.setAttr("sink", sink);
			for(Unit unit : sinkCtxt.getElems())
				sinkTuple.addValue(Program.unitToString(unit));
			*/
		}

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

		relCtxtFlows.close();

	}
	}

	private String ctxtToString(Ctxt context) {
		StringBuilder stmtbuilder = new StringBuilder();
		StringBuilder srcbuilder = new StringBuilder();
		/*
		for(Unit unit : context.getElems()) {
			Stmt stmt = (Stmt)unit;

			SootMethod method = Program.containerMethod(stmt);
			SootClass klass = method.getDeclaringClass();
			String filename = sourceInfo.filePath(klass);
			int lineNum = sourceInfo.stmtLineNum(stmt);

			stmtbuilder.append(Program.unitToString(unit)).append(',');
			srcbuilder.append(filename+' '+lineNum).append('~');
		}
		return stmtbuilder.toString() + "~~~" + srcbuilder.toString();
		*/
		return "ERROR";
	}
}
