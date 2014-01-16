package stamp.analyses;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.Scene;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import chord.util.tuple.object.Pair;

import shord.program.Program;
import stamp.srcmap.sourceinfo.abstractinfo.AbstractSourceInfo;

import java.util.*;

public class StringAnalysis
{
	public final Set<String> scs = new HashSet();
	
	public void analyze()
	{
		Iterator mIt = Program.g().scene().getReachableMethods().listener();
		while(mIt.hasNext()){
			SootMethod m = (SootMethod) mIt.next();
			if(!m.isConcrete())
				continue;
			SootClass declKlass = m.getDeclaringClass();
			if(AbstractSourceInfo.isFrameworkClass(declKlass))
				continue;

			for(ValueBox vb : m.retrieveActiveBody().getUseBoxes()){
				Value val = vb.getValue();
				if(!(val instanceof StringConstant))
					continue;
				String str = ((StringConstant) val).value;
				scs.add(str);
			}
		}
		
		Scene scene = Program.g().scene();
		CallGraph cg = scene.getCallGraph();

		List<Pair<String,Integer>> httpHeaderMeths = new ArrayList();
		httpHeaderMeths.add(new Pair("<org.apache.http.client.methods.HttpGet: void addHeader(java.lang.String,java.lang.String)>", 1));
		httpHeaderMeths.add(new Pair("<org.apache.http.client.methods.HttpGet: void addHeader(java.lang.String,java.lang.String)>", 2));
		httpHeaderMeths.add(new Pair("<org.apache.http.client.methods.HttpPost: void addHeader(java.lang.String,java.lang.String)>", 1));
		httpHeaderMeths.add(new Pair("<org.apache.http.client.methods.HttpPost: void addHeader(java.lang.String,java.lang.String)>", 2));
		httpHeaderMeths.add(new Pair("<org.apache.http.message.BasicHeader: void <init>(java.lang.String,java.lang.String)>", 1));
		httpHeaderMeths.add(new Pair("<org.apache.http.message.BasicHeader: void <init>(java.lang.String,java.lang.String)>", 2));
		
		httpHeaderMeths.add(new Pair("<org.apache.http.params.DefaultedHttpParams: org.apache.http.params.HttpParams setParameter(java.lang.String,java.lang.Object)>", 1));
		httpHeaderMeths.add(new Pair("<org.apache.http.params.BasicHttpParams: org.apache.http.params.HttpParams setParameter(java.lang.String,java.lang.Object)>", 1));
		httpHeaderMeths.add(new Pair("<org.apache.http.params.AbstractHttpParams: org.apache.http.params.HttpParams setBooleanParameter(java.lang.String,boolean)>", 1));
		httpHeaderMeths.add(new Pair("<org.apache.http.params.AbstractHttpParams: org.apache.http.params.HttpParams setDoubleParameter(java.lang.String,double)>", 1));
		httpHeaderMeths.add(new Pair("<org.apache.http.params.AbstractHttpParams: org.apache.http.params.HttpParams setIntParameter(java.lang.String,int)>", 1));
		httpHeaderMeths.add(new Pair("<org.apache.http.params.AbstractHttpParams: org.apache.http.params.HttpParams setLongParameter(java.lang.String,long)>", 1));

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
			}
		}
	}
}