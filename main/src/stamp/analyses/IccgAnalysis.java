package stamp.analyses;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Local;
import soot.Value;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.StaticFieldRef;

import shord.analyses.VarNode;
import shord.analyses.LocalVarNode;
import shord.analyses.Ctxt;
import shord.analyses.DomC;
import shord.analyses.DomV;
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
	   consumes={"V", "C", "CICM", "pt", "OutLabelArg", "MI", "MmethArg"})
public class IccgAnalysis extends JavaAnalysis
{
	protected Map<Pair<Ctxt,Stmt>,Set<WidgetList>> cache = new HashMap();
	protected Map<Stmt,SootMethod> invkStmtToMethod = new HashMap();
	protected Set<SootMethod> targetMethods = new HashSet();
	protected Map<SootMethod,Map<Ctxt,Set<Pair<Ctxt,Stmt>>>> csCg = new HashMap();
	protected Map<SootMethod,VarNode> onClickMethToArg = new HashMap();
	protected Map<Ctxt,String> widgetToId = new HashMap();
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

			mapWidgetsToIds();
			
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
			
			writeResults(workList);

			relPt.close();

			writer.endArray();
			writer.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}
	private int cacheHit = 0;
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
			Set<WidgetList> cachedResult = cache.get(pair);
			if(cachedResult != null){
				System.out.println("cachehit: "+cacheHit++);
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
		//System.out.println("caching "+callSite+" "+(widgets==null));
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
			debug(callSite, widgets);
		}
	}

	protected void debug(Pair<Ctxt,Stmt> callSite, WidgetList wl)
	{
		Stmt callStmt = callSite.val1;
		System.out.println("debug: stmt: "+callStmt+"@"+invkStmtToMethod.get(callStmt).getSignature()+" "+
						   "ctxt: "+callSite.val0);
		StringBuilder builder = new StringBuilder();
		builder.append("[ ");
		for(Set<Ctxt> widgets : wl){
			builder.append("{");
			for(Ctxt ctxt : widgets){
				String id = widgetToId.get(ctxt);
				if(id == null)
					id = ctxt.toString();
				builder.append(id+", ");
			}
			builder.append("}, ");
		}
		builder.append("]");
		System.out.println(builder.toString());
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
				//prefix.add(0, new Trio(ctxt, invkStmt, widgets));
			
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
		System.out.println("identifyWidgets: ctxt: "+context+" m: "+m.getSignature());
		VarNode vn = onClickMethToArg.get(m);
		assert vn != null;
		RelView ptView = pointsToSetFor(vn, context);
		Iterable<Ctxt> objs = ptView.getAry1ValTuples();
		Set<Ctxt> ret = new HashSet();
		for(Ctxt obj : objs){
			ret.add(obj);
			System.out.println("identifyWidgets: widget: "+obj);
		}		
		ptView.free();
		return ret;
	}

	protected RelView pointsToSetFor(VarNode vn, Ctxt context)
	{
		RelView view = relPt.getView();
		view.selectAndDelete(0, context);
		view.selectAndDelete(1, vn);
		return view;
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

	static class WidgetList extends ArrayList<Set<Ctxt>>//ArrayList<Trio<Ctxt,Stmt,Set<Ctxt>>>
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
				//check for cached result
				System.out.println("Querying "+caller);
				Set<WidgetList> widgetListSet = cache.get(caller);
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
				//for(Trio<Ctxt,Stmt,Set<Ctxt>> widgetInfo : wl){
					//Stmt callSite = widgetInfo.val1;
					//SootMethod meth = invkStmtToMethod.get(callSite);
					//writer.beginObject();
					//writer.name("method").value(meth.getSignature());
					//writer.name("context").value(widgetInfo.val0.toString());
					//writer.name("widgets");
				for(Set<Ctxt> widgets : wl){
					writer.beginArray();
					//Set<Ctxt> widgets = widgetInfo.val2;
					for(Ctxt ctxt : widgets){
						String id = widgetToId.get(ctxt);
						if(id == null)
							id = ctxt.toString();
						writer.value(id);
					}
					writer.endArray();
					//writer.endObject();
				}
				writer.endArray();
			}
			writer.endArray();
			writer.endObject();
		}
	}
	
	protected void mapWidgetsToIds()
	{
		Map<Local,String> localToId = new HashMap();

		SootClass gClass = Scene.v().getSootClass("stamp.harness.G");
		SootMethod gClinit = gClass.getMethod("void <clinit>()");
		for(Unit unit : gClinit.retrieveActiveBody().getUnits()){
			Stmt stmt = (Stmt) unit;
			if(!(stmt instanceof DefinitionStmt))
				continue;
			Value left = ((DefinitionStmt) stmt).getLeftOp();
			if(!(left instanceof StaticFieldRef))
				continue;
			SootField f = ((StaticFieldRef) left).getField();
			if(!f.getDeclaringClass().equals(gClass))
				continue;
			Local right = (Local) ((DefinitionStmt) stmt).getRightOp();
			String id = f.getSubSignature();
			localToId.put(right, id);
		}
		
        DomC domC = (DomC) ClassicProject.g().getTrgt("C");
		Ctxt emptyCtxt = domC.get(0);
        DomV domV = (DomV) ClassicProject.g().getTrgt("V");

        for(VarNode vnode : domV){
            if(!(vnode instanceof LocalVarNode))
				continue;
			LocalVarNode lvn = (LocalVarNode) vnode;
			if(!lvn.meth.equals(gClinit))
				continue;
			String id = localToId.get(lvn.local);
			if(id == null)
				continue;		
			RelView view = pointsToSetFor(vnode, emptyCtxt);
			Iterable<Ctxt> objs = view.getAry1ValTuples();
			for(Ctxt obj : objs)
				widgetToId.put(obj, id);
			view.free();
        }		
	}
}
