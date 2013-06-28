package stamp.reporting;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import chord.util.tuple.object.Pair;

import soot.SootClass;
import soot.SootMethod;
import shord.analyses.VarNode;
import shord.analyses.LocalVarNode;
import soot.Local;

import java.util.*;
import stamp.srcmap.*;

/**
 * @author Saswat Anand
 */
public class TaintedVar extends XMLReport
{
	public TaintedVar()
	{
		super("Tainted Vars");
	}

    public void generate()
	{
		Map<SootMethod,List<Pair<String,VarNode>>> methToTaintedVars = methToTaintedVars();

		for(Map.Entry<SootMethod,List<Pair<String,VarNode>>> entry : methToTaintedVars.entrySet()){
			SootMethod meth = entry.getKey();
			List<Pair<String,VarNode>> tvs = entry.getValue();
			Set<Tuple> tups = createTuples(meth, tvs);
			if(tups.size() > 0){
				Category cat = makeOrGetPkgCat(meth);
				for(Tuple t : tups)
					cat.addTuple(t);
			}
		}
	}

	private Map<SootMethod,List<Pair<String,VarNode>>> methToTaintedVars()
	{
		Map<SootMethod,List<Pair<String,VarNode>>> methToPairs = new HashMap();

		final ProgramRel relRef = (ProgramRel) ClassicProject.g().getTrgt("out_taintedRefVar");
		relRef.load();

		Iterable<Pair<String,VarNode>> res1 = relRef.getAry2ValTuples();
		for(Pair<String,VarNode> pair : res1) {
			if (!(pair.val1 instanceof LocalVarNode)) continue;

			VarNode var = (VarNode) pair.val1;
			SootMethod meth = ((LocalVarNode)var).meth;
//			SootMethod meth = domU.getMethod(var);
			List<Pair<String,VarNode>> vars = methToPairs.get(meth);
			if(vars == null){
				vars = new ArrayList();
				methToPairs.put(meth, vars);
			}
			vars.add(pair);
		}
		relRef.close();

		final ProgramRel relPrim = (ProgramRel) ClassicProject.g().getTrgt("out_taintedPrimVar");
		relPrim.load();

		Iterable<Pair<String,VarNode>> res2 = relPrim.getAry2ValTuples();
		for(Pair<String,VarNode> pair : res2) {
			if (!(pair.val1 instanceof LocalVarNode)) continue;

			VarNode var = (VarNode)pair.val1;
			SootMethod meth = ((LocalVarNode)var).meth;
//			SootMethod meth = domU.getMethod(var);
			List<Pair<String,VarNode>> vars = methToPairs.get(meth);
			if(vars == null){
				vars = new ArrayList();
				methToPairs.put(meth, vars);
			}
			vars.add(pair);
		}
		relPrim.close();

		return methToPairs;
	}

	private Set<Tuple> createTuples(SootMethod meth, List<Pair<String,VarNode>> taintedVars)
	{
		//System.out.println("meth " + meth);
		SootClass klass = meth.getDeclaringClass();
		//WATCHOUT: getName might be wrong, want to get the name of the source file (might have to do translation)
		RegisterMap regMap = new RegisterMap(meth, SrcMapper.methodInfo(SourceInfo.filePath(meth.getDeclaringClass()), meth.getSignature()));

		Set<Tuple> tuples = new HashSet();
		for(Pair<String,VarNode> pair : taintedVars){
			String taintLabel = pair.val0;
			LocalVarNode lvn = (LocalVarNode)pair.val1;
			//System.out.println("taintedReg: " + reg + " "+meth);
			Set<Expr> locs = regMap.srcLocsFor(lvn.local);
			if(locs != null && locs.size() > 0){
				for(Expr l : locs){
					if(l.start() < 0 || l.length() < 0 || l.text() == null)
						continue;
					Tuple tuple = new Tuple();
					tuple.addValueWithHighlight(klass, l);
					tuple.addValue(taintLabel);
					tuples.add(tuple);
					//System.out.println("tuple for " + var + " in " + meth + ": " + tuple);
				}
			}
		}
		return tuples;
	}

}
