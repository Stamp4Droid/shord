package stamp.ifaceextract;

import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.ClassicProject;
import shord.program.Program;

import shord.analyses.VarNode;
import shord.analyses.LocalVarNode;
import shord.analyses.DomV;
import stamp.analyses.DomL;
import shord.analyses.ComponentAnalysis1;

import soot.SootClass;
import soot.SootMethod;
import soot.Local;
import soot.Body;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.util.Chain;
import soot.util.NumberedSet;

import chord.project.Chord;
import chord.util.tuple.object.Pair;

import java.util.*;

/*
@Chord(name="tainttextinputs-java",
       consumes= { "V", "L" },
	   produces= { "VarLabel" },
	   namesOfSigns = { "VarLabel" },
	   signs = { "V0,L0:V0_L0" }
      )
*/
public class TaintTextInputs //extends JavaAnalysis
{
	Map<Local,List<String>> varToLabels = new HashMap();

    public void run()
    {
		WidgetIdAnalysis wia = new WidgetIdAnalysis();

		NumberedSet fklasses = ComponentAnalysis1.frameworkClasses();
		for(SootClass klass : Program.g().getClasses()){
			if(fklasses.contains(klass))
				continue;
			for(SootMethod method : klass.getMethods()) {
				if(!method.isConcrete())
					continue;

				Body body = method.retrieveActiveBody();
				Chain<Local> locals = body.getLocals();
				Chain<Unit> units = body.getUnits();
				Iterator<Unit> uit = units.snapshotIterator();
				while(uit.hasNext()){
					Stmt stmt = (Stmt) uit.next();
					//invocation statements
					if(!stmt.containsInvokeExpr())
						continue;
					
					InvokeExpr ie = stmt.getInvokeExpr();
					String mSig = ie.getMethod().getSignature();
					if(!mSig.equals("<android.widget.TextView: java.lang.CharSequence getText()>") &&
					   !mSig.equals("<android.widget.EditText: android.text.Editable getText()>"))
						continue;

					if(!(stmt instanceof DefinitionStmt))
						continue;
					
					Local leftOp = (Local) ((DefinitionStmt) stmt).getLeftOp();
					Local wVar = (Local) ((InstanceInvokeExpr) ie).getBase();
					System.out.println("getText call Found: "+method.getSignature());
					
					Set<String> ids = wia.findIds(wVar, stmt, method);
					for(String id : ids){
						System.out.println("Found textbox "+id);
						List<String> labels = varToLabels.get(leftOp);
						if(labels == null){
							labels = new ArrayList();
							varToLabels.put(leftOp, labels);
						}
						labels.add(id);
					}
				}
			}
		}

		wia.finish();
	}

	public void populateDomL()
	{
		DomL domL = (DomL) ClassicProject.g().getTrgt("L");
		for(List<String> labels : varToLabels.values())
			for(String l : labels)
				domL.add(l);
		domL.save();
	}
	
	public void populateVarLabel()
	{
		ProgramRel relVarLabel = (ProgramRel) ClassicProject.g().getTrgt("VarLabel");
		relVarLabel.zero();
		DomV domV = (DomV) ClassicProject.g().getTrgt("V");
		for(VarNode varNode : domV){
			if(!(varNode instanceof LocalVarNode))
				continue;
			Local l = ((LocalVarNode) varNode).local;
			List<String> labels = varToLabels.get(l);
			if(labels == null)
				continue;
			for(String label : labels)
				relVarLabel.add(varNode, label);
		}
		relVarLabel.save();
	}

}