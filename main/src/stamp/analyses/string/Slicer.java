package stamp.analyses.string;

import soot.SootMethod;
import soot.Local;
import soot.Immediate;
import soot.Value;
import soot.SootField;
import soot.Unit;

import soot.jimple.StringConstant;
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

import shord.program.Program;
import shord.analyses.VarNode;
import shord.analyses.AllocNode;
import shord.analyses.LocalVarNode;
import shord.analyses.DomV;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;

import chord.bddbddb.Rel.RelView;
import chord.util.tuple.object.Pair;

import java.util.*;

/*
  @author Saswat Anand
*/
public class Slicer
{
	private List<Pair<Local,SootMethod>> workList;

	private Set<Statement> slice;
	private Set<Local> visited;

	private List<Local> mLocalList;
	private SootMethod m;

	private Map<Local,LocalVarNode> localToNode = new HashMap();

	private ProgramRel relIM;
	private ProgramRel relIpt;

	private Map<SootField,List<Pair<Local,Immediate>>> fieldToInstanceStores = new HashMap();
	private Map<SootField,List<Immediate>> fieldToStaticStores = new HashMap();

	public Slicer()
	{
		init();
	}
		
	public void generate(Local l, SootMethod m)
	{
		this.workList = new LinkedList();
		this.slice = new HashSet();
		this.visited = new HashSet();
		this.mLocalList = null;
		this.m = null;

		workList.add(new Pair(l,m));

		while(!workList.isEmpty()){
			Pair<Local,SootMethod> p = workList.remove(0);

			Local local = p.val0;
			SootMethod method = p.val1;

			visit(method, local);			
		}
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
		if(s == null){
			s = new Havoc(local);
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
					List<Pair<Local,Immediate>> pairs = fieldToInstanceStores.get(field);
					if(pairs == null){
						pairs = new ArrayList();
						fieldToInstanceStores.put(field, pairs);
					}
					pairs.add(new Pair(base, (Immediate) rightOp));
				} else if(leftOp instanceof StaticFieldRef){
					StaticFieldRef sfr = (StaticFieldRef) leftOp;
					SootField field = sfr.getField();
					List<Immediate> imms = fieldToStaticStores.get(field);
					if(imms == null){
						imms = new ArrayList();
						fieldToStaticStores.put(field, imms);
					}
					imms.add((Immediate) rightOp);
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

	private SootMethod containerMethodFor(Local local) 
	{
		return localToNode.get(local).meth;
    }
	
	private Iterable<Immediate> findAlias(Local local, SootField f)
	{
		//Store(u:V,f:F,v:V)  input   # u.f = v
		List<Immediate> aliases = new ArrayList();

		VarNode vn = localToNode.get(local);
		RelView viewIpt1 = relIpt.getView();
		viewIpt1.selectAndDelete(0, vn);
		Iterable<AllocNode> os = viewIpt1.getAry1ValTuples();
	
		Iterable<Pair<Local,Immediate>> it = fieldToInstanceStores.get(f);
		for(Pair<Local,Immediate> pair : it){
			Local base = pair.val0;
			Immediate alias = pair.val1;
			
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
				aliases.add(alias);
			}
			viewIpt2.free();
		}
		
		viewIpt1.free();
		return aliases;
	}
	
	private Iterable<Immediate> findAlias(SootField f)
	{
		return fieldToStaticStores.get(f);
	}
	
	public String sliceStr()
	{
		StringBuilder sb = new StringBuilder();
		for(Statement stmt : slice){
			if(stmt instanceof Assign){
				sb.append(String.format("assign %s %s\n", ((Assign) stmt).left, ((Assign) stmt).left));
			} else if(stmt instanceof Concat){
				Concat concat = (Concat) stmt;
				sb.append(String.format("concat %s %s %s\n", concat.left, concat.right1, concat.right2));
			} else if(stmt instanceof Havoc){
				sb.append(String.format("havoc %s\n", ((Havoc) stmt).local));
			} else
				assert false;
		}
		return sb.toString();
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