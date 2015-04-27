package shord.analyses;

import java.util.Map;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.UnitBox;
import soot.jimple.IdentityStmt;
import soot.jimple.Stmt;
import stamp.analyses.LocalsToVarNodeMap;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import chord.project.Chord;

@Chord(name = "exception-java",
consumes = { "T", "M", "V", "U", },
produces = { "CaughtException", "MethodExceptionDependeeBase", "MethodExceptionDependeePrimBase" },
namesOfTypes = {},
types = {},
namesOfSigns = { "CaughtException", "MethodExceptionDependeeBase", "MethodExceptionDependeePrimBase" },
signs = { "I0,T0,V0:T0_V0_I0", "M0,T0,V0:T0_M0_V0", "M0,T0,U0:T0_M0_U0" })
public class ExceptionAnalysis extends JavaAnalysis {
	private static MultivalueMap<SootClass,SootClass> canStore;
	public static Iterable<SootClass> getCanStore(SootClass lhs) {
		if(canStore == null) {
			canStore = new MultivalueMap<SootClass,SootClass>();
			for(SootClass rhsClass : Scene.v().getClasses()) {
				for(SootClass lhsClass : Scene.v().getClasses()) {
					if(PAGBuilder.canStore(rhsClass.getType(), lhsClass.getType())) {
						canStore.add(lhsClass, rhsClass);
					}
				}
			}
		}
		return canStore.get(lhs);
	}
	
	// This class should build relations CaughtException(i:I,t:T) and MethodExceptionDependeeBase(m:M,t:T,v:V)
	// - CaughtException(i,t) means that exception of type t thrown at invoke site i is caught
	// - MethodExceptionDependeeBase(m,t,v) means that method m throws exception of type t with dependee variable v
	// To improve precision, we should make sure that the exception is not caught within the method.
	//
	// In Datalog, we construct the relation
	// - MethodExceptionDependee(m,t,v) :- MethodExceptionDependeeBase(m,t,v)
	// - MethodExceptionDependee(m,t,v) :- MethodExceptionDependee(mp,t,v), IM(i,m), !CaughtException(i,t), MI(m,i)
	//
	// Using this, we can augment the control-dependence graph (but we should probably just reconstruct it).
	@Override
	public void run() {
		// STEP 0: Setup relations
		ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("ci_reachableM");
		relReachableM.load();
		ProgramRel relMethodExceptionDependee = (ProgramRel)ClassicProject.g().getTrgt("MethodExceptionDependeeBase");
		relMethodExceptionDependee.zero();
		ProgramRel relMethodExceptionDependeePrim = (ProgramRel)ClassicProject.g().getTrgt("MethodExceptionDependeePrimBase");
		relMethodExceptionDependeePrim.zero();
		ProgramRel relCaughtException = (ProgramRel)ClassicProject.g().getTrgt("CaughtException");
		relCaughtException.zero();
		// STEP 1: Set up canStore relation
		// STEP 2: Process methods
		for(Object obj : relReachableM.getAry1ValTuples()) {
			SootMethod method = (SootMethod)obj;
			if(!method.hasActiveBody()) {
				continue;
			}
			Map<Local,LocalVarNode> localsToVarNodes = LocalsToVarNodeMap.getLocalToVarNodeMap(method);
			// STEP 2a: Caught exceptions
			MultivalueMap<Unit,SootClass> caughtExceptions = new MultivalueMap<Unit,SootClass>();
			for(Trap trap : method.getActiveBody().getTraps()) {
				VarNode catchVar = localsToVarNodes.get((Local)((IdentityStmt)trap.getHandlerUnit()).getLeftOp());
				for(UnitBox unitBox : trap.getUnitBoxes()) {
					for(SootClass klass : getCanStore(trap.getException())) {
						caughtExceptions.add(unitBox.getUnit(), klass);
						if(((Stmt)unitBox.getUnit()).containsInvokeExpr()) {
							System.out.println("CaughtException:" + unitBox.getUnit() + " -> " + klass.getType());
							relCaughtException.add(unitBox.getUnit(), klass.getType(), catchVar);
						}
					}
				}
			}
			// STEP 2b: Thrown exceptions
			for(Unit unit : method.getActiveBody().getUnits()) {
				MultivalueMap<SootClass,LocalVarNode> map = baseExceptionsThrown(localsToVarNodes, unit);
				for(SootClass klass : map.keySet()) {
					if(caughtExceptions.get(unit).contains(klass)) {
						continue;
					}
					for(LocalVarNode var : map.get(klass)) {
						if(var.local.getType() instanceof RefType) {
							System.out.println("ThrownException: " + method + " -> " + klass.getType() + " -> " + var);
							relMethodExceptionDependee.add(method, klass.getType(), var);
						} else {
							relMethodExceptionDependeePrim.add(method, klass.getType(), var);
						}
					}
				}
			}
		}
		// STEP 3: Cleanup
		relReachableM.close();
		relMethodExceptionDependee.save();
		relMethodExceptionDependeePrim.save();
		relCaughtException.save();
	}
	
	private static MultivalueMap<SootClass,LocalVarNode> baseExceptionsThrown(Map<Local,LocalVarNode> map, Unit unit) {
		MultivalueMap<SootClass,LocalVarNode> result = new MultivalueMap<SootClass,LocalVarNode>();
		Stmt stmt = (Stmt)unit;
		if(stmt.containsInvokeExpr()) {
			SootMethod callee = stmt.getInvokeExpr().getMethod();
			if(callee.getSignature().equals("<java.lang.Integer: int parseInt(java.lang.String)>")) {
				result.add(Scene.v().getSootClass("java.lang.NumberFormatException"), map.get((Local)stmt.getInvokeExpr().getArg(0)));
			}
		}
		return result;
	}
}
