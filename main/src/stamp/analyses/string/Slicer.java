package stamp.analyses.string;

import soot.SootMethod;
import soot.Local;
import soot.Immediate;
import soot.Value;
import soot.SootField;
import soot.Unit;
import soot.jimple.StringConstant;
import soot.jimple.NullConstant;
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

import shord.program.Program;
import shord.analyses.VarNode;
import shord.analyses.AllocNode;
import shord.analyses.LocalVarNode;
import shord.analyses.DomV;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;

import stamp.srcmap.sourceinfo.abstractinfo.AbstractSourceInfo;

import chord.bddbddb.Rel.RelView;
import chord.util.tuple.object.Pair;
import chord.util.tuple.object.Trio;

import java.util.*;

/*
  @author Saswat Anand
*/
public class Slicer
{
	private List<Trio<Local,Stmt,SootMethod>> workList;

	private Set<Statement> slice;
	private Set<Pair<Local,Stmt>> visited;

	private List<Pair<Local,Stmt>> mLocalList;
	private SootMethod m;

	private Map<Local,LocalVarNode> localToNode = new HashMap();

	private ProgramRel relIM;
	private ProgramRel relIpt;

	private Map<SootField,List<Trio<Local,Stmt,Immediate>>> fieldToInstanceStores = new HashMap();
	private Map<SootField,List<Pair<Stmt,Immediate>>> fieldToStaticStores = new HashMap();

	public Slicer()
	{
		init();
	}
		
	public void generate(Local l, Stmt s, SootMethod m)
	{
		this.workList = new LinkedList();
		this.slice = new LinkedHashSet();
		this.visited = new HashSet();
		this.mLocalList = null;
		this.m = null;

		workList.add(new Trio(l, s, m));

		while(!workList.isEmpty()){
			Trio<Local,Stmt,SootMethod> t = workList.remove(0);

			Local local = t.val0;
			Stmt stmt = t.val1;
			SootMethod method = t.val2;

			visit(method, stmt, local);			
		}
		
		System.out.println(sliceStr());
	}

	public Set<String> evaluate(Local l, Stmt stmt, SootMethod m)
	{
		generate(l, stmt, m);
		Evaluator evaluator = new Evaluator();
		return evaluator.evaluate(slice, l);
	}

	private void visit(SootMethod method, Stmt s, Local l)
	{
		this.m = method;
		this.mLocalList = new LinkedList();
		mLocalList.add(new Pair(l,s));
		
		ReachingDefsAnalysis ld = new ReachingDefsAnalysis(m.retrieveActiveBody());

		while(!mLocalList.isEmpty()){
			Pair<Local,Stmt> p = mLocalList.remove(0);
			if(visited.contains(p))
				continue;
			visited.add(p);

			Local local = p.val0;
			Stmt useStmt = p.val1;
			
			for(Stmt stmt : ld.getDefsOf(local, useStmt)){
				if(stmt instanceof DefinitionStmt){
					DefinitionStmt ds = (DefinitionStmt) stmt;
					Value leftOp = ds.getLeftOp();
					Value rightOp = ds.getRightOp();
					
					if(local.equals(leftOp)){
						handleDefinitionStmt(ds, (Local) leftOp, rightOp);
					}
				}
				
				if(!stmt.containsInvokeExpr())
					continue;

				InvokeExpr ie = stmt.getInvokeExpr();
				if(!(ie instanceof InstanceInvokeExpr))
					continue;
				Local rcvr = (Local) ((InstanceInvokeExpr) ie).getBase();
				if(!rcvr.equals(local))
					continue;
				String mSig = ie.getMethod().getSignature();

				if(mSig.equals("<java.lang.StringBuilder: void <init>(java.lang.String)>") ||
				   mSig.equals("<java.lang.StringBuffer: void <init>(java.lang.String)>") ||
				   mSig.equals("<java.lang.String: void <init>(java.lang.String)>")){
					Immediate arg = (Immediate) ie.getArg(0);
					slice.add(new Assign(arg, rcvr));
					if(arg instanceof Local){
						mLocalList.add(new Pair((Local) arg, stmt));
					}
				} else if(mSig.equals("<java.lang.StringBuilder: void <init>()>") ||
						  mSig.equals("<java.lang.StringBuffer: void <init>()>") ||
						  mSig.equals("<java.lang.String: void <init>()>")){
					slice.add(new Assign(StringConstant.v(""), rcvr));
				} else if(mSig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>") ||
						  mSig.equals("<java.lang.StringBuffer: java.lang.StringBuffer append(java.lang.String)>")){
					Immediate arg = (Immediate) ie.getArg(0);
					slice.add(new Concat(rcvr, arg, rcvr));
					if(arg instanceof Local){
						mLocalList.add(new Pair((Local) arg, stmt));
					}
					mLocalList.add(new Pair(rcvr, stmt));
				}
			}
		}

	}

	private void handleDefinitionStmt(Stmt dfnStmt, Local local, Value rightOp)
	{
		System.out.println("handleDefinitionStmt: "+dfnStmt);
		if(rightOp instanceof ParameterRef){
			//find caller
			int index = ((ParameterRef) rightOp).getIndex();
			for(Object cs : callsitesFor(m)){
				Stmt callsite = (Stmt) cs;
				InvokeExpr ie = callsite.getInvokeExpr();
				Immediate arg = (Immediate) ie.getArg(index);
				if(!(arg instanceof NullConstant)){
					Statement s = new Assign(arg, local);
					slice.add(s); //System.out.println(stmtStr(s));
				}
				if(arg instanceof Local){
					Local loc = (Local) arg;
					SootMethod containerMethod = containerMethodFor(loc);
					workList.add(new Trio(loc, callsite, containerMethod));
				}
			}
		} if(rightOp instanceof InstanceFieldRef){
			//alias
			Local base = (Local) ((InstanceFieldRef) rightOp).getBase();
			SootField field = ((InstanceFieldRef) rightOp).getField();
			for(Pair<Stmt,Immediate> pair : findAlias(base, field)){
				Stmt stmt = pair.val0;
				Immediate alias = pair.val1;
				if(!(alias instanceof NullConstant)){
					Statement s = new Assign(alias, local);
					slice.add(s); //System.out.println(stmtStr(s));
				}
				if(alias instanceof Local){
					SootMethod containerMethod = containerMethodFor((Local) alias);
					workList.add(new Trio((Local) alias, stmt, containerMethod));
				}
			}
		} else if(rightOp instanceof StaticFieldRef){
			SootField field = ((StaticFieldRef) rightOp).getField();
			for(Pair<Stmt,Immediate> pair : findAlias(field)){
				Stmt stmt = pair.val0;
				Immediate alias = pair.val1;
				if(!(alias instanceof NullConstant)){
					Statement s = new Assign(alias, local);
					slice.add(s); //System.out.println(stmtStr(s));
				}
				if(alias instanceof Local){
					SootMethod containerMethod = containerMethodFor((Local) alias);
					workList.add(new Trio((Local) alias, stmt, containerMethod));
				}
			}
		} else if(rightOp instanceof ArrayRef){
			Statement s = new Havoc(local);
			slice.add(s); //System.out.println(stmtStr(s));
		} else if(rightOp instanceof InvokeExpr){
			InvokeExpr ie = (InvokeExpr) rightOp;
			String mSig = ie.getMethod().getSignature();
			if(mSig.equals("<java.lang.StringBuilder: java.lang.String toString()>") ||
			   mSig.equals("<java.lang.StringBuffer: java.lang.String toString()>")){
				Local rcvr = (Local) ((VirtualInvokeExpr) ie).getBase();
				Statement s = new Assign(rcvr, local);
				slice.add(s); //System.out.println(stmtStr(s));
				mLocalList.add(new Pair(rcvr, dfnStmt));
			} else if(mSig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>") ||
					  mSig.equals("<java.lang.StringBuffer: java.lang.StringBuffer append(java.lang.String)>")){
				Immediate arg = (Immediate) ie.getArg(0);
				Local rcvr = (Local) ((VirtualInvokeExpr) ie).getBase();
				Statement s = new Concat(rcvr, arg, local);
				slice.add(s); //System.out.println(stmtStr(s));
				if(arg instanceof Local){
					mLocalList.add(new Pair((Local) arg, dfnStmt));
				}
				mLocalList.add(new Pair(rcvr, dfnStmt));
			} else if(mSig.equals("<java.lang.String: java.lang.String concat(java.lang.String)>")){
				Immediate arg = (Immediate) ie.getArg(0);
				Local rcvr = (Local) ((VirtualInvokeExpr) ie).getBase();
				Statement s = new Concat(rcvr, arg, local);
				slice.add(s); //System.out.println(stmtStr(s));
				if(arg instanceof Local){
					mLocalList.add(new Pair((Local) arg, dfnStmt));
				}
				mLocalList.add(new Pair(rcvr, dfnStmt));
			} else {
				for(SootMethod callee : calleesFor(dfnStmt)){
					if(AbstractSourceInfo.isFrameworkClass(callee.getDeclaringClass())){
						Statement s = new Havoc(local);
						slice.add(s); //System.out.println(stmtStr(s));
					} else {
						for(Pair<Stmt,Immediate> pair : retsFor(callee)){
							Stmt stmt = pair.val0;
							Immediate r = pair.val1;
							if(!(r instanceof NullConstant)){
								Statement s = new Assign(r, local);
								slice.add(s); //System.out.println(stmtStr(s));
							}
							if(r instanceof Local)
								workList.add(new Trio((Local) r, stmt, callee));
						}
					}
				}
			}
		} else if(rightOp instanceof Immediate){
			if(!(rightOp instanceof NullConstant)){
				Statement s = new Assign((Immediate) rightOp, local);
				slice.add(s); //System.out.println(stmtStr(s));
			}
			if(rightOp instanceof Local)
				mLocalList.add(new Pair((Local) rightOp, dfnStmt));
		} else if(rightOp instanceof NewExpr){
			//dont cause havoc
		} else {
			Statement s = new Havoc(local);
			slice.add(s); //System.out.println(stmtStr(s));
		}
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
	}

	public void finish()
	{
		relIM.close();
		relIpt.close();
	}

	private Iterable<Object> callsitesFor(SootMethod meth) 
	{
		RelView viewIM = relIM.getView();
        viewIM.selectAndDelete(1, meth);
        return viewIM.getAry1ValTuples();
    }

	private Iterable<SootMethod> calleesFor(Stmt stmt) 
	{
		RelView viewIM = relIM.getView();
        viewIM.selectAndDelete(0, stmt);
        Iterable<SootMethod> it = viewIM.getAry1ValTuples();
		return it;
    }

	private SootMethod containerMethodFor(Local local) 
	{
		return localToNode.get(local).meth;
    }
	
	private Iterable<Pair<Stmt,Immediate>> findAlias(Local local, SootField f)
	{
		//Store(u:V,f:F,v:V)  input   # u.f = v
		List<Pair<Stmt,Immediate>> aliases = new ArrayList();

		VarNode vn = localToNode.get(local);
		RelView viewIpt1 = relIpt.getView();
		viewIpt1.selectAndDelete(0, vn);
		Iterable<AllocNode> os = viewIpt1.getAry1ValTuples();
	
		Iterable<Trio<Local,Stmt,Immediate>> it = fieldToInstanceStores.get(f);
		if(it == null){
			System.out.println("Warning: No stores found for field "+f);
		} else {
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
		return aliases;
	}
	
	private Iterable<Pair<Stmt,Immediate>> findAlias(SootField f)
	{
		return fieldToStaticStores.get(f);
	}

	private Iterable<Pair<Stmt,Immediate>> retsFor(SootMethod m)
	{
		if(!m.isConcrete())
			return Collections.EMPTY_LIST;
		List<Pair<Stmt,Immediate>> rets = new ArrayList();
		for(Unit unit : m.retrieveActiveBody().getUnits()){
			if(!(unit instanceof ReturnStmt))
				continue;
			Immediate retOp = (Immediate) ((ReturnStmt) unit).getOp();
			rets.add(new Pair((Stmt) unit, retOp));
		}
		return rets;
	}
	
	public String sliceStr()
	{
		StringBuilder sb = new StringBuilder();
		for(Statement stmt : slice){
			sb.append(stmtStr(stmt));
		}
		return sb.toString();
	}

	public String stmtStr(Statement stmt)
	{
		if(stmt instanceof Assign){
			return String.format("assign %s %s\n", toStr(((Assign) stmt).left), toStr(((Assign) stmt).right));
		} else if(stmt instanceof Concat){
			Concat concat = (Concat) stmt;
			return String.format("concat %s %s %s\n", toStr(concat.left), toStr(concat.right1), toStr(concat.right2));
		} else if(stmt instanceof Havoc){
			return String.format("havoc %s\n", toStr(((Havoc) stmt).local));
		} else
			assert false;
		return null;
	}
	
	private String toStr(Immediate i)
	{
		if(i instanceof StringConstant)
			return String.format("\"%s\"", ((StringConstant) i).value);
		else{
			Local l = (Local) i;
			SootMethod m = containerMethodFor(l);
			return String.format("%s!%s@%s", l.getName(), l.getType().toString(), m.getSignature());
		}
	}
}