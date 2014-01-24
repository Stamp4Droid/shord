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
		this.slice = new LinkedHashSet();
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
						mLocalList.add((Local) arg);
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
						mLocalList.add((Local) arg);
					}
					mLocalList.add(rcvr);
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
					workList.add(new Pair(loc, containerMethod));
				}
			}
		} if(rightOp instanceof InstanceFieldRef){
			//alias
			Local base = (Local) ((InstanceFieldRef) rightOp).getBase();
			SootField field = ((InstanceFieldRef) rightOp).getField();
			for(Immediate alias : findAlias(base, field)){
				if(!(alias instanceof NullConstant)){
					Statement s = new Assign(alias, local);
					slice.add(s); //System.out.println(stmtStr(s));
				}
				if(alias instanceof Local){
					SootMethod containerMethod = containerMethodFor((Local) alias);
					workList.add(new Pair((Local) alias, containerMethod));
				}
			}
		} else if(rightOp instanceof StaticFieldRef){
			SootField field = ((StaticFieldRef) rightOp).getField();
			for(Immediate alias : findAlias(field)){
				if(!(alias instanceof NullConstant)){
					Statement s = new Assign(alias, local);
					slice.add(s); //System.out.println(stmtStr(s));
				}
				if(alias instanceof Local){
					SootMethod containerMethod = containerMethodFor((Local) alias);
					workList.add(new Pair((Local) alias, containerMethod));
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
				mLocalList.add(rcvr);
			} else if(mSig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>") ||
					  mSig.equals("<java.lang.StringBuffer: java.lang.StringBuffer append(java.lang.String)>")){
				Immediate arg = (Immediate) ie.getArg(0);
				Local rcvr = (Local) ((VirtualInvokeExpr) ie).getBase();
				Statement s = new Concat(rcvr, arg, local);
				slice.add(s); //System.out.println(stmtStr(s));
				if(arg instanceof Local){
					mLocalList.add((Local) arg);
				}
				mLocalList.add(rcvr);
			} else if(mSig.equals("<java.lang.String: java.lang.String concat(java.lang.String)>")){
				Immediate arg = (Immediate) ie.getArg(0);
				Local rcvr = (Local) ((VirtualInvokeExpr) ie).getBase();
				Statement s = new Concat(rcvr, arg, local);
				slice.add(s); //System.out.println(stmtStr(s));
				if(arg instanceof Local){
					mLocalList.add((Local) arg);
				}
				mLocalList.add(rcvr);
			} else {
				for(SootMethod callee : calleesFor(dfnStmt)){
					if(AbstractSourceInfo.isFrameworkClass(callee.getDeclaringClass())){
						Statement s = new Havoc(local);
						slice.add(s); //System.out.println(stmtStr(s));
					} else {
						for(Immediate r : retsFor(callee)){
							if(!(r instanceof NullConstant)){
								Statement s = new Assign(r, local);
								slice.add(s); //System.out.println(stmtStr(s));
							}
							if(r instanceof Local)
								workList.add(new Pair((Local) r, callee));
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
				mLocalList.add((Local) rightOp);
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
	
	private Iterable<Immediate> findAlias(Local local, SootField f)
	{
		//Store(u:V,f:F,v:V)  input   # u.f = v
		List<Immediate> aliases = new ArrayList();

		VarNode vn = localToNode.get(local);
		RelView viewIpt1 = relIpt.getView();
		viewIpt1.selectAndDelete(0, vn);
		Iterable<AllocNode> os = viewIpt1.getAry1ValTuples();
	
		Iterable<Pair<Local,Immediate>> it = fieldToInstanceStores.get(f);
		if(it == null){
			System.out.println("Warning: No stores found for field "+f);
		} else {
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
		}
		
		viewIpt1.free();
		return aliases;
	}
	
	private Iterable<Immediate> findAlias(SootField f)
	{
		return fieldToStaticStores.get(f);
	}

	private Iterable<Immediate> retsFor(SootMethod m)
	{
		if(!m.isConcrete())
			return Collections.EMPTY_LIST;
		List<Immediate> rets = new ArrayList();
		for(Unit unit : m.retrieveActiveBody().getUnits()){
			if(!(unit instanceof ReturnStmt))
				continue;
			Immediate retOp = (Immediate) ((ReturnStmt) unit).getOp();
			rets.add(retOp);
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