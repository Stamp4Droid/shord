package shord.analyses;

import soot.Scene;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import chord.project.Chord;

import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.ClassicProject;

import java.util.*;

@Chord(name = "cha-java", sign = "I0,M0:I0_M0")
public class CHA extends ProgramRel
{
	public void fill()
	{
		CallGraph cg = Scene.v().getCallGraph();
		//ProgramRel relChaIM = (ProgramRel) ClassicProject.g().getTrgt("chaIM");
        //relChaIM.zero();
		Iterator<Edge> edgeIt = cg.listener();
		while(edgeIt.hasNext()){
			Edge edge = edgeIt.next();
			if(!edge.isExplicit())
				continue;
			Stmt stmt = edge.srcStmt();
			SootMethod tgt = (SootMethod) edge.tgt();
			add(stmt, tgt);
		}

		//relChaIM.save();
	}
}