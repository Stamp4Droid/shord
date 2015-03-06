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

import chord.project.Chord; 
import chord.util.tuple.object.Pair;
import chord.util.tuple.object.Trio;

import com.google.gson.stream.JsonWriter;

import java.util.*;
import java.io.*;

/*
 * @author Saswat Anand
 */
@Chord(name="taint-ondemand-java")
public class TaintAnalysis extends JavaAnalysis
{
	protected TaintManager taintManager;
	protected OnDemandPTA dpta;
	protected JsonWriter writer;

	private Map<SootMethod,List<Edge>> callEdges = new HashMap();
	private Set<SootMethod> sinkMethods;

	public TaintAnalysis()
	{
	}

	public void run()
	{
		setup();
		perform();
		done();
	}

	protected void setup()
	{
		Program.g().runSpark();

		this.dpta = OnDemandPTA.makeDefault();
		this.taintManager = new TaintManager(dpta);

		try{
			String stampOutDir = System.getProperty("stamp.out.dir");
			this.writer = new JsonWriter(new BufferedWriter(new FileWriter(new File(stampOutDir, "flows.json"))));
			writer.setIndent("  ");
			writer.beginArray();
		}catch(IOException e){
			throw new Error(e);
		}
	}

	protected void done()
	{
		try{
			writer.endArray();
			writer.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}

	protected void perform()
	{
		taintManager.readAnnotations();

		this.sinkMethods = taintManager.sinkMethods();

		List<SootMethod> workList = new ArrayList();
		try{
			String stampOutDir = System.getProperty("stamp.out.dir");
			PrintWriter reachableMethodsWriter = new PrintWriter(new BufferedWriter(
											 new FileWriter(new File(stampOutDir, "reachablemethods.txt"))));
			for(Iterator<MethodOrMethodContext> it = Scene.v().getReachableMethods().listener(); it.hasNext();){
				SootMethod m = (SootMethod) it.next();
				reachableMethodsWriter.println(m);
				if(sinkMethods.contains(m)){
					//System.out.println("R: "+m);			
					workList.add(m);
				}
			}
			reachableMethodsWriter.close();
		}catch(IOException e){
			throw new Error(e);
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
				if(!edge.isExplicit())
					continue;
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
		}
		
		SootMethod main = Scene.v().getMethod("<stamp.harness.Main1: void main(java.lang.String[])>");
		traverse(main, dpta.emptyStack(), new HashSet(), null);
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

	protected void visitFinal(SootMethod caller, 
							  Stmt callStmt, 
							  SootMethod callee, 
							  ImmutableStack<Integer> calleeContext, 
							  Object data,
							  List<Trio<Integer,String,Set<String>>> flows)
	{
		CallStack callStack = (CallStack) visit(caller, callStmt, callee, calleeContext, data);
		
		try{
			writer.beginObject();

			writer.name("sink").value(callee.getSignature());
						
			writer.name("callstack");
			writer.beginArray();
			for(Pair<Stmt,SootMethod> elem : callStack)
				writer.value(elem.val0 + "@" + elem.val1.getSignature());
			writer.endArray();
			
			writer.name("flows");
			writer.beginArray();
			for(Trio<Integer,String,Set<String>> e : flows){
				writer.beginObject();
				writer.name("pindex").value(e.val0);
				writer.name("sink-label").value(e.val1);
				writer.name("source-label");
				writer.beginArray();
				for(String w : e.val2)
					writer.value(w);
				writer.endArray();
				writer.endObject();
			}
			writer.endArray();

			writer.endObject();
		} catch(IOException e){
			throw new Error(e);
		}
	}

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

			if(sinkMethods.contains(callee)) {
				List<Trio<Integer,String,Set<String>>> flows = computeTaintFlows(callee, calleeContext);
				visitFinal(caller, callStmt, callee, calleeContext, data, flows);
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
	
	protected List<Trio<Integer,String,Set<String>>> computeTaintFlows(SootMethod sink, ImmutableStack<Integer> sinkContext)
	{
		Body body = sink.retrieveActiveBody();
		List<Trio<Integer,String,Set<String>>> result = new ArrayList();

		List<Pair<Integer,String>> sinkParams = taintManager.sinkParamsOf(sink);

		for(Pair<Integer,String> sinkParam : sinkParams){
			int pCount = sinkParam.val0;
			String sinkLabel = sinkParam.val1;

			Local param = body.getParameterLocal(pCount);
			Set<String> taints = computeTaintSetFor(param, sinkContext);
			if(!taints.isEmpty())
				result.add(new Trio(pCount, sinkLabel, taints));
		}

		return result;
	}

	protected Set<String> computeTaintSetFor(Local loc, ImmutableStack<Integer> locContext)
	{
		Set<String> taints = new HashSet();
		if(!(loc.getType() instanceof RefLikeType)){
			//TODO
			return taints;
		}

		List<VarAndContext> varsWL = new ArrayList();
		Set<VarAndContext> varsVisited = new HashSet();
		varsWL.add(new VarAndContext(dpta.varNode(loc), locContext));

		List<AllocAndContext> objectsWL = new ArrayList();
		Set<AllocAndContext> objectsVisited = new HashSet();

		do{
			while(!varsWL.isEmpty()){
				VarAndContext vc = varsWL.remove(0);
				if(varsVisited.contains(vc))
					continue;
				varsVisited.add(vc);
				
				AllocAndContextSet pt = (AllocAndContextSet) dpta.pointsToSetFor(vc);
				for(AllocAndContext obj : pt)
					objectsWL.add(obj);
			}
			
			while(!objectsWL.isEmpty()){
				AllocAndContext oc = objectsWL.remove(0);
				if(objectsVisited.contains(oc))
					continue;
				objectsVisited.add(oc); 
				
				Set<String> ts = taintManager.getTaint(oc.alloc);
				if(ts != null)
					taints.addAll(ts);
				
				Set<VarAndContext> vcs = dpta.flowsToSetFor(oc);
				for(VarAndContext vc : vcs){
					VarNode dest = vc.var;
					ImmutableStack<Integer> destContext = vc.context;
					Collection<LocalVarNode> sources = taintManager.findTaintTransferSourceFor(dest);
					if(sources == null)
						continue;
					for(LocalVarNode src : sources)
						varsWL.add(new VarAndContext(src, destContext));
				}
			}
		} while(!varsWL.isEmpty());

		return taints;
	}
}