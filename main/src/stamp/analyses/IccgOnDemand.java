package stamp.analysis;

import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.PackManager;
import soot.MethodOrMethodContext;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.spark.ondemand.genericutil.ImmutableStack;

import chord.project.Chord; 
import shord.project.analyses.JavaAnalysis;

import com.google.gson.stream.JsonWriter;

import java.util.*;
import java.io.*;
/*
 * @author Saswat Anand
 */
@Chord(name="iccg-ondemand-java")
public class IccgOnDemand extends JavaAnalysis
{
    private List<String> iccMethSigs = Arrays.asList(new String[] {
        "<android.content.ContextWrapper: void startActivity(android.content.Intent)>",
        "<android.app.Activity: void startActivityForResult(android.content.Intent,int)>",
        "<android.app.Activity: void startActivityForResult(android.content.Intent,int,android.os.Bundle)>",
        "<android.app.Activity: boolean startActivityIfNeeded(android.content.Intent,int)>",
        "<android.app.Activity: boolean startActivityIfNeeded(android.content.Intent,int,android.os.Bundle)>",
        "<android.app.Activity: boolean startNextMatchingActivity(android.content.Intent)>",
        "<android.app.Activity: boolean startNextMatchingActivity(android.content.Intent,android.os.Bundle)>",
        "<android.app.Activity: void startActivity(android.content.Intent)>"
		});

	private Set<SootMethod> iccMeths = new HashSet();
	private Map<SootMethod,List<Edge>> callEdges = new HashMap();
	private OnDemandPTA dpta;
	private JsonWriter writer;

	public void run() 
	{
		setup();
		
		for(String methSig : iccMethSigs){
			if(!Scene.v().containsMethod(methSig))
				continue;
			SootMethod m = Scene.v().getMethod(methSig);
			iccMeths.add(m);
		}

		List<SootMethod> workList = new ArrayList();
		for(Iterator<MethodOrMethodContext> it = Scene.v().getReachableMethods().listener(); it.hasNext();){
			SootMethod m = (SootMethod) it.next();
			if(iccMeths.contains(m)){
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
				if(true/*checkCallEdge(src, stmt, tgt)*/){
					workList.add(src);
					List<Edge> outgoingEdges = callEdges.get(src);
					if(outgoingEdges == null){
						outgoingEdges = new ArrayList();
						callEdges.put(src, outgoingEdges);
					}
					outgoingEdges.add(edge);
					//System.out.println("success: "+src+" "+stmt+" "+tgt);
				} else
					;//System.out.println("fail: "+src+" "+stmt+" "+tgt);
			}
		}

		//for(SootMethod m : callEdges.keySet()){
		//	System.out.println("XX: "+m);
		//}
		
		SootMethod main = Scene.v().getMethod("<stamp.harness.Main1: void main(java.lang.String[])>");

		try{
			String stampOutDir = System.getProperty("stamp.out.dir");
			this.writer = new JsonWriter(new BufferedWriter(new FileWriter(new File(stampOutDir, "iccg.json"))));
			writer.setIndent("  ");
			writer.beginArray();
			traverse(main, new ArrayList(), new HashSet(), dpta.emptyStack());
			writer.endArray();
			writer.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}

	void traverse(SootMethod m, List<Edge> path, Set<SootMethod> visited, ImmutableStack<Integer> callerContext) throws IOException
	{
		List<Edge> outgoingEdges = callEdges.get(m);
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

			if(iccMeths.contains(callee)){
				pathStr(path, e);
				continue;
			}
			Set<SootMethod> visitedCopy = new HashSet();
			visitedCopy.addAll(visited);
			visitedCopy.add(callee);

			List<Edge> pathCopy = new ArrayList();
			pathCopy.addAll(path);
			pathCopy.add(e);

			traverse(callee, pathCopy, visitedCopy, calleeContext);
		}
	}

	void pathStr(List<Edge> path, Edge e) throws IOException
	{
		writer.beginObject();

		String srcAct = path.get(0).tgt().getDeclaringClass().getName();
		writer.name("src").value(srcAct);
		
		writer.name("icc-path");
		writer.beginArray();
		for(Edge edge : path)
			writer.value(edge.srcStmt()+"@"+edge.src().getSignature());
		writer.value(e.srcStmt()+"@"+e.src());
		writer.endArray();

		writer.name("icc-meth").value(e.tgt().getSignature());
		
		writer.endObject();
	}

	private void setup()
	{
		//run spark
		Transform sparkTransform = PackManager.v().getTransform( "cg.spark" );
		String defaultOptions = sparkTransform.getDefaultOptions();
		StringBuilder options = new StringBuilder();
		options.append("enabled:true");
		options.append(" verbose:true");
		//options.append(" dump-answer:true");
		options.append(" "+defaultOptions);
		System.out.println("spark options: "+options.toString());
		sparkTransform.setDefaultOptions(options.toString());
		sparkTransform.apply();	
		
		this.dpta = OnDemandPTA.makeDefault();
	}
}