package stamp.analyses;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import shord.analyses.Ctxt;
import shord.analyses.LocalVarNode;
import shord.analyses.LocalsClassifier;
import shord.analyses.PAGBuilder;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import soot.Local;
import soot.PrimType;
import soot.RefLikeType;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.util.ExceptionalControlDependenceGraph;
import stamp.missingmodels.util.cflsolver.util.LocalsToVarNodeMap;
import stamp.missingmodels.util.cflsolver.util.ExceptionalControlDependenceGraph.ExceptionDependeeStruct;
import chord.project.Chord;

@Chord(name = "implicit-exception-java",
consumes = { "M", "C", "V", "U", },
produces = { "Ref2RefImpCCtxt", "Ref2PrimImpCCtxt", "Prim2RefImpCCtxt", "Prim2PrimImpCCtxt" },
namesOfTypes = {},
types = {},
namesOfSigns = { "Ref2RefImpCCtxt", "Ref2PrimImpCCtxt", "Prim2RefImpCCtxt", "Prim2PrimImpCCtxt" },
signs = { "C0,V0,C1,V1:C0xC1_V0xV1", "C0,V0,C1,U0:C0xC1_V0_U0", "C0,U0,C1,V0:C0xC1_V0_U0", "C0,U0,C1,U1:C0xC1_U0xU1" })
public class ImplicitExceptionAnalysis extends JavaAnalysis {
	private static <T,U,V> MultivalueMap<T,V> join(MultivalueMap<T,U> first, MultivalueMap<U,V> second) {
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
		ProgramRel relRef2RefImp = (ProgramRel)ClassicProject.g().getTrgt("Ref2RefImpCCtxt");
		ProgramRel relRef2PrimImp = (ProgramRel)ClassicProject.g().getTrgt("Ref2PrimImpCCtxt");
		ProgramRel relPrim2RefImp = (ProgramRel)ClassicProject.g().getTrgt("Prim2RefImpCCtxt");
		ProgramRel relPrim2PrimImp = (ProgramRel)ClassicProject.g().getTrgt("Prim2PrimImpCCtxt");
		
		relRef2RefImp.zero();
		relRef2PrimImp.zero();
		relPrim2RefImp.zero();
		relPrim2PrimImp.zero();
		
		ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("ci_reachableM");
		relReachableM.load();
		Set<SootMethod> methods = new HashSet<SootMethod>();
		for(Object obj : relReachableM.getAry1ValTuples()) {
			methods.add((SootMethod)obj);
		}
		relReachableM.close();
		
		for(SootMethod method : methods) {
			// STEP 0: Setup
			//System.out.println("PROCESSING METHOD: " + method);
			if(PAGBuilder.stubMethods.contains(method)) {
				continue;
			}
			if(!method.hasActiveBody()) {
				continue;
			}
			
			// STEP 1: Get useful data structures
			Map<Local,LocalVarNode> localsToVarNodes = LocalsToVarNodeMap.getLocalToVarNodeMap(method);
			LocalsClassifier lc = new LocalsClassifier(method.getActiveBody());
			
			// STEP 2: Get the control dependence edges
			MultivalueMap<Unit,Unit> dependentToDependees = ExceptionalControlDependenceGraph.getExceptionalControlDependenceGraph(method);
			
			// STEP 3: Get the exception dependees
			MultivalueMap<Unit,ExceptionDependeeStruct> exceptionDependeeStructs = ExceptionalControlDependenceGraph.getExceptionDependeeStructs(method);
			
			// STEP 4: Build implicit exception relations
			for(Unit dependent : dependentToDependees.keySet()) {
				for(Unit dependee : dependentToDependees.get(dependent)) {
					for(ExceptionDependeeStruct struct : exceptionDependeeStructs.get(dependee)) {
						Ctxt sourceCtxt = struct.varCtxt;
						LocalVarNode sourceVar = (LocalVarNode)struct.var;
						Ctxt sinkCtxt = struct.ctxt;
						for(ValueBox valueBox : dependent.getDefBoxes()) {
							if(valueBox.getValue() instanceof Local) {
								LocalVarNode sinkVar = localsToVarNodes.get((Local)valueBox.getValue());
								// Handle all four case
								if(sourceVar.local.getType() instanceof RefLikeType) {
									if(lc.nonPrimLocals().contains(sinkVar.local)) {
										relRef2RefImp.add(sourceCtxt, sourceVar, sinkCtxt, sinkVar);
										//System.out.println("Ref2RefImpCCtxt Edge: (" + sourceCtxt + ", " + sourceVar + ") -> (" + sinkCtxt + ", " + sinkVar + ")");
									} else if(lc.primLocals().contains(sinkVar.local)) {
										relRef2PrimImp.add(sourceCtxt, sourceVar, sinkCtxt, sinkVar);
									} else {
										throw new RuntimeException("Unrecognized variable type!");
									}
								} else if(sourceVar.local.getType() instanceof PrimType) {
									if(lc.nonPrimLocals().contains(sinkVar.local)) {
										relPrim2RefImp.add(sourceCtxt, sourceVar, sinkCtxt, sinkVar);
									} else if(lc.primLocals().contains(sinkVar.local)) {
										relPrim2PrimImp.add(sourceCtxt, sourceVar, sinkCtxt, sinkVar);
									} else {
										throw new RuntimeException("Unrecognized variable type!");
									}
								} else {
									throw new RuntimeException("Unrecognized variable type!");
								}
							}
						}
					}
				}
			}
		}
		
		relRef2RefImp.save();
		relRef2PrimImp.save();
		relPrim2RefImp.save();
		relPrim2PrimImp.save();
	}
}
