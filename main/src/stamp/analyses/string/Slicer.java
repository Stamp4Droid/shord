package stamp.analyses.string;

import soot.SootMethod;
import soot.Local;
import soot.Immediate;
import soot.Value;
import soot.SootField;
import soot.Unit;

import soot.jimple.Stmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.ArrayRef;

import shord.analyses.VarNode;
import shord.analyses.LocalVarNode;
import shord.analyses.DomV;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;

import chord.bddbddb.Rel.RelView;
import chord.util.tuple.object.Pair;

import java.util.*;

public class Slicer
{
	private Set<Pair<Local,SootMethod>> seeds = new HashSet();
	private List<Pair<Local,SootMethod>> workList = new LinkedList();

	private Set<Statement> slice = new HashSet();
	private Set<Local> visited = new HashSet();

	private List<Local> mLocalList;
	private SootMethod m;

	private Map<Local,LocalVarNode> localToNode = new HashMap();


	private ProgramRel relIM;
	private ProgramRel relIpt;

	public Slicer()
	{
	}
	
	public void addSeed(Local l, SootMethod m)
	{
		seeds.add(new Pair(l,m));
	}
	
	public void generate()
	{
		init();

		for(Pair<Local,SootMethod> p : seeds)
			workList.add(p);

		while(!workList.isEmpty()){
			Pair<Local,SootMethod> p = workList.remove(0);

			Local l = p.val0;
			SootMethod m = p.val1;

			visit(m, l);			
		}

		finish();
	}
	
	private void visit(SootMethod method, Local l)
	{
		this.m = method;
		this.mLocalList = new LinkedList();
		mLocalList.add(l);
		
		MyLocalDefs ld = new MyLocalDefs(m.retrieveActiveBody());
		Set<Local> visited = new HashSet();

		while(!mLocalList.isEmpty()){
			Local local = mLocalList.remove(0);
			if(visited.contains(local))
				continue;
			visited.add(local);
			
			for(Stmt stmt : ld.getDefsOf(local)){
				if(stmt instanceof DefinitionStmt){
					DefinitionStmt ds = (DefinitionStmt) stmt;
					Value leftOp = ds.getLeftOp();
					Value rightOp = ds.getRightOp();
					
					Statement s = handleDefinitionStmt((Local) leftOp, rightOp);
					if(s != null)
						slice.add(s);
				}
				
				if(!stmt.containsInvokeExpr())
					continue;

				InvokeExpr ie = stmt.getInvokeExpr();
				String mSig = ie.getMethod().getSignature();

				if(mSig.equals("<java.lang.StringBuilder: void <init>(java.lang.String)>") ||
				   mSig.equals("<java.lang.StringBuffer: void <init>(java.lang.String)>") ||
				   mSig.equals("<java.lang.String: void <init>(java.lang.String)>")){
					Immediate arg = (Immediate) ie.getArg(0);
					Local rcvr = (Local) ((SpecialInvokeExpr) ie).getBase();
					slice.add(new Assign(arg, rcvr));
					if(arg instanceof Local){
						mLocalList.add((Local) arg);
					}
				} else if(mSig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>") ||
						  mSig.equals("<java.lang.StringBuffer: java.lang.StringBuffer append(java.lang.String)>")){
					Immediate arg = (Immediate) ie.getArg(0);
					Local rcvr = (Local) ((VirtualInvokeExpr) ie).getBase();
					slice.add(new Concat(rcvr, arg, rcvr));
					if(arg instanceof Local){
						mLocalList.add((Local) arg);
					}
					mLocalList.add(rcvr);
				}
			}
		}

	}

	private Statement handleDefinitionStmt(Local local, Value rightOp)
	{
		Statement s = null;
		
		if(rightOp instanceof ParameterRef){
			//find caller
			int index = ((ParameterRef) rightOp).getIndex();
			for(Object cs : callsitesFor(m)){
				Stmt callsite = (Stmt) cs;
				InvokeExpr ie = callsite.getInvokeExpr();
				Immediate arg = (Immediate) ie.getArg(index);
				s = new Assign(arg, local);
				if(arg instanceof Local){
					Local loc = (Local) arg;
					SootMethod containerMethod = containerMethodFor(loc);
					workList.add(new Pair(loc, containerMethod));
				}
			}
		} if(rightOp instanceof InstanceFieldRef){
			//alias
			Local base = (Local) ((InstanceFieldRef) rightOp).getBase();
			SootField field = ((InstanceFieldRef) rightOp).getField();
			for(Immediate alias : findAlias(base, field)){
				s = new Assign(alias, local);
				if(alias instanceof Local){
					SootMethod containerMethod = containerMethodFor((Local) alias);
					workList.add(new Pair((Local) alias, containerMethod));
				}
			}
		} else if(rightOp instanceof StaticFieldRef){
			SootField field = ((StaticFieldRef) rightOp).getField();
			for(Immediate alias : findAlias(field)){
				s = new Assign(alias, local);
				if(alias instanceof Local){
					SootMethod containerMethod = containerMethodFor((Local) alias);
					workList.add(new Pair((Local) alias, containerMethod));
				}
			}
		} else if(rightOp instanceof ArrayRef){
			
		} else if(rightOp instanceof InvokeExpr){
			InvokeExpr ie = (InvokeExpr) rightOp;
			String mSig = ie.getMethod().getSignature();
			if(mSig.equals("<java.lang.StringBuilder: java.lang.String toString()>") ||
			   mSig.equals("<java.lang.StringBuffer: java.lang.String toString()>")){
				Local rcvr = (Local) ((VirtualInvokeExpr) ie).getBase();
				s = new Assign(rcvr, local);
				mLocalList.add(rcvr);
			} else if(mSig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>") ||
					  mSig.equals("<java.lang.StringBuffer: java.lang.StringBuffer append(java.lang.String)>")){
				Immediate arg = (Immediate) ie.getArg(0);
				Local rcvr = (Local) ((VirtualInvokeExpr) ie).getBase();
				s = new Concat(rcvr, arg, local);
				if(arg instanceof Local){
					mLocalList.add((Local) arg);
				}
				mLocalList.add(rcvr);
			} else if(mSig.equals("<java.lang.String: java.lang.String concat(java.lang.String)>")){
				Immediate arg = (Immediate) ie.getArg(0);
				Local rcvr = (Local) ((VirtualInvokeExpr) ie).getBase();
				s = new Concat(rcvr, arg, local);
				if(arg instanceof Local){
					mLocalList.add((Local) arg);
				}
				mLocalList.add(rcvr);
			}
		} else if(rightOp instanceof Immediate){
			s = new Assign((Immediate) rightOp, local);
			if(rightOp instanceof Local)
				mLocalList.add((Local) rightOp);
		}
		
		return s;
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
		relIpt = (ProgramRel) ClassicProject.g().getTrgt("ci_pt");		
	}

	private void finish()
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

	private SootMethod containerMethodFor(Local local) 
	{
		return localToNode.get(local).meth;
    }
	
	private Iterable<Immediate> findAlias(Local local, SootField f)
	{
		/*
		VarNode vn = localToNode.get(local);
		RelView viewIpt = relIpt.getView();
		viewIM.

		Iterable<Object> vir
		*/
		return null;
	}
	
	private Iterable<Immediate> findAlias(SootField f)
	{
		return null;
	}
}