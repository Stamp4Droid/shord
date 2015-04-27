package stamp.analyses;

import java.util.Iterator;
import java.util.Map;

import shord.analyses.LocalVarNode;
import shord.analyses.LocalsClassifier;
import shord.analyses.PAGBuilder;
import shord.analyses.VarNode;
import shord.program.Program;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import chord.project.Chord;

@Chord(name = "implicit-exception-java",
consumes = { "M", "V", "U", },
produces = { "Ref2MethodImp", "Prim2MethodImp", "Method2MethodImp", "Method2RefImp", "Method2PrimImp" },
namesOfTypes = {},
types = {},
namesOfSigns = { "Ref2MethodImp", "Prim2MethodImp", "Method2MethodImp", "Method2RefImp", "Method2PrimImp" },
signs = { "V0,M0:M0_V0", "U0,M0:M0_U0", "M0,M1:M0xM1", "M0,V0:M0_V0", "M0,U0:M0_U0" })
public class ImplicitExceptionAnalysis extends JavaAnalysis {
	public static <T,U,V> MultivalueMap<T,V> join(MultivalueMap<T,U> first, MultivalueMap<U,V> second) {
		MultivalueMap<T,V> join = new MultivalueMap<T,V>();
		for(T t : first.keySet()) {
			for(U u : first.get(t)) {
				for(V v : second.get(u)) {
					join.add(t, v);
				}
			}
		}
		return join;
	}
	
	public void run() {
		MultivalueMap<VarNode,Unit> ref2Unit = new MultivalueMap<VarNode,Unit>();
		MultivalueMap<VarNode,Unit> prim2Unit = new MultivalueMap<VarNode,Unit>();
		
		MultivalueMap<Unit,SootMethod> unit2Method = new MultivalueMap<Unit,SootMethod>();
		
		MultivalueMap<SootMethod,Unit> method2Unit = new MultivalueMap<SootMethod,Unit>(); // This is just IM (backwards)
		
		MultivalueMap<Unit,VarNode> unit2Ref = new MultivalueMap<Unit,VarNode>();
		MultivalueMap<Unit,VarNode> unit2Prim = new MultivalueMap<Unit,VarNode>();
		
		// STEP 1: Build method2Unit
		ProgramRel relIM = (ProgramRel)ClassicProject.g().getTrgt("chaIM");
		relIM.load();
		for(chord.util.tuple.object.Pair<Object,Object> pair : relIM.getAry2ValTuples()) {
			Unit unit = (Unit)pair.val0;
			SootMethod method = (SootMethod)pair.val1;
			method2Unit.add(method, unit);
		}
		relIM.close();
		
		Iterator<MethodOrMethodContext> it = Program.g().scene().getReachableMethods().listener();
		while(it.hasNext()) {
			SootMethod method = (SootMethod)it.next();
			
			if(PAGBuilder.stubMethods.contains(method)) {
				continue;
			}
			if(!method.hasActiveBody()) {
				continue;
			}
			
			ExceptionalControlDependenceGraph.getExceptionalControlDependenceGraph(method);
			
			ControlDependenceGraph cdg = new ControlDependenceGraph(method);
			Map<Local,LocalVarNode> localsToVarNodes = LocalsToVarNodeMap.getLocalToVarNodeMap(method);
			LocalsClassifier lc = new LocalsClassifier(method.getActiveBody());
			
			for(Unit unit : method.getActiveBody().getUnits()) {
				// STEP 2: Build ref2Unit
				for(ValueBox valueBox : unit.getUseBoxes()) {
					if(valueBox.getValue() instanceof Local) {
						LocalVarNode var = localsToVarNodes.get((Local)valueBox.getValue());
						if(lc.nonPrimLocals().contains(var.local)) {
							ref2Unit.add(var, unit);
						} else if(lc.primLocals().contains(var.local)) {
							prim2Unit.add(var, unit);
						} else {
							throw new RuntimeException("Invalid local!");
						}
					}
				}
				
				// STEP 3: Build unit2Method
				unit2Method.add(unit, method);
				
				// STEP 4: Build unit2Ref
				if(cdg.dependeeToDependentsSetMap().get(unit) != null) {
					for(Unit dependent : cdg.dependeeToDependentsSetMap().get(unit)) {
						// for every defined value in dependent, add 
						for(LocalVarNode var : LocalsToVarNodeMap.getVarNodesIn(localsToVarNodes, dependent, true)) {
							if(lc.nonPrimLocals().contains(var.local)) {
								unit2Ref.add(unit, var);
							} else if(lc.primLocals().contains(var.local)) {
								unit2Prim.add(unit, var);
							} else {
								throw new RuntimeException("Unrecognized variable type!");
							}
						}
					}
				}
			}
		}
		
		MultivalueMap<VarNode,SootMethod> ref2Method = join(ref2Unit, unit2Method);
		MultivalueMap<VarNode,SootMethod> prim2Method = join(prim2Unit, unit2Method);
		
		// These are just ref2RefImp, ...
		/*
		MultivalueMap<VarNode,VarNode> ref2Ref = join(ref2Unit, unit2Ref);
		MultivalueMap<VarNode,VarNode> ref2Prim = join(ref2Unit, unit2Prim);
		MultivalueMap<VarNode,VarNode> prim2Ref = join(prim2Unit, unit2Ref);
		MultivalueMap<VarNode,VarNode> prim2Prim = join(prim2Unit, unit2Prim);
		*/
		
		MultivalueMap<SootMethod,SootMethod> method2Method = join(method2Unit, unit2Method);
		
		MultivalueMap<SootMethod,VarNode> method2Ref = join(method2Unit, unit2Ref);
		MultivalueMap<SootMethod,VarNode> method2Prim = join(method2Unit, unit2Prim);
		
		ProgramRel relRef2MethodImp = (ProgramRel)ClassicProject.g().getTrgt("Ref2MethodImp");
		ProgramRel relPrim2MethodImp = (ProgramRel)ClassicProject.g().getTrgt("Prim2MethodImp");
		ProgramRel relMethod2MethodImp = (ProgramRel)ClassicProject.g().getTrgt("Method2MethodImp");
		ProgramRel relMethod2RefImp = (ProgramRel)ClassicProject.g().getTrgt("Method2RefImp");
		ProgramRel relMethod2PrimImp = (ProgramRel)ClassicProject.g().getTrgt("Method2PrimImp");
		
		relRef2MethodImp.zero();
		relPrim2MethodImp.zero();
		relMethod2MethodImp.zero();
		relMethod2RefImp.zero();
		relMethod2PrimImp.zero();
		/*
		for(VarNode source : ref2Method.keySet()) {
			for(SootMethod sink : ref2Method.get(source)) {
				System.out.println("Ref2MethodImp: " + source + " -> " + sink);
				relRef2MethodImp.add(source, sink);
			}
		}
		for(VarNode source : prim2Method.keySet()) {
			for(SootMethod sink : prim2Method.get(source)) {
				System.out.println("Prim2MethodImp: " + source + " -> " + sink);
				relPrim2MethodImp.add(source, sink);
			}
		}
		for(SootMethod source : method2Method.keySet()) {
			for(SootMethod sink : method2Method.get(source)) {
				System.out.println("Method2MethodImp: " + source + " -> " + sink);
				relMethod2MethodImp.add(source, sink);
			}
		}
		for(SootMethod source : method2Ref.keySet()) {
			for(VarNode sink : method2Ref.get(source)) {
				System.out.println("Method2RefImp: " + source + " -> " + sink);
				relMethod2RefImp.add(source, sink);
			}
		}
		for(SootMethod source : method2Prim.keySet()) {
			for(VarNode sink : method2Prim.get(source)) {
				System.out.println("Method2PrimImp: " + source + " -> " + sink);
				relMethod2PrimImp.add(source, sink);
			}
		}
		*/
		relRef2MethodImp.save();
		relPrim2MethodImp.save();
		relMethod2MethodImp.save();
		relMethod2RefImp.save();
		relMethod2PrimImp.save();
	}
}
