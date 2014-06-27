package stamp.ifaceextract;

import soot.SootMethod;
import soot.Value;
import soot.Unit;
import soot.Local;
import soot.Type;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.InvokeExpr;

import stamp.analyses.string.Slicer;

import shord.program.Program;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.ClassicProject;

import chord.bddbddb.Rel.RelView;
import chord.project.Chord;
import chord.util.tuple.object.Pair;

import java.util.*;

@Chord(name = "iface-java-2",
	   consumes={"extraInvkComp", "extraInvkCompositeArg", "IntentExtraMeths", "ci_IM", "MI"})
public class Extract extends JavaAnalysis
{
	private ProgramRel relExtraInvkComp;
	private ProgramRel relExtraInvkCompositeArg;
	private ProgramRel relMI;
	private ProgramRel relIM;

	private Slicer slicer;

	public void run()
	{
		relExtraInvkComp = (ProgramRel) ClassicProject.g().getTrgt("extraInvkComp");
		relExtraInvkComp.load();

		relExtraInvkCompositeArg = (ProgramRel) ClassicProject.g().getTrgt("extraInvkCompositeArg");
		relExtraInvkCompositeArg.load();

		relIM = (ProgramRel) ClassicProject.g().getTrgt("ci_IM");
		relIM.load();

		relMI = (ProgramRel) ClassicProject.g().getTrgt("MI");
		relMI.load();

		Map<SootMethod,List<Pair<Stmt,SootMethod>>> cg = new HashMap();
		final ProgramRel relIntentExtraMeths = (ProgramRel) ClassicProject.g().getTrgt("IntentExtraMeths");
		relIntentExtraMeths.load();
		Iterable<SootMethod> mIt = relIntentExtraMeths.getAry1ValTuples();
		for(SootMethod m : mIt){			
			List<Pair<Stmt,SootMethod>> callers = cg.get(m);
			if(callers == null){
				callers = new ArrayList();
				cg.put(m, callers);
			}
			for(Object cs : callsitesFor(m)){
				Stmt stmt = (Stmt) cs;
				SootMethod src = containerMethFor(stmt);
				callers.add(new Pair(stmt, src));
			}
		}

		relIM.close();
		relMI.close();
		relIntentExtraMeths.close();

		slicer = new Slicer();

		for(Map.Entry<SootMethod,List<Pair<Stmt,SootMethod>>> e : cg.entrySet()){
			SootMethod m = e.getKey();
			for(Pair<Stmt,SootMethod> pair : e.getValue()){
				Stmt stmt = pair.val0;
				SootMethod src = pair.val1;
				Iterable<String> targetIt = getIntentTargets(stmt);
				System.out.println("%% Interface: Target = " + concat(targetIt) + " " + ifaceStr(m, stmt, src));
			}
		}

		slicer.finish();

		relExtraInvkComp.close();
		relExtraInvkCompositeArg.close();
	}

	private String ifaceStr(SootMethod m, Stmt stmt, SootMethod src)
	{
		String iface = "";
		InvokeExpr ie = stmt.getInvokeExpr();

		String name = m.getName();
		if(name.startsWith("setDataAndType"))			
			return "Data: " + reachingDefs(ie, 0, stmt, src) + " DataType: " + reachingDefs(ie, 1, stmt, src);
		else if(name.startsWith("setData"))
		   	return "Data: " + reachingDefs(ie, 0, stmt, src);
		else if(name.startsWith("get"))
			return "Key: " + reachingDefs(ie, 0, stmt, src) + " ParameterType: [" + m.getReturnType().toString()+"]";
		else if(name.equals("putExtras"))
			;//TODO
		else if(name.startsWith("put"))
			return "Key: " + reachingDefs(ie, 0, stmt, src) + " ParameterType: " + getType(stmt);

		return iface;
	}

	private String reachingDefs(InvokeExpr ie, int argIndex, Stmt stmt, SootMethod src)
	{
		Value arg = ie.getArg(argIndex);
		Set<String> vals;
		if(arg instanceof StringConstant){
			vals = new HashSet();
			vals.add(((StringConstant) arg).value);
		} else
			vals = slicer.evaluate((Local) arg, stmt, src);
		if(vals == null)
			return "";
		else
			return concat(vals);
	}

	private String getType(Unit invkUnit)
	{
		/*
		String name = m.getName();		
		if(name.equals("putCharSequenceArrayListExtra"))
			return "java.util.ArrayList<java.lang.CharSequence>";
		else if(name.equals("putIntegerArrayListExtra"))
			return "java.util.ArrayList<java.lang.Integer>";
		else if(name.equals("putParcelableArrayListExtra"))
			return "java.util.ArrayList<android.os.Parcelable>";
		else if(name.equals("putStringArrayListExtra"))
			return "java.util.ArrayList<java.lang.String>";
		else { System.out.println("debug: "+ m.getSignature());
			return m.getParameterType(1).toString(); }
		*/
		RelView view = relExtraInvkCompositeArg.getView();
        view.selectAndDelete(0, invkUnit);
        Iterable<Type> types = view.getAry1ValTuples();
		return concat(types);
	}

	private String concat(Iterable vals)
	{
		String ss = "[";
		boolean first = true;
		for(Object s : vals){
			if(!first)
				ss += ", ";
			else 
				first = false;
			ss += s.toString();
		}
		ss += "]";
		return ss;
	}
	
	private Iterable<String> getIntentTargets(Unit invkUnit)
	{
        RelView view = relExtraInvkComp.getView();
        view.selectAndDelete(0, invkUnit);
        return view.getAry1ValTuples();
    }

	private Iterable<Object> callsitesFor(SootMethod meth) 
	{
		RelView viewIM = relIM.getView();
        viewIM.selectAndDelete(1, meth);
        return viewIM.getAry1ValTuples();
    }

	private SootMethod containerMethFor(Unit invkUnit) 
	{
		RelView view = relMI.getView();
        view.selectAndDelete(1, invkUnit);
        Iterable<SootMethod> it = view.getAry1ValTuples();
		return it.iterator().next();
    }

}