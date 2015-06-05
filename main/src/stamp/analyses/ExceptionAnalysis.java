package stamp.analyses;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import shord.analyses.LocalVarNode;
import shord.analyses.PAGBuilder;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import soot.Local;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.AbstractInvokeExpr;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.util.LocalsToVarNodeMap;
import chord.project.Chord;

@Chord(name = "exception-java",
consumes = { "T", "M", "V", "U", },
produces = { "CaughtException", "MethodExceptionDependeeBase", "MethodExceptionDependeePrimBase", "MethodExceptionDependeeBaseVar" },
namesOfTypes = {},
types = {},
namesOfSigns = { "CaughtException", "MethodExceptionDependeeBase", "MethodExceptionDependeePrimBase", "MethodExceptionDependeeBaseVar" },
signs = { "I0,T0,V0:T0_V0_I0", "M0,T0,V0:T0_M0_V0", "M0,T0,U0:T0_M0_U0", "M0,V0:M0_V0" })
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
		ProgramRel relMethodExceptionDependeeVar = (ProgramRel)ClassicProject.g().getTrgt("MethodExceptionDependeeBaseVar");
		relMethodExceptionDependeeVar.zero();
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
			if(localsToVarNodes == null) {
				System.out.println("ERROR: No locals to var nodes map for method " + method);
				continue;
			}
			// STEP 2a: Caught exceptions
			MultivalueMap<Unit,SootClass> caughtExceptions = new MultivalueMap<Unit,SootClass>();
			for(Trap trap : method.getActiveBody().getTraps()) {
				//System.out.println("TRAP: " + trap.toString());
				VarNode catchVar = localsToVarNodes.get((Local)((IdentityStmt)trap.getHandlerUnit()).getLeftOp());
				boolean reached = false;
				for(Unit unit : method.getActiveBody().getUnits()) {
					if(!reached) {
						if(unit == trap.getBeginUnit()) {
							reached = true;
						} else {
							continue;
						}
					} else if(unit == trap.getEndUnit()) {
						break;
					}
					//System.out.println("TRAPPED UNIT: " + unit.toString());
					for(SootClass klass : getCanStore(trap.getException())) {
						caughtExceptions.add(unit, klass);
						if(((Stmt)unit).containsInvokeExpr()) {
							//System.out.println("CAUGHT EXCEPTION: " + unit + " -> " + klass.getType());
							relCaughtException.add(unit, klass.getType(), catchVar);
						}
					}
				}
			}
			for(Unit unit : method.getActiveBody().getUnits()) {
				// STEP 2b: Implicitly thrown exceptions
				MultivalueMap<SootClass,LocalVarNode> impMap = baseImpExceptionsThrown(localsToVarNodes, unit);
				for(SootClass klass : impMap.keySet()) {
					if(caughtExceptions.get(unit).contains(klass)) {
						continue;
					}
					for(LocalVarNode var : impMap.get(klass)) {
						if(var.local.getType() instanceof RefLikeType) {
							//System.out.println("THROWN EXCEPTION: " + method + " -> " + klass.getType() + " -> " + var);
							relMethodExceptionDependee.add(method, klass.getType(), var);
						} else if(var.local.getType() instanceof PrimType) {
							relMethodExceptionDependeePrim.add(method, klass.getType(), var);
						} else {
							throw new RuntimeException("Type not recognized!");
						}
					}
				}
				// STEP 2c: Explicitly thrown exceptions
				for(LocalVarNode var : baseExpExceptionsThrown(localsToVarNodes, unit)) {
					//System.out.println("PROCESSING THROWN EXCEPTION: " + var);
					// Determine if the exception is caught
					boolean caught = false;
					SootClass klass = ((RefType)var.local.getType()).getSootClass();
					for(SootClass unitThrownKlass : getCanStore(klass)) {
						if(caughtExceptions.get(unit).contains(unitThrownKlass)) {
							caught = true;
							break;
						}
					}
					if(caught) {
						//System.out.println("UNIT TRAPPED");
						continue;
					}
					// Determine if the exception is thrown
					boolean thrown = false;
					for(SootClass unitThrownKlass : getCanStore(klass)) {
						for(SootClass methodThrownKlass : method.getExceptions()) {
							if(PAGBuilder.canStore(unitThrownKlass.getType(), methodThrownKlass.getType())) {
								thrown = true;
								break;
							}
						}
					}
					// Runtime exceptions don't need to be thrown
					for(SootClass unitThrownKlass : getCanStore(klass)) {
						if(PAGBuilder.canStore(unitThrownKlass.getType(), Scene.v().getSootClass("java.lang.RuntimeException").getType())) {
							thrown = false;
							break;
						}
					}
					if(!thrown) {
						//System.out.println("EXCEPTION NOT THROWN");
						continue;
					}
					// Add to relation
					if(var.local.getType() instanceof RefLikeType) {
						//System.out.println("METHOD THROWS: " + method + " -> " + var.local.getType());
						relMethodExceptionDependeeVar.add(method, var);
					} else {
						throw new RuntimeException("Type not recognized!");
					}
				}
			}
		}
		// STEP 3: Cleanup
		relReachableM.close();
		relMethodExceptionDependee.save();
		relMethodExceptionDependeePrim.save();
		relMethodExceptionDependeeVar.save();
		relCaughtException.save();
	}
	
	private static MultivalueMap<SootClass,LocalVarNode> baseImpExceptionsThrown(Map<Local,LocalVarNode> map, Unit unit) {
		MultivalueMap<SootClass,LocalVarNode> result = new MultivalueMap<SootClass,LocalVarNode>();
		Stmt stmt = (Stmt)unit;
		// CASE: Invoke parseInt
		if(stmt.containsInvokeExpr()) {
			SootMethod callee = stmt.getInvokeExpr().getMethod();
			if(callee.getSignature().equals("<java.lang.Integer: int parseInt(java.lang.String)>")) {
				result.add(Scene.v().getSootClass("java.lang.NumberFormatException"), map.get((Local)stmt.getInvokeExpr().getArg(0)));
			}
		}
		// CASE: Array access
		if(stmt.containsArrayRef()) {
			result.add(Scene.v().getSootClass("java.lang.ArrayIndexOutOfBoundsException"), map.get((Local)stmt.getArrayRef().getBase()));
		}
		// CASE: Null pointer exception (for instance method call)
		if(stmt.containsInvokeExpr() && stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
			result.add(Scene.v().getSootClass("java.lang.NullPointerException"), map.get((Local)((InstanceInvokeExpr)stmt.getInvokeExpr()).getBase()));
		}
		// CASE: Null pointer exepction (for instance field access)
		if(stmt.containsFieldRef() && !stmt.getFieldRef().getField().isStatic()) {
			result.add(Scene.v().getSootClass("java.lang.NullPointerException"), map.get((Local)((InstanceFieldRef)stmt.getFieldRef()).getBase()));
		}
		return result;
	}
	
	private static Set<LocalVarNode> baseExpExceptionsThrown(Map<Local,LocalVarNode> map, Unit unit) {
		Set<LocalVarNode> result = new HashSet<LocalVarNode>();
		Stmt stmt = (Stmt)unit;
		// CASE: Exception thrown
		if(stmt instanceof ThrowStmt) {
			LocalVarNode var = map.get((Local)((ThrowStmt)stmt).getOp());
			result.add(var);
		}
		return result;
	}
}
