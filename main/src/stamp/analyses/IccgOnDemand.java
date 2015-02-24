package stamp.analysis;

import soot.RefType;
import soot.Type;
import soot.AnySubType;
import soot.ArrayType;
import soot.RefLikeType;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootMethod;
import soot.SootField;
import soot.Transform;
import soot.PackManager;
import soot.MethodOrMethodContext;
import soot.jimple.toolkits.callgraph.VirtualCalls;
import soot.util.NumberedString;
import soot.jimple.Stmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.spark.ondemand.DemandCSPointsTo;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.spark.internal.TypeManager;
import chord.project.Chord; 
import shord.project.analyses.JavaAnalysis;

import java.util.*;

/*
 * @author Saswat Anand
 */
@Chord(name="iccg-ondemand-java")
public class IccgOnDemand extends JavaAnalysis
{
    private List<String> iccMeths = Arrays.asList(new String[] {
        "<android.content.ContextWrapper: void startActivity(android.content.Intent)>",
        "<android.app.Activity: void startActivityForResult(android.content.Intent,int)>",
        "<android.app.Activity: void startActivityForResult(android.content.Intent,int,android.os.Bundle)>",
        "<android.app.Activity: boolean startActivityIfNeeded(android.content.Intent,int)>",
        "<android.app.Activity: boolean startActivityIfNeeded(android.content.Intent,int,android.os.Bundle)>",
        "<android.app.Activity: boolean startNextMatchingActivity(android.content.Intent)>",
        "<android.app.Activity: boolean startNextMatchingActivity(android.content.Intent,android.os.Bundle)>",
        "<android.app.Activity: void startActivity(android.content.Intent)>"
		});

	private List<Edge> callEdges = new ArrayList();
	private DemandCSPointsTo dpta;

	public void run() 
	{
		setup();
		
		Set<SootMethod> meths = new HashSet();
		for(String methSig : iccMeths){
			if(!Scene.v().containsMethod(methSig))
				continue;
			SootMethod m = Scene.v().getMethod(methSig);
			meths.add(m);
		}

		List<SootMethod> workList = new ArrayList();
		for(Iterator<MethodOrMethodContext> it = Scene.v().getReachableMethods().listener(); it.hasNext();){
			SootMethod m = (SootMethod) it.next();
			if(meths.contains(m)){
				System.out.println("R: "+m);			
				workList.add(m);
			}
		}
		
		CallGraph cg = Scene.v().getCallGraph();
		Set<SootMethod> visited = new HashSet();
		while(!workList.isEmpty()){
			SootMethod tgt = workList.remove(0);
			if(visited.contains(tgt))
				continue;
			visited.add(tgt);
			Iterator<Edge> edgeIt = cg.edgesInto(tgt);
			while(edgeIt.hasNext()){
				Edge edge = edgeIt.next();
				if(!edge.isExplicit() && !edge.isThreadRunCall())
					continue;
				Stmt stmt = edge.srcStmt();
				SootMethod src = (SootMethod) edge.src();
				if(checkCallEdge(src, stmt, tgt)){
					workList.add(src);
					callEdges.add(edge);
					System.out.println("success: "+src+" "+stmt+" "+tgt);
				} else
					System.out.println("fail: "+src+" "+stmt+" "+tgt);
			}
		}
	}

	boolean checkCallEdge(SootMethod srcm, Stmt stmt, SootMethod tgtm)
	{
		if(!stmt.containsInvokeExpr())
			assert false;
		InvokeExpr ie = stmt.getInvokeExpr();
		SootMethod callee = ie.getMethod(); 
		if(ie instanceof StaticInvokeExpr || ie instanceof SpecialInvokeExpr){
			assert callee.equals(tgtm);
			return true;
		}

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
	}

	private void setup()
	{
		//run spark
		Transform sparkTransform = PackManager.v().getTransform( "cg.spark" );
		String defaultOptions = sparkTransform.getDefaultOptions();
		StringBuilder options = new StringBuilder();
		options.append("enabled:true");
		options.append(" verbose:true");
		options.append(" cs-demand:true");
		//options.append(" dump-answer:true");
		options.append(" "+defaultOptions);
		System.out.println("spark options: "+options.toString());
		sparkTransform.setDefaultOptions(options.toString());
		sparkTransform.apply();	
		
		this.dpta = (DemandCSPointsTo) Scene.v().getPointsToAnalysis();
	}
}