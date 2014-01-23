package stamp.analyses;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.Scene;
import soot.Local;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.AssignStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

import chord.util.tuple.object.Pair;

import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.analyses.ProgramDom;
import shord.project.ClassicProject;
import shord.program.Program;

import chord.project.Chord;

import java.util.*;

@Chord(name = "string-java",
	   produces = {"HttpMeth"},
	   namesOfSigns = {"HttpMeth"},
	   signs = {"M0,Z0:M0_Z0"})
public class StringAnalysis extends JavaAnalysis
{
	public void run()
	{		
		Scene scene = Program.g().scene();
		CallGraph cg = scene.getCallGraph();

		Map<String,Object> httpHeaderMeths = new HashMap();

		httpHeaderMeths.put("<org.apache.http.client.methods.HttpGet: void addHeader(java.lang.String,java.lang.String)>", 
							new int[]{1, 2});

		httpHeaderMeths.put("<org.apache.http.client.methods.HttpPost: void addHeader(java.lang.String,java.lang.String)>", 
							new int[]{1, 2});

		httpHeaderMeths.put("<org.apache.http.message.BasicHeader: void <init>(java.lang.String,java.lang.String)>", 
							new int[]{1, 2});

		httpHeaderMeths.put("<org.apache.http.params.DefaultedHttpParams: org.apache.http.params.HttpParams setParameter(java.lang.String,java.lang.Object)>", 
							new int[]{1, 2});

		httpHeaderMeths.put("<org.apache.http.params.BasicHttpParams: org.apache.http.params.HttpParams setParameter(java.lang.String,java.lang.Object)>",
							new int[]{1, 2});

		httpHeaderMeths.put("<org.apache.http.params.AbstractHttpParams: org.apache.http.params.HttpParams setBooleanParameter(java.lang.String,boolean)>", 
							new int[]{1});

		httpHeaderMeths.put("<org.apache.http.params.AbstractHttpParams: org.apache.http.params.HttpParams setDoubleParameter(java.lang.String,double)>", 
							new int[]{1});

		httpHeaderMeths.put("<org.apache.http.params.AbstractHttpParams: org.apache.http.params.HttpParams setIntParameter(java.lang.String,int)>",
							new int[]{1});

		httpHeaderMeths.put("<org.apache.http.params.AbstractHttpParams: org.apache.http.params.HttpParams setLongParameter(java.lang.String,long)>",
							new int[]{1});

        		             
        httpHeaderMeths.put("<org.apache.http.params.HttpProtocolParams: void setUserAgent(org.apache.http.params.HttpParams,java.lang.String)>",
							new int[]{1});

        httpHeaderMeths.put("<org.apache.http.params.HttpProtocolParamBean: void setUserAgent(java.lang.String)>", 
							new int[]{1});

        httpHeaderMeths.put("<android.webkit.WebSettings: void setUserAgentString(java.lang.String)>", 
							new int[]{1});

		httpHeaderMeths.put("<org.apache.http.message.BasicNameValuePair: void <init>(java.lang.String,java.lang.String)>", 
							new int[]{1, 2});


		ProgramRel relHttpMeth = (ProgramRel) ClassicProject.g().getTrgt("HttpMeth");
		relHttpMeth.zero();

		for(Map.Entry<String,Object> pair : httpHeaderMeths.entrySet()){
			String mSig = pair.getKey();
			if(!scene.containsMethod(mSig))
				continue;
			SootMethod m = scene.getMethod(mSig);
			int[] paramIndices = (int[]) pair.getValue();
			for(int paramIndex : paramIndices){
				relHttpMeth.add(m, new Integer(paramIndex));
			}
		}

		relHttpMeth.save();

		/*
		for(Pair<String,Integer> pair : httpHeaderMeths){
			String mSig = pair.val0;
			if(!scene.containsMethod(mSig))
				continue;
			SootMethod m = scene.getMethod(mSig);
			Integer paramIndex = pair.val1;

			Iterator<Edge> edgeIt = cg.edgesInto(m);
			while(edgeIt.hasNext()){
				Edge edge = edgeIt.next();
				Stmt stmt = edge.srcStmt();
				SootMethod src = edge.src();

				System.out.println("headerstmt: "+stmt + " in " + src.getSignature());

				String val = null;
				InvokeExpr ie = stmt.getInvokeExpr();
				Value arg = ie.getArg(paramIndex);
				if(arg instanceof StringConstant){
					val = ((StringConstant) arg).value;					
				} else if(arg instanceof Local){
					SimpleLocalDefs	sld = new SimpleLocalDefs(new ExceptionalUnitGraph(src.retrieveActiveBody()));
					
					//System.out.println("WARN: Argument of setContentView is not constant");					
					for(Unit def : sld.getDefsOfAt((Local) arg, stmt)){
						if(!(def instanceof AssignStmt)){
							System.out.println("def: "+def);
							continue;
						}
						Value rhs = ((AssignStmt) def).getRightOp();
						if(!(rhs instanceof StringConstant)){
							System.out.println("def: "+def);
							continue;
						}
						val = ((StringConstant) rhs).value;
					}
				}
				if(val != null){
					System.out.println("arg "+val);
					Set<String> params = httpParams.get(pair);
					if(params == null){
						params = new HashSet();
						httpParams.put(pair, params);
					}
					params.add(val);
				}
			}
		}
		*/
	}
}