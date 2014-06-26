package stamp.ifaceextract;

import soot.Scene;
import soot.SootMethod;
import soot.Value;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import shord.program.Program;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.ClassicProject;

import chord.bddbddb.Rel.RelView;
import chord.project.Chord;

import java.util.*;

@Chord(name = "iface-java-2",
	   consumes={"getExtraInvkComp", "GetIntentExtraMeths"})
public class Extract extends JavaAnalysis
{
	private ProgramRel relGetExtraInvkComp;

	public void run()
	{
		relGetExtraInvkComp = (ProgramRel) ClassicProject.g().getTrgt("getExtraInvkComp");
		relGetExtraInvkComp.load();

		Scene scene = Program.g().scene();
		CallGraph cg = scene.getCallGraph();

		final ProgramRel relGetIntentExtraMeths = (ProgramRel) ClassicProject.g().getTrgt("GetIntentExtraMeths");
		relGetIntentExtraMeths.load();
		Iterable<SootMethod> mIt = relGetIntentExtraMeths.getAry1ValTuples();
		for(SootMethod m : mIt){
			String type = m.getReturnType().toString();
			Iterator<Edge> edgeIt = cg.edgesInto(m);
			while(edgeIt.hasNext()){
				Edge edge = edgeIt.next();
				Stmt stmt = edge.srcStmt();
				SootMethod src = edge.src();
				InvokeExpr ie = stmt.getInvokeExpr();
				Value arg = ie.getArg(0);
				Set<String> vals = null;
				if(arg instanceof StringConstant){
					vals = new HashSet();
					vals.add(((StringConstant) arg).value);
				}
				if(vals != null){
					Iterable<String> targetIt = getIntentTargets(stmt);
					for(String target : targetIt){
						for(String key : vals)
							System.out.println("name: "+ target + " key: "+key + " type: "+type);
					}
				}
			}
		}
		relGetIntentExtraMeths.close();


		//Iterable<Pair<Unit,String>> res1 = relRef.getAry2ValTuples();
		//for(

		relGetExtraInvkComp.close();
	}
	
	private Iterable<String> getIntentTargets(Unit invkUnit)
	{
        RelView view = relGetExtraInvkComp.getView();
        view.selectAndDelete(0, invkUnit);
        return view.getAry1ValTuples();
    }
}