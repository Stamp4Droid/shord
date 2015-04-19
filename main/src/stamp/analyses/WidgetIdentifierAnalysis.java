package stamp.analyses;

import soot.Scene;
import soot.SootField;
import soot.jimple.spark.pag.SparkField;

import shord.analyses.Ctxt;
import shord.analyses.AllocNode;

import shord.project.analyses.ProgramRel;
import shord.project.ClassicProject;

import chord.util.tuple.object.Pair;
import chord.bddbddb.Rel.RelView;

import java.util.*;

public class WidgetIdentifierAnalysis
{
	private Map<Ctxt,Set<Ctxt>> outgoing = new HashMap();
	private Map<Ctxt,Set<Ctxt>> incoming = new HashMap();
	private Map<Ctxt,String> widgetToId = new HashMap();

	private Traverser fwd = new Traverser(outgoing, true);
	private Traverser bwd = new Traverser(incoming, false);
	
	void prepare()
	{
		buildGraph();
		mapWidgetsToIds();
	}

	String findId(Ctxt widgetObj)
	{
		String id = widgetToId.get(widgetObj);
		if(id != null)
			return id;
		
		List<Ctxt> result = fwd.findPath(widgetObj);
		if(result != null){
			int size = result.size();
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < (size-1); i++){
				Ctxt w = result.get(i);
				AllocNode an = (AllocNode) w.getElems()[0];
				builder.append(an.getType().toString()+" > ");
			}
			id = widgetToId.get(result.get(size-1));
			builder.append(id);
			return builder.toString();
		}
		
		result = bwd.findPath(widgetObj);
		if(result != null){
			int size = result.size();
			StringBuilder builder = new StringBuilder();
			id = widgetToId.get(result.get(0));
			builder.append(id + " > ");
			for(int i = 1; i < size; i++){
				Ctxt w = result.get(i);
				AllocNode an = (AllocNode) w.getElems()[0];
				builder.append(an.getType().toString());
				if(i < (size-1))
					builder.append(" > ");					
			}
			return builder.toString();
		}
		
		return null;
	}

	private class Traverser
	{
		Map<Ctxt,Set<Ctxt>> graph;
		boolean forward;

		Traverser(Map<Ctxt,Set<Ctxt>> graph, boolean forward)
		{
			this.graph = graph;
			this.forward = forward;
		}

		List<Ctxt> findPath(Ctxt node)
		{
			List<Ctxt> result = traverse(node, new ArrayList());
			if(result == null)
				return null;
			if(!forward)
				Collections.reverse(result);
			return result;
		}
		
		private List<Ctxt> traverse(Ctxt node, List<Ctxt> path)
		{
			if(path.contains(node))
				return null;
			path.add(node);
			String id = widgetToId.get(node);
			if(id != null)
				return path;
			Set<Ctxt> succs = outgoing.get(node);
			if(succs == null)
				return null; //dead end
			for(Ctxt succ : succs){
				List<Ctxt> pathCopy = new ArrayList();
				pathCopy.addAll(path);
				List<Ctxt> succResult = traverse(succ, pathCopy); 
				if(succResult != null)
					return succResult;
			}
			return null;
		}
	}

	protected void buildGraph()
	{
        ProgramRel relFpt = (ProgramRel) ClassicProject.g().getTrgt("fpt");		
        relFpt.load();

		SootField childFld = Scene.v().getSootClass("android.view.ViewGroup").getFieldByName("child");

		RelView view = relFpt.getView();
		view.selectAndDelete(1, childFld);
		Iterable<Pair<Ctxt,Ctxt>> iter = view.getAry2ValTuples();
		for(Pair<Ctxt,Ctxt> pair : iter){
			Ctxt from = pair.val0;
			Ctxt to = pair.val1;
			
			Set<Ctxt> outs = outgoing.get(from);
			if(outs == null){
				outs = new HashSet();
				outgoing.put(from, outs);
			}
			outs.add(to);
			
			Set<Ctxt> ins = incoming.get(to);
			if(ins == null){
				ins = new HashSet();
				incoming.put(to, ins);
			}
			ins.add(from);
		}
		view.free();
		relFpt.close();
	}

	private void mapWidgetsToIds()
	{
        ProgramRel relFpt = (ProgramRel) ClassicProject.g().getTrgt("fpt");		
        relFpt.load();
 
		RelView view = relFpt.getView();
		view.delete(0);
		Iterable<Pair<SparkField,Ctxt>> iter = view.getAry2ValTuples();
		for(Pair<SparkField,Ctxt> pair : iter){
			if(!(pair.val0 instanceof SootField))
				continue;
			SootField fld = (SootField) pair.val0;
			Ctxt obj = pair.val1;
			String className = fld.getDeclaringClass().getName();
			if(!className.startsWith("stamp.harness.LayoutInflater$"))
				continue;
			String fldSubsig = fld.getSubSignature();
			widgetToId.put(obj, fldSubsig);
		}
		view.free();
		relFpt.close();
	}
}