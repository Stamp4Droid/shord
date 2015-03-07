package stamp.analyses.ondemand;

import soot.Scene;
import soot.SootMethod;
import soot.MethodOrMethodContext;
import soot.Body;
import soot.Local;
import soot.Type;
import soot.RefLikeType;
import soot.PointsToSet;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.spark.pag.VarNode;
import soot.jimple.spark.pag.LocalVarNode;
import soot.jimple.spark.ondemand.genericutil.ImmutableStack;
import soot.jimple.spark.ondemand.AllocAndContextSet;
import soot.jimple.spark.ondemand.AllocAndContext;
import soot.jimple.spark.ondemand.DemandCSPointsTo.VarAndContext;

import shord.program.Program;
import shord.project.analyses.JavaAnalysis;

import chord.util.tuple.object.Pair;
import chord.util.tuple.object.Trio;

import java.util.*;
import java.io.*;

/*
 * @author Saswat Anand
 */
public abstract class MethodReachabilityAnalysis extends JavaAnalysis
{
	protected OnDemandPTA dpta;
	protected Map<SootMethod,List<Edge>> callEdges = new HashMap();
	protected Set<SootMethod> targetMethods = new HashSet();

	public void run()
	{
		setup();
		perform();
		done();
	}

	protected void setup()
	{
		Program.g().runSpark("merge-stringbuffer:false");
		this.dpta = OnDemandPTA.makeDefault();
	}

	protected void done()
	{
	}

	protected void perform()
	{
		List<SootMethod> workList = new ArrayList();
		try{
			String stampOutDir = System.getProperty("stamp.out.dir");
			PrintWriter reachableMethodsWriter = new PrintWriter(new BufferedWriter(
											 new FileWriter(new File(stampOutDir, "reachablemethods.txt"))));
			for(Iterator<MethodOrMethodContext> it = Scene.v().getReachableMethods().listener(); it.hasNext();){
				SootMethod m = (SootMethod) it.next();
				reachableMethodsWriter.println(m);
				if(targetMethods.contains(m)){
					System.out.println("sinkmethod: "+m);			
					workList.add(m);
				}
			}
			reachableMethodsWriter.close();
		}catch(IOException e){
			throw new Error(e);
		}
		
		CallGraph cg = Scene.v().getCallGraph();
		Set<SootMethod> roots = new HashSet();
		Set<SootMethod> visited = new HashSet();
		while(!workList.isEmpty()){
			SootMethod tgt = workList.remove(0);
			if(visited.contains(tgt))
				continue;
			visited.add(tgt);
			Iterator<Edge> edgeIt = cg.edgesInto(tgt);
			boolean root = true;
			while(edgeIt.hasNext()){
				Edge edge = edgeIt.next();
				if(!edge.isExplicit())
					continue;
				root = false;
				Stmt stmt = edge.srcStmt();
				SootMethod src = (SootMethod) edge.src();
				workList.add(src);
				List<Edge> outgoingEdges = callEdges.get(src);
				if(outgoingEdges == null){
					outgoingEdges = new ArrayList();
					callEdges.put(src, outgoingEdges);
				}
				outgoingEdges.add(edge);
				//System.out.println("success: "+src+" "+stmt+" "+tgt);
				//System.out.println("XY: "+src+" "+tgt);
			}
			if(root)
				roots.add(tgt);
		}
		
		for(SootMethod rootMethod : roots){
			System.out.println("rootMethod: "+rootMethod);
			if(targetMethods.contains(rootMethod)) 
				visitFinal(null, null, rootMethod, dpta.emptyStack(), null);
			else
				traverse(rootMethod, dpta.emptyStack(), new HashSet(), null);
		}
	}

	protected Object visit(SootMethod caller, 
						   Stmt callStmt, 
						   SootMethod callee, 
						   ImmutableStack<Integer> calleeContext, 
						   Object data)
	{
		CallStack cs = data == null ? new CallStack() : (CallStack) data;
		return cs.append(callStmt, caller);
	}

	protected abstract void visitFinal(SootMethod caller, 
									   Stmt callStmt, 
									   SootMethod callee, 
									   ImmutableStack<Integer> calleeContext, 
									   Object data);

	protected void traverse(SootMethod caller, 
							ImmutableStack<Integer> callerContext, 
							Set<SootMethod> visited, 
							Object data)
	{
		List<Edge> outgoingEdges = callEdges.get(caller);
		if(outgoingEdges == null)
			return;
		for(Edge e : outgoingEdges){
			SootMethod callee = e.tgt();
			if(visited.contains(callee))
				continue;

			//check validity of the calledge
			Stmt callStmt = e.srcStmt();
			//System.out.println("Query: "+ callStmt + "@" + (path.size()==0 ? "" : path.get(path.size()-1).tgt()) + " callee: "+callee);
			ImmutableStack<Integer> calleeContext = dpta.calleeContext(callStmt, callee, callerContext);
			if(calleeContext == null)
				continue; //invalid edge

			if(targetMethods.contains(callee)) {
				visitFinal(caller, callStmt, callee, calleeContext, data);
			}
			else {
				Object newData = visit(caller, callStmt, callee, calleeContext, data);
				
				Set<SootMethod> visitedCopy = new HashSet();
				visitedCopy.addAll(visited);
				visitedCopy.add(callee);

				traverse(callee, calleeContext, visitedCopy, newData);
			}
		}
	}
}