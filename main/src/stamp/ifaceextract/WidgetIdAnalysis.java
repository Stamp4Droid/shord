package stamp.ifaceextract;

import soot.Scene;
import soot.SootMethod;
import soot.Local;
import soot.Immediate;
import soot.Value;
import soot.SootField;
import soot.Unit;
import soot.RefType;
import soot.Type;
import soot.jimple.IntConstant;
import soot.jimple.Constant;
import soot.jimple.Stmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.ArrayRef;
import soot.jimple.ReturnStmt;
import soot.jimple.NewExpr;
import soot.jimple.CastExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.util.NumberedSet;

import shord.program.Program;
import shord.analyses.VarNode;
import shord.analyses.AllocNode;
import shord.analyses.LocalVarNode;
import shord.analyses.DomV;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;

import stamp.app.Widget;
import stamp.app.Component;

import stamp.srcmap.sourceinfo.abstractinfo.AbstractSourceInfo;

import chord.bddbddb.Rel.RelView;
import chord.util.tuple.object.Pair;
import chord.util.tuple.object.Trio;

import java.util.*;


public class WidgetIdAnalysis
{
	private List<Trio<Local,Stmt,SootMethod>> workList;
	private Set<Pair<Local,Stmt>> visited;
	private ProgramRel relIM;
	private ProgramRel relIpt;
	private NumberedSet reachableMeths;

	private Map<Local,LocalVarNode> localToNode = new HashMap();
	private Map<SootField,List<Trio<Local,Stmt,Immediate>>> fieldToInstanceStores = new HashMap();
	private Map<SootField,List<Pair<Stmt,Immediate>>> fieldToStaticStores = new HashMap();

	private Set<String> ids;

	public WidgetIdAnalysis()
	{
		init();
	}

	private void init()
	{
        DomV domV = (DomV) ClassicProject.g().getTrgt("V");
        for(VarNode node : domV){
			if(!(node instanceof LocalVarNode))
				continue;
			Local local = ((LocalVarNode) node).local;
			localToNode.put(local, (LocalVarNode) node);
		}

		relIM = (ProgramRel) ClassicProject.g().getTrgt("ci_IM");
		relIM.load();
		
		relIpt = (ProgramRel) ClassicProject.g().getTrgt("ci_pt");		
		relIpt.load();

		//populate fieldToInstanceStores and fieldToStaticStores
		Iterator mIt = Program.g().scene().getReachableMethods().listener();
		while(mIt.hasNext()){
			SootMethod m = (SootMethod) mIt.next();
			if(!m.isConcrete())
				continue;
			for(Unit unit : m.retrieveActiveBody().getUnits()){
				Stmt stmt = (Stmt) unit;
				if(!stmt.containsFieldRef())
					continue;
				Value leftOp = ((DefinitionStmt) stmt).getLeftOp();
				Value rightOp = ((DefinitionStmt) stmt).getRightOp();
				if(leftOp instanceof InstanceFieldRef){
					InstanceFieldRef ifr = (InstanceFieldRef) leftOp;
					Local base = (Local) ifr.getBase();
					SootField field = ifr.getField();
					List<Trio<Local,Stmt,Immediate>> triples = fieldToInstanceStores.get(field);
					if(triples == null){
						triples = new ArrayList();
						fieldToInstanceStores.put(field, triples);
					}
					triples.add(new Trio(base, stmt, (Immediate) rightOp));
				} else if(leftOp instanceof StaticFieldRef){
					StaticFieldRef sfr = (StaticFieldRef) leftOp;
					SootField field = sfr.getField();
					List<Pair<Stmt,Immediate>> imms = fieldToStaticStores.get(field);
					if(imms == null){
						imms = new ArrayList();
						fieldToStaticStores.put(field, imms);
					}
					imms.add(new Pair(stmt, (Immediate) rightOp));
				}
			}
		}

		computeReachableMeths();
	}
	//new  public  void  field = r;

	public void finish()
	{
		relIpt.close();
		relIM.close();
	}
	
	public Set<String> findIds(Local l, Stmt s, SootMethod m)
	{
		this.ids = new HashSet();
		this.workList = new LinkedList();
		this.visited = new HashSet();

		workList.add(new Trio(l, s, m));

		while(!workList.isEmpty()){
			Trio<Local,Stmt,SootMethod> t = workList.remove(0);

			Local local = t.val0;
			Stmt stmt = t.val1;
			SootMethod method = t.val2;

			if(reachableMeths.contains(method))
				visit(method, stmt, local);			
		}		
		return ids;
	}

	private void visit(SootMethod method, Stmt s, Local l)
	{		
		List<Pair<Local,Stmt>> mLocalList = new LinkedList();
		mLocalList.add(new Pair(l,s));
		
		SimpleLocalDefs sld = new SimpleLocalDefs(new ExceptionalUnitGraph(method.retrieveActiveBody()));

		while(!mLocalList.isEmpty()){
			Pair<Local,Stmt> p = mLocalList.remove(0);
			if(visited.contains(p))
				continue;
			visited.add(p);

			Local local = p.val0;
			Stmt useStmt = p.val1;

			System.out.println("Processing local:"+ local + " useStmt:"+useStmt);

			for(Unit dfnStmt : sld.getDefsOfAt(local, useStmt)){
				System.out.println("dfnStmt: "+dfnStmt);
				Value rhs = ((DefinitionStmt) dfnStmt).getRightOp();
				if(rhs instanceof Local){
					mLocalList.add(new Pair((Local) rhs, dfnStmt));
				}
				else if(rhs instanceof CastExpr){
					mLocalList.add(new Pair((Local) ((CastExpr) rhs).getOp(), dfnStmt));
				}
				else if(rhs instanceof InstanceFieldRef){
					//alias
					Local base = (Local) ((InstanceFieldRef) rhs).getBase();
					SootField field = ((InstanceFieldRef) rhs).getField();
					for(Pair<Stmt,Immediate> pair : findAlias(base, field)){
						Stmt stmt = pair.val0;
						Immediate alias = pair.val1;
						
						if(alias instanceof Local){
							SootMethod containerMethod = containerMethodFor((Local) alias);
							workList.add(new Trio((Local) alias, stmt, containerMethod));
						}
					}
				}
				else if(rhs instanceof InvokeExpr){
					if(invokesFindViewById((Stmt) dfnStmt)){
						int id = ((IntConstant) ((InvokeExpr) rhs).getArg(0)).value;
						Widget w = Program.g().app().widgetWithId(id);
						if(w != null){
							String idStr = w.id;
							idStr = idStr.substring(idStr.indexOf('/')+1);							
							List<Component> cs = w.layout.getComponents();
							if(cs.size() != 0){
								idStr += "@{";
								boolean first = true;
								for(Component c : cs){
									if(!first)
										idStr += ", ";
									else
										first = false;
									idStr += c.name;
								}
								idStr += "}";
							}
							ids.add(idStr);
						}
					}
				}

			}
		}
	}

	private Iterable<Pair<Stmt,Immediate>> findAlias(Local local, SootField f)
	{
		VarNode vn = localToNode.get(local);
		RelView viewIpt1 = relIpt.getView();
		viewIpt1.selectAndDelete(0, vn);
		Iterable<AllocNode> os = viewIpt1.getAry1ValTuples();

		Iterable<Pair<Stmt,Immediate>> ret;	

		Iterable<Trio<Local,Stmt,Immediate>> it = fieldToInstanceStores.get(f);
		if(it == null){
			System.out.println("Warning: No stores found for field "+f);
			ret = Collections.emptyList();
		} else {
			List<Pair<Stmt,Immediate>> aliases = new ArrayList();
			ret = aliases;
			for(Trio<Local,Stmt,Immediate> trio : it){
				Local base = trio.val0;
				Stmt stmt = trio.val1;
				Immediate alias = trio.val2;
			
				//check if base and local can point to a common object
				VarNode baseNode = localToNode.get(base);
				RelView viewIpt2 = relIpt.getView();
				viewIpt2.selectAndDelete(0, baseNode);
				
				boolean isAlias = false;
				for(AllocNode o : os){
					if(viewIpt2.contains(o)){
						isAlias = true;
						break;
					}
				}
		
				if(isAlias){
					aliases.add(new Pair(stmt,alias));
				}
				viewIpt2.free();
			}
		}
		
		viewIpt1.free();
		return ret;
	}
	
	private Iterable<Pair<Stmt,Immediate>> findAlias(SootField f)
	{
		Iterable<Pair<Stmt,Immediate>> ret = fieldToStaticStores.get(f);
		if(ret == null)
			ret = Collections.emptySet();
		return ret;
	}

	private SootMethod containerMethodFor(Local local) 
	{
		return localToNode.get(local).meth;
    }

	private Iterable<SootMethod> calleesFor(Stmt stmt) 
	{
		RelView viewIM = relIM.getView();
        viewIM.selectAndDelete(0, stmt);
        Iterable<SootMethod> it = viewIM.getAry1ValTuples();
		return it;
    }

	private boolean invokesFindViewById(Stmt stmt)
	{
		for(SootMethod m : calleesFor(stmt)){
			System.out.println("callee: "+m.getSignature());
			if(m.getSignature().equals("<android.app.Activity: android.view.View findViewById(int)>"))
				return true;
		}
		System.out.println("invokesFindById: false");
		return false;
	}

	private void computeReachableMeths()
	{
		reachableMeths = new NumberedSet(Scene.v().getMethodNumberer());
		final ProgramRel relReachableM = (ProgramRel) ClassicProject.g().getTrgt("ci_reachableM");		
		relReachableM.load();
		Iterable<SootMethod> mIt = relReachableM.getAry1ValTuples();
		for(SootMethod meth : mIt){
			reachableMeths.add(meth);
		}
		relReachableM.close();
	}
}