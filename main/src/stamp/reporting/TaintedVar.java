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
import stamp.srcmap.SourceInfo;
import stamp.srcmap.RegisterMap;
import stamp.srcmap.Expr;

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
		Map<String,Map<SootMethod,Set<VarNode>>> labelToTaintedVars = labelToTaintedVars();

		for(Map.Entry<String,Map<SootMethod,Set<VarNode>>> entry1 : labelToTaintedVars.entrySet()){
			String label = entry1.getKey();
			Map<SootMethod,Set<VarNode>> taintedVars = entry1.getValue();
			Category labelCat = makeOrGetSubCat(label);
			for(Map.Entry<SootMethod,Set<VarNode>> entry2 : taintedVars.entrySet()){
				SootMethod meth = entry2.getKey();
				SootClass klass = meth.getDeclaringClass();
				RegisterMap regMap = SourceInfo.buildRegMapFor(meth);
				Set<VarNode> vars = entry2.getValue();
				Category methCat = labelCat.makeOrGetPkgCat(meth);
				for(VarNode v : vars){
					LocalVarNode lvn = (LocalVarNode) v;
					//System.out.println("taintedReg: " + reg + " "+meth);
					Set<Expr> locs = regMap.srcLocsFor(lvn.local);
					if(locs != null && locs.size() > 0){
						for(Expr l : locs){
							if(l.start() < 0 || l.length() < 0 || l.text() == null)
								continue;
							methCat.newTuple().addValueWithHighlight(klass, l);
						}
					}
				}
			}
		}
	}

	private Map<String,Map<SootMethod,Set<VarNode>>> labelToTaintedVars()
	{
		Map<String,Map<SootMethod,Set<VarNode>>> labelToTaintedVars = new HashMap();

		final ProgramRel relRef = (ProgramRel) ClassicProject.g().getTrgt("out_taintedRefVar");
		relRef.load();

		Iterable<Pair<String,VarNode>> res1 = relRef.getAry2ValTuples();
		for(Pair<String,VarNode> pair : res1) {
			if (!(pair.val1 instanceof LocalVarNode)) continue;

			String label = pair.val0;
			VarNode var = pair.val1;

			Map<SootMethod,Set<VarNode>> taintedVars = labelToTaintedVars.get(label);
			if(taintedVars == null){
				taintedVars = new HashMap();
				labelToTaintedVars.put(label, taintedVars);
			}
			
			SootMethod meth = ((LocalVarNode)var).meth;
			Set<VarNode> vars = taintedVars.get(meth);
			if(vars == null){
				vars = new HashSet();
				taintedVars.put(meth, vars);
			}
			vars.add(var);
		}
		relRef.close();

		final ProgramRel relPrim = (ProgramRel) ClassicProject.g().getTrgt("out_taintedPrimVar");
		relPrim.load();

		Iterable<Pair<String,VarNode>> res2 = relPrim.getAry2ValTuples();
		for(Pair<String,VarNode> pair : res2) {
			if (!(pair.val1 instanceof LocalVarNode)) continue;

			String label = pair.val0;
			VarNode var = pair.val1;

			Map<SootMethod,Set<VarNode>> taintedVars = labelToTaintedVars.get(label);
			if(taintedVars == null){
				taintedVars = new HashMap();
				labelToTaintedVars.put(label, taintedVars);
			}
			
			SootMethod meth = ((LocalVarNode)var).meth;
			Set<VarNode> vars = taintedVars.get(meth);
			if(vars == null){
				vars = new HashSet();
				taintedVars.put(meth, vars);
			}
			vars.add(var);
		}
		relPrim.close();

		return labelToTaintedVars;
	}
}
