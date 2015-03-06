package stamp.analysis;

import soot.G;
import soot.Scene;
import soot.SootMethod;
import soot.SootField;
import soot.Local;
import soot.PointsToSet;
import soot.jimple.Stmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.spark.sets.EmptyPointsToSet;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.VarNode;
import soot.jimple.spark.ondemand.pautil.ContextSensitiveInfo;
import soot.jimple.spark.ondemand.genericutil.ImmutableStack;
import soot.jimple.spark.ondemand.DemandCSPointsTo;
import soot.jimple.spark.ondemand.HeuristicType;
import soot.jimple.spark.ondemand.TerminateEarlyException;
import soot.jimple.spark.ondemand.AllocAndContextSet;
import soot.jimple.toolkits.callgraph.Edge;


import java.util.*; 

public class OnDemandPTA extends DemandCSPointsTo
{
	public static OnDemandPTA makeDefault()
	{
		return makeWithBudget(500000, 50, DEFAULT_LAZY);
	}

	public static OnDemandPTA makeWithBudget(int maxTraversal, int maxPasses, boolean lazy) 
	{
        PAG pag = (PAG) Scene.v().getPointsToAnalysis();
		ContextSensitiveInfo csInfo = new ContextSensitiveInfo(pag);
        return new OnDemandPTA(csInfo, pag, maxTraversal, maxPasses, lazy);
    }

	private OnDemandPTA(ContextSensitiveInfo csInfo, PAG pag, int maxTraversal, int maxPasses, boolean lazy)
	{
		super(csInfo, pag, maxTraversal, maxPasses, lazy);
		init();
	}

	/*
	public Integer callSiteFor(InstanceInvokeExpr ie)
	{
		Local rcvr = (Local) ((InstanceInvokeExpr) ie).getBase();
		LocalVarNode rcvrVarNode = (LocalVarNode) pag.findLocalVarNode(rcvr);
		Set<Integer> callSites = csInfo.getVirtCallSitesForReceiver(rcvrVarNode);
		SootMethod invokedMethod = ie.getMethod();
		for(Integer cs : callSites){
			if(csInfo.getInvokedMethod(cs).equals(invokedMethod))
				return cs;
		}
		return null;
		}*/

	ImmutableStack<Integer> calleeContext(Stmt stmt, SootMethod callee, ImmutableStack<Integer> callerContext)
	{
		if(!stmt.containsInvokeExpr())
			assert false;
		ImmutableStack<Integer> calleeContext = null;
		InvokeExpr ie = stmt.getInvokeExpr();
		Integer callSite = csInfo.getCallSiteFor(ie);
		if(ie instanceof StaticInvokeExpr || ie instanceof SpecialInvokeExpr){
			assert ie.getMethod().equals(callee);
			if(callSite != null)
				calleeContext = callerContext.push(callSite);
			else
				System.out.println("UNSOUND");
		} else if(!pag.virtualCallsToReceivers.containsKey(ie)){
			//single outgoing calledge for a VirtualInvokeExpr or InterfaceInvokeExpr
			Iterator<Edge> it = Scene.v().getCallGraph().edgesOutOf(stmt);
			SootMethod tgt = it.next().tgt();
			assert !it.hasNext() && tgt.equals(callee);
			calleeContext = callerContext.push(callSite);
		} else {
			clearState();
			this.fieldCheckHeuristic = HeuristicType.getHeuristic(heuristicType, pag.getTypeManager(), getMaxPasses());
			try{
				Set<SootMethod> callees = refineCallSite(callSite, callerContext);
				if(callees.contains(callee))
					calleeContext = callerContext.push(callSite);
			}catch(TerminateEarlyException e){
			}
		}
		return calleeContext;

		/*
		Local rcvr = (Local) ((InstanceInvokeExpr) ie).getBase();
		RefLikeType rcvrType = (RefLikeType) rcvr.getType();
		NumberedString methSubsig = callee.getNumberedSubSignature();
		TypeManager tm = dpta.getPAG().getTypeManager();
		for(Type runtimeType : dpta.doReachingObjects(rcvr).possibleTypes()){
			if(!tm.castNeverFails(runtimeType, rcvrType))
				continue;
			assert !(runtimeType instanceof AnySubType);
			if(runtimeType instanceof ArrayType) {
				runtimeType = RefType.v("java.lang.Object");
			}
			SootMethod targetMethod = VirtualCalls.v().resolveNonSpecial((RefType) runtimeType, methSubsig);
			if(tgtm.equals(targetMethod))
				return true;
		}
		return false;
		*/
	}

    /**
     * Computes the refined set of reaching objects for l.                                                            
     * Returns <code>null</code> if refinement failed.
     */
    public PointsToSet pointsToSetFor(Local l, ImmutableStack<Integer> context) {
        VarNode v = pag.findLocalVarNode(l);
        if (v == null) {
			//no reaching objects
			return EmptyPointsToSet.v();
        }

		clearState();
        // must reset the refinement heuristic for each query
        this.fieldCheckHeuristic = HeuristicType.getHeuristic(
															  heuristicType, pag.getTypeManager(), getMaxPasses());
        doPointsTo = true;
        numPasses = 0;
        PointsToSet contextSensitiveResult = null;
        while (true) {
            numPasses++;
            if (DEBUG_PASS != -1 && numPasses > DEBUG_PASS) {
                break;
            }
            if (numPasses > maxPasses) {
                break;
            }
            if (DEBUG) {
                G.v().out.println("PASS " + numPasses);
                G.v().out.println(fieldCheckHeuristic);
            }
            clearState();
            pointsTo = new AllocAndContextSet();
            try {
                refineP2Set(new VarAndContext(v, context), null);
                contextSensitiveResult = pointsTo;
            } catch (TerminateEarlyException e) {
            }
            if (!fieldCheckHeuristic.runNewPass()) {
                break;
            }
        }
        return contextSensitiveResult;
    }
	
	public PointsToSet pointsToSetFor(SootField f)
	{
		assert f.isStatic() : f.getSignature();
		return pag.reachingObjects(f);
	}
	
	ImmutableStack<Integer> emptyStack()
	{
		return EMPTY_CALLSTACK;
	}
}