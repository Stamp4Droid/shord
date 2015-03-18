package stamp.analyses;

import soot.SootMethod;
import soot.jimple.Stmt;

import shord.analyses.VarNode;
import shord.analyses.Ctxt;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.ClassicProject;

import chord.project.Chord;
import chord.util.tuple.object.Pair;
import chord.util.tuple.object.Trio;
import chord.util.tuple.object.Quad;
import chord.bddbddb.Rel.RelView;

import com.google.gson.stream.JsonWriter;

import java.util.*;
import java.io.*;

/*
 * @author Saswat Anand
 */
@Chord(name="iccg-bdd-java",
	   consumes={"CICM", "pt", "OutLabelArg", "MI", "MmethArg"})
public class IccgAnalysis extends JavaAnalysis
{
	protected Map<Pair<Ctxt,Stmt>,Set<WidgetList>> cache = new HashMap();
	protected Map<Stmt,SootMethod> invkStmtToMethod = new HashMap();
	protected Set<SootMethod> targetMethods = new HashSet();
	protected Map<SootMethod,Map<Ctxt,Set<Pair<Ctxt,Stmt>>>> csCg = new HashMap();
	protected Map<SootMethod,VarNode> onClickMethToArg = new HashMap();
	protected ProgramRel relPt;
	protected JsonWriter writer;

	public void run()
	{
		try{
			String stampOutDir = System.getProperty("stamp.out.dir");
			this.writer = new JsonWriter(new BufferedWriter(new FileWriter(new File(stampOutDir, "flows.json"))));
			writer.setIndent("  ");
			writer.beginArray();

			populateTargetMethods(Arrays.asList(new String[]{"!Activity"}));

			populateOnClickMethToArg();

			readMI();
		
			readCICM();

			relPt = (ProgramRel) ClassicProject.g().getTrgt("pt");
			relPt.load();
			
			List<Pair<SootMethod,Ctxt>> workList = new ArrayList();
			for(SootMethod target : targetMethods){
				System.out.println("contexts of "+target);
				Iterable<Ctxt> allCtxts = allContextsOf(target);
				if(allCtxts == null)
					continue;
				for(Ctxt ctxt : allCtxts){
					System.out.println(ctxt.toString());
					workList.add(new Pair(target, ctxt));
					traverse(target, ctxt, new ArrayList());
				}
				
			}		
			//System.out.println("Total number of paths = "+count);
			
			relPt.close();
			
			writeResults(workList);

			writer.endArray();
			writer.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}

	protected void traverse(SootMethod callee, 
							Ctxt calleeContext, 
							List<Trio<Ctxt,Stmt,Set<Ctxt>>> path)
	{
		Iterable<Pair<Ctxt,Stmt>> callers = callersOf(callee, calleeContext);
		if(callers == null){
			//reached the entry
			cacheResult(Collections.<WidgetList> emptySet(), path);
			return;
		}

		for(Pair<Ctxt,Stmt> pair : callers){
			Ctxt callerContext = pair.val0;
			Stmt callSite = pair.val1;
			
			boolean cyclic = false;
			for(Trio<Ctxt,Stmt,Set<Ctxt>> trio : path){
				Ctxt ctxt = trio.val0;
				Stmt cs = trio.val1;
				if(cs.equals(callSite) && ctxt.equals(callerContext)){
					cyclic = true;
					break;
				}
			}
			if(cyclic)
				continue;

			//check for cached result
			Pair<Stmt,Ctxt> callSiteAndCtxt = new Pair(callSite, callerContext);
			Set<WidgetList> cachedResult = cache.get(callSiteAndCtxt);
			if(cachedResult != null){
				cacheResult(cachedResult, path);
				continue;
			}


			//if callSite is conditional dependent
			//on whether a specific widget is clicked
			//then propagate

			Set<Ctxt> widgets = null;
			SootMethod caller = invkStmtToMethod.get(callSite);
			if(caller.getSubSignature().equals("void onClick(android.view.View)"))
				widgets = identifyWidgets(callerContext, caller);

			List<Trio<Ctxt,Stmt,Set<Ctxt>>> newPath = new ArrayList();
			newPath.addAll(path);
			newPath.add(new Trio(callerContext, callSite, widgets));

			traverse(caller, callerContext, newPath);
		}
	}
	
	protected void cacheResult(Pair<Ctxt,Stmt> callSite, WidgetList widgets)
	{
		Set<WidgetList> ws = cache.get(callSite);
		if(widgets == null || widgets.isEmpty()){
			if(ws == null){
				ws = Collections.<WidgetList> emptySet();
				cache.put(callSite, ws);
			}
		} else{
			if(ws == null || ws.isEmpty()){
				ws = new HashSet();
				cache.put(callSite, ws);
			}
			ws.add(widgets);
		}
	}

	protected void cacheResult(Set<WidgetList> suffixes, List<Trio<Ctxt,Stmt,Set<Ctxt>>> path)
	{
		int size = path.size();		
		WidgetList prefix = new WidgetList();
		for(int i = size-1; i >= 0; i--){
			Trio<Ctxt,Stmt,Set<Ctxt>> trio = path.get(i);
			Ctxt ctxt = trio.val0;
			Stmt invkStmt = trio.val1;
			Set<Ctxt> widgets = trio.val2;

			if(widgets != null)
				prefix.add(0, widgets);
			
			Pair<Ctxt,Stmt> p = new Pair(ctxt,invkStmt);
			if(suffixes.size() > 0){
				for(WidgetList suffix : suffixes){
					WidgetList allWidgets = new WidgetList();
					allWidgets.addAll(prefix);
					allWidgets.addAll(suffix);
					cacheResult(p, allWidgets);
				}
			} else{
				if(prefix.isEmpty()){
					//both suffix and prefix empty
					cacheResult(p, null);
				} else{
					//non-empty prefix, empty suffix
					WidgetList allWidgets = new WidgetList();
					allWidgets.addAll(prefix);
					cacheResult(p, allWidgets);
				}
			}
		}
		
	}

	protected Set<Ctxt> identifyWidgets(Ctxt context, SootMethod m)
	{
		VarNode vn = onClickMethToArg.get(m);
		assert vn != null;
		RelView view = relPt.getView();
		view.selectAndDelete(0, context);
		view.selectAndDelete(1, vn);
		Iterable<Ctxt> objs = view.getAry1ValTuples();
		Set<Ctxt> ret = new HashSet();
		for(Ctxt obj : objs){
			ret.add(obj);
		}		
		view.free();
		return ret;
	}

	void populateTargetMethods(Collection<String> labels)
	{
		ProgramRel rel = (ProgramRel) ClassicProject.g().getTrgt("OutLabelArg");
        rel.load();
		for(String l : labels){
			RelView view = rel.getView();
			view.delete(2); //param index
			view.selectAndDelete(0, l);
			Iterable<SootMethod> meths = view.getAry1ValTuples();
			for(SootMethod m : meths){
				System.out.println("target method: "+m);
				targetMethods.add(m);
			}
		}
		rel.close();
	}

	private Iterable<Ctxt> allContextsOf(SootMethod m)
	{
		Map<Ctxt,Set<Pair<Ctxt,Stmt>>> callers = csCg.get(m);
		if(callers == null)
			return null;
		return callers.keySet();
	}

	private void populateOnClickMethToArg()
	{
		ProgramRel rel = (ProgramRel) ClassicProject.g().getTrgt("MmethArg");
        rel.load();
		Iterable<Trio<SootMethod, Integer, VarNode>> it = rel.getAry3ValTuples();
		for(Trio<SootMethod, Integer, VarNode> trio : it){
			SootMethod m = trio.val0;
			Integer index = trio.val1;
			VarNode vn = trio.val2;
			if(!m.getSubSignature().equals("void onClick(android.view.View)"))
				continue;
			if(index != 1)
				continue;
			onClickMethToArg.put(m, vn);
		}
		rel.close();
	}

	private Iterable<Pair<Ctxt,Stmt>> callersOf(SootMethod m, Ctxt ctxt)
	{
		/*
		Pair<SootMethod,Ctxt> p = new Pair(m,ctxt);
		if(!queries.add(p))
			repeatedQueryCount++;
		totalQueryCount++;
		if(totalQueryCount % 10000 == 0)
			System.out.println("totalQueryCount = "+totalQueryCount+" repeatedQueryCount = "+repeatedQueryCount);
		*/
		Map<Ctxt,Set<Pair<Ctxt,Stmt>>> callers = csCg.get(m);
		if(callers == null)
			return null;
		return callers.get(ctxt);
	}

	private void readCICM()
	{
		ProgramRel relCICM = (ProgramRel) ClassicProject.g().getTrgt("CICM");
        relCICM.load();
		System.out.println("starting to read cicm");

		Iterable<Quad<Ctxt,Stmt,Ctxt,SootMethod>> callEdges = relCICM.getAry4ValTuples();
		for(Quad<Ctxt,Stmt,Ctxt,SootMethod> quad : callEdges){
			Ctxt callerCtxt = quad.val0;
			Stmt invkStmt = quad.val1;
			Ctxt calleeCtxt = quad.val2;
			SootMethod callee = quad.val3;
			
			Map<Ctxt,Set<Pair<Ctxt,Stmt>>> callers = csCg.get(callee);
			if(callers == null){
				callers = new HashMap();
				csCg.put(callee, callers);
			}
			
			Set<Pair<Ctxt,Stmt>> callSites = callers.get(calleeCtxt);
			if(callSites == null){
				callSites = new HashSet();
				callers.put(calleeCtxt, callSites);
			}
			callSites.add(new Pair(callerCtxt, invkStmt));
		}

		System.out.println("finished reading cicm");
		relCICM.close();
	}
	
	private void readMI()
	{
        ProgramRel relMI = (ProgramRel) ClassicProject.g().getTrgt("MI");		
        relMI.load();
        Iterable<Pair<SootMethod,Stmt>> res = relMI.getAry2ValTuples();
        for(Pair<SootMethod,Stmt> pair : res) {
            SootMethod meth = pair.val0;
            Stmt invk = pair.val1;
			invkStmtToMethod.put(invk, meth);
        }
        relMI.close();
	}

	static class WidgetList extends ArrayList<Set<Ctxt>>
	{
	}
	
	void writeResults(List<Pair<SootMethod,Ctxt>> workList) throws IOException
	{
		for(Pair<SootMethod,Ctxt> pair : workList){
			SootMethod target = pair.val0;
			Ctxt targetCtxt = pair.val1;

			Iterable<Pair<Ctxt,Stmt>> callers = callersOf(target, targetCtxt);
			if(callers == null)
				continue;
			
			Set<WidgetList> allWidgetList = new HashSet();

			for(Pair<Ctxt,Stmt> caller : callers){
				Ctxt callerContext = caller.val0;
				Stmt callSite = caller.val1;
				
				//check for cached result
				Pair<Stmt,Ctxt> callSiteAndCtxt = new Pair(callSite, callerContext);
				Set<WidgetList> widgetListSet = cache.get(callSiteAndCtxt);
				allWidgetList.addAll(widgetListSet);
			}

			if(allWidgetList.isEmpty()) 
				continue;

			writer.beginObject();
			writer.name("target");
			writer.value(target.getSignature());
			
			writer.name("context");
			writer.value(targetCtxt.toString());
			
			writer.name("control");
			writer.beginArray();
			for(WidgetList wl : allWidgetList){
				writer.beginArray();
				for(Set<Ctxt> ctxts : wl){
					writer.beginArray();
					for(Ctxt ctxt : ctxts){
						writer.value(ctxt.toString());
					}
					writer.endArray();
				}
				writer.endArray();
			}
			writer.endArray();
			writer.endObject();
		}
	}
}
