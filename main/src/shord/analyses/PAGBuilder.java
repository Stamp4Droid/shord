package shord.analyses;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Local;
import soot.Immediate;
import soot.Value;
import soot.Unit;
import soot.Body;
import soot.Type;
import soot.RefType;
import soot.RefLikeType;
import soot.PrimType;
import soot.VoidType;
import soot.NullType;
import soot.AnySubType;
import soot.UnknownType;
import soot.FastHierarchy;
import soot.PatchingChain;
import soot.jimple.Constant;
import soot.jimple.Stmt;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.AnyNewExpr;
import soot.jimple.ThrowStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import soot.jimple.ArrayRef;
import soot.jimple.BinopExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewExpr;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.spark.pag.ArrayElement;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.Tag;
import soot.util.NumberedSet;
import soot.util.NumberedString;

import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.analyses.ProgramDom;
import shord.project.ClassicProject;
import shord.program.Program;

import chord.project.Chord;

import java.util.*;

@Chord(name="base-java", 
	   produces={"M", "Z", "I", "H", "V", "T", "F", "U", "S",
				 "Alloc", "Assign", 
				 "Load", "Store", 
				 "LoadStat", "StoreStat", 
				 "MmethArg", "MmethRet", 
				 "IinvkRet", "IinvkArg", 
				 "VT", "chaIM",
				 "HT", "HTFilter",
				 "MI", "MH",
				 "MV", "MU",
				 "AssignPrim", 
				 "LoadPrim", "StorePrim",
				 "LoadStatPrim", "StoreStatPrim",
				 "MmethPrimArg", "MmethPrimRet", 
				 "IinvkPrimRet", "IinvkPrimArg",
				 "SpecIM", "StatIM",
				 "VirtIM", "SubSig",
				 "Dispatch",
	             "Stub" },
       namesOfTypes = { "M", "Z", "I", "H", "V", "T", "F", "U", "S"},
       types = { DomM.class, DomZ.class, DomI.class, DomH.class, DomV.class, DomT.class, DomF.class, DomU.class, DomS.class},
	   namesOfSigns = { "Alloc", "Assign", 
						"Load", "Store", 
						"LoadStat", "StoreStat", 
						"MmethArg", "MmethRet", 
						"IinvkRet", "IinvkArg", 
						"VT", "chaIM",
						"HT", "HTFilter",
						"MI", "MH",
						"MV", "MU",
						"AssignPrim", 
						"LoadPrim", "StorePrim",
						"LoadStatPrim", "StoreStatPrim",
						"MmethPrimArg", "MmethPrimRet", 
						"IinvkPrimRet", "IinvkPrimArg",
				                "SpecIM", "StatIM",
				                "VirtIM", "SubSig",
						"Dispatch",
                        "Stub" },
	   signs = { "V0,H0:V0_H0", "V0,V1:V0xV1",
				 "V0,V1,F0:F0_V0xV1", "V0,F0,V1:F0_V0xV1",
				 "V0,F0:F0_V0", "F0,V0:F0_V0",
				 "M0,Z0,V0:M0_V0_Z0", "M0,Z0,V0:M0_V0_Z0",
				 "I0,Z0,V0:I0_V0_Z0", "I0,Z0,V0:I0_V0_Z0",
				 "V0,T0:T0_V0", "I0,M0:I0_M0",
				 "H0,T0:H0_T0", "H0,T0:H0_T0",
				 "M0,I0:M0_I0", "M0,H0:M0_H0",
				 "M0,V0:M0_V0", "M0,U0:M0_U0",
				 "U0,U1:U0xU1",
				 "U0,V0,F0:U0_V0_F0", "V0,F0,U0:U0_V0_F0",
				 "U0,F0:U0_F0", "F0,U0:U0_F0",
				 "M0,Z0,U0:M0_U0_Z0", "M0,Z0,U0:M0_U0_Z0",
				 "I0,Z0,U0:I0_U0_Z0", "I0,Z0,U0:I0_U0_Z0",
				 "I0,M0:I0_M0", "I0,M0:I0_M0",
				 "I0,M0:I0_M0", "M0,S0:M0_S0",
				 "T0,S0,M0:T0_S0_M0",
                 "M0:M0" }
	   )
public class PAGBuilder extends JavaAnalysis
{
	private ProgramRel relAlloc;//(l:V,h:H)
	private ProgramRel relAssign;//(l:V,r:V)
	private ProgramRel relLoad;//(l:V,b:V,f:F)
	private ProgramRel relStore;//(b:V,f:F,r:V)
	private ProgramRel relLoadStat;//(l:V,f:F)
	private ProgramRel relStoreStat;//(f:F,r:V)

    private ProgramRel relMmethArg;//(m:M,z:Z,v:V)
    private ProgramRel relMmethRet;//(m:M,z:Z,v:V)
    private ProgramRel relIinvkRet;//(i:I,n:Z,v:V)
    private ProgramRel relIinvkArg;//(i:I,n:Z,v:V)
	
	private ProgramRel relAssignPrim;//(l:U,r:U)
	private ProgramRel relLoadPrim;//(l:U,b:V,f:F)
	private ProgramRel relStorePrim;//(b:V,f:F,r:U)
	private ProgramRel relLoadStatPrim;//(l:U,f:F)
	private ProgramRel relStoreStatPrim;//(f:F,r:U)

    private ProgramRel relMmethPrimArg;//(m:M,z:Z,u:U)
    private ProgramRel relMmethPrimRet;//(m:M,z:Z,u:U)
    private ProgramRel relIinvkPrimRet;//(i:I,n:Z,u:U)
    private ProgramRel relIinvkPrimArg;//(i:I,n:Z,u:U)

	private ProgramRel relSpecIM;//(i:I,m:M)
	private ProgramRel relStatIM;//(i:I,m:M)
	private ProgramRel relVirtIM;//(i:I,m:M)
	private ProgramRel relSubSig;//(m:M,s:S)
	private ProgramRel relDispatch;//(t:T,s:S,m:M)

	private ProgramRel relVT;
	private ProgramRel relHT;
	private ProgramRel relHTFilter;
	private ProgramRel relMI;
	private ProgramRel relMH;
	private ProgramRel relMV;
	private ProgramRel relMU;

	private DomV domV;
	private DomU domU;
	private DomH domH;
	private DomZ domZ;
	private DomI domI;

	private int maxArgs = -1;
	private FastHierarchy fh;
	public static NumberedSet stubMethods;

	public static final boolean ignoreStubs = false;

	void openRels()
	{
		relAlloc = (ProgramRel) ClassicProject.g().getTrgt("Alloc");
		relAlloc.zero();
		relAssign = (ProgramRel) ClassicProject.g().getTrgt("Assign");
		relAssign.zero();
		relLoad = (ProgramRel) ClassicProject.g().getTrgt("Load");
		relLoad.zero();
		relStore = (ProgramRel) ClassicProject.g().getTrgt("Store");
		relStore.zero();
		relLoadStat = (ProgramRel) ClassicProject.g().getTrgt("LoadStat");
		relLoadStat.zero();
		relStoreStat = (ProgramRel) ClassicProject.g().getTrgt("StoreStat");
		relStoreStat.zero();

		relMmethArg = (ProgramRel) ClassicProject.g().getTrgt("MmethArg");
		relMmethArg.zero();
		relMmethRet = (ProgramRel) ClassicProject.g().getTrgt("MmethRet");
		relMmethRet.zero();
		relIinvkRet = (ProgramRel) ClassicProject.g().getTrgt("IinvkRet");
		relIinvkRet.zero();
		relIinvkArg = (ProgramRel) ClassicProject.g().getTrgt("IinvkArg");
		relIinvkArg.zero();
		relVT = (ProgramRel) ClassicProject.g().getTrgt("VT");
        	relVT.zero();
		relHT = (ProgramRel) ClassicProject.g().getTrgt("HT");
        	relHT.zero();
		relHTFilter = (ProgramRel) ClassicProject.g().getTrgt("HTFilter");
		relHTFilter.zero();
		relMI = (ProgramRel) ClassicProject.g().getTrgt("MI");
        	relMI.zero();
		relMH = (ProgramRel) ClassicProject.g().getTrgt("MH");
        	relMH.zero();
		relMV = (ProgramRel) ClassicProject.g().getTrgt("MV");
        	relMV.zero();
		relMU = (ProgramRel) ClassicProject.g().getTrgt("MU");
        	relMU.zero();

		relAssignPrim = (ProgramRel) ClassicProject.g().getTrgt("AssignPrim");
		relAssignPrim.zero();
		relLoadPrim = (ProgramRel) ClassicProject.g().getTrgt("LoadPrim");
		relLoadPrim.zero();
		relStorePrim = (ProgramRel) ClassicProject.g().getTrgt("StorePrim");
		relStorePrim.zero();
		relLoadStatPrim = (ProgramRel) ClassicProject.g().getTrgt("LoadStatPrim");
		relLoadStatPrim.zero();
		relStoreStatPrim = (ProgramRel) ClassicProject.g().getTrgt("StoreStatPrim");
		relStoreStatPrim.zero();

		relMmethPrimArg = (ProgramRel) ClassicProject.g().getTrgt("MmethPrimArg");
		relMmethPrimArg.zero();
		relMmethPrimRet = (ProgramRel) ClassicProject.g().getTrgt("MmethPrimRet");
		relMmethPrimRet.zero();
		relIinvkPrimRet = (ProgramRel) ClassicProject.g().getTrgt("IinvkPrimRet");
		relIinvkPrimRet.zero();
		relIinvkPrimArg = (ProgramRel) ClassicProject.g().getTrgt("IinvkPrimArg");
		relIinvkPrimArg.zero();

		relSpecIM = (ProgramRel) ClassicProject.g().getTrgt("SpecIM");
		relSpecIM.zero();
		relStatIM = (ProgramRel) ClassicProject.g().getTrgt("StatIM");
		relStatIM.zero();
		relVirtIM = (ProgramRel) ClassicProject.g().getTrgt("VirtIM");
		relVirtIM.zero();
		relSubSig = (ProgramRel) ClassicProject.g().getTrgt("SubSig");
		relSubSig.zero();
		relDispatch = (ProgramRel) ClassicProject.g().getTrgt("Dispatch");
		relDispatch.zero();
	}
	
	void saveRels()
	{
		relAlloc.save();
		relAssign.save();
		relLoad.save();
		relStore.save();
		relLoadStat.save();
		relStoreStat.save();

		relMmethArg.save();
		relMmethRet.save();
		relIinvkRet.save();
		relIinvkArg.save();
		relVT.save();
		relHT.save();
		relHTFilter.save();
		relMI.save();
		relMH.save();
		relMV.save();
		relMU.save();

		relAssignPrim.save();
		relLoadPrim.save();
		relStorePrim.save();
		relLoadStatPrim.save();
		relStoreStatPrim.save();

		relMmethPrimArg.save();
		relMmethPrimRet.save();
		relIinvkPrimRet.save();
		relIinvkPrimArg.save();

		relSpecIM.save();
		relStatIM.save();
		relVirtIM.save();
		relSubSig.save();
		relDispatch.save();
	}



	void Alloc(VarNode l, AllocNode h)
	{
		assert l != null;
		relAlloc.add(l, h);
		//relAlloc.add(l, unit2Node.get(h));
	}

	void Assign(VarNode l, VarNode r)
	{
		if(l == null || r == null)
			return;
		relAssign.add(l, r);
	}

	void Load(LocalVarNode l, LocalVarNode b, SparkField f)
	{
		if(b == null || l == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relLoad.add(l, b, f);
	}

	void Store(LocalVarNode b, SparkField f, LocalVarNode r)
	{
		if(b == null || r == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relStore.add(b, f, r);
	}

	void LoadStat(LocalVarNode l, SootField f)
	{
		if(l == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relLoadStat.add(l, f);
	}

	void StoreStat(SootField f, LocalVarNode r)
	{
		if(r == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relStoreStat.add(f, r);
	}

	void MmethArg(SootMethod m, int index, VarNode v)
	{
		if(v == null)
			return;
		relMmethArg.add(m, new Integer(index), v);
	}

	void MmethRet(SootMethod m, RetVarNode v)
	{
		if(v == null)
			return;
		relMmethRet.add(m, new Integer(0), v);
	}
	
	void IinvkArg(Unit invkUnit, int index, LocalVarNode v)
	{
		if(v == null)
			return;
		relIinvkArg.add(invkUnit, new Integer(index), v);
	}

	void IinvkRet(Unit invkUnit, LocalVarNode v)
	{
		if(v == null)
			return;
		relIinvkRet.add(invkUnit, new Integer(0), v);
	}

	void AssignPrim(VarNode l, VarNode r)
	{
		if(l == null || r == null)
			return;
		relAssignPrim.add(l, r);
	}

	void LoadPrim(LocalVarNode l, LocalVarNode b, SparkField f)
	{
		if(b == null || l == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relLoadPrim.add(l, b, f);
	}

	void StorePrim(LocalVarNode b, SparkField f, LocalVarNode r)
	{
		if(b == null || r == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relStorePrim.add(b, f, r);
	}

	void LoadStatPrim(LocalVarNode l, SootField f)
	{
		if(l == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relLoadStatPrim.add(l, f);
	}

	void StoreStatPrim(SootField f, LocalVarNode r)
	{
		if(r == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relStoreStatPrim.add(f, r);
	}

	void MmethPrimArg(SootMethod m, int index, VarNode v)
	{
		if(v == null)
			return;
		relMmethPrimArg.add(m, new Integer(index), v);
	}

	void MmethPrimRet(SootMethod m, RetVarNode v)
	{
		if(v == null)
			return;
		relMmethPrimRet.add(m, new Integer(0), v);
	}
	
	void IinvkPrimArg(Unit invkUnit, int index, LocalVarNode v)
	{
		if(v == null)
			return;
		relIinvkPrimArg.add(invkUnit, new Integer(index), v);
	}

	void IinvkPrimRet(Unit invkUnit, LocalVarNode v)
	{
		if(v == null)
			return;
		relIinvkPrimRet.add(invkUnit, new Integer(0), v);
	}

    public void growZIfNeeded(int newSize) 
	{
        int oldSize = maxArgs;
		if(newSize <= oldSize)
			return;
        for(int i = oldSize+1; i <= newSize; i++)
            domZ.add(new Integer(i));
        maxArgs = newSize;
    }

	class MethodPAGBuilder
	{
		private ThisVarNode thisVar;
		private RetVarNode retVar;
		private ParamVarNode[] paramVars;
		private SootMethod method;
		private Map<Local,LocalVarNode> localToVarNode;
		private Set<Local> nonPrimLocals;
		private Set<Local> primLocals;
		private Map<Stmt,CastVarNode> stmtToCastNode;
		private Tag containerTag;
		private Set<StubAllocNode> stubSet = new HashSet();
		private boolean isStub;
		private Map<Unit, SiteAllocNode> unit2Node = new HashMap();

		MethodPAGBuilder(SootMethod method)
		{
			this(method, false);
		}

		MethodPAGBuilder(SootMethod method, boolean stub)
		{
			//System.out.println("XY "+method+" "+stub);
			this.method = method;
			this.isStub = stub;
		}

		private HashMap<SootClass,List<SootClass>> classToSubtypes = new HashMap();

		List<SootClass> subTypesOf(SootClass cl)
		{
			List<SootClass> subTypes = classToSubtypes.get(cl);
			if(subTypes != null)
				return subTypes;

			classToSubtypes.put(cl, subTypes = new ArrayList());

			subTypes.add(cl);

			LinkedList<SootClass> worklist = new LinkedList<SootClass>();
			HashSet<SootClass> workset = new HashSet<SootClass>();
			FastHierarchy fh = Program.g().scene().getOrMakeFastHierarchy();

			if(workset.add(cl)) worklist.add(cl);
			while(!worklist.isEmpty()) {
				cl = worklist.removeFirst();
				if(cl.isInterface()) {
					for(Iterator cIt = fh.getAllImplementersOfInterface(cl).iterator(); cIt.hasNext();) {
						final SootClass c = (SootClass) cIt.next();
						if(workset.add(c)) worklist.add(c);
					}
				} else {
					if(cl.isConcrete()) {
						subTypes.add(cl);
					}
					for(Iterator cIt = fh.getSubclassesOf(cl).iterator(); cIt.hasNext();) {
						final SootClass c = (SootClass) cIt.next();
						if(workset.add(c)) worklist.add(c);
					}
				}
			}
			return subTypes;
		}

		
		void pass1()
		{
			growZIfNeeded(method.getParameterCount());
			
			if(!method.isStatic()) {
				thisVar = new ThisVarNode(method);
				domV.add(thisVar);
 			}

			int count = method.getParameterCount();
			if(count > 0){
				paramVars = new ParamVarNode[count];
				int j = 0;
				for(Type pType : method.getParameterTypes()){
					ParamVarNode node = new ParamVarNode(method, j);
					if(pType instanceof RefLikeType){
						domV.add(node);
					} else if(pType instanceof PrimType){
						domU.add(node);
					} else
						assert false;
					paramVars[j++] = node;
				}
			}
			
			Type retType = method.getReturnType();
			if(!(retType instanceof VoidType)){
				retVar = new RetVarNode(method);
				if(retType instanceof RefLikeType) {
					domV.add(retVar);
					//meth2Ret.put(method, retVar);
				} else if(retType instanceof PrimType) {
					domU.add(retVar);
				} else
					assert false;
			} 

			if(!method.isConcrete())
				return;

			//for stub, we don't want to touch it's body.
			if(isStub) {
				//for each method's return types, we add it.
				if((method.getReturnType() instanceof RefType)){
					RefType t = (RefType)method.getReturnType();
					for(SootClass st: subTypesOf(t.getSootClass())){
						StubAllocNode n = new StubAllocNode(st.getType(), method);
						domH.add(n);
						stubSet.add(n);
						//System.out.println("OO "+method+" "+n);
					}
				}
				return;
			} 

			localToVarNode = new HashMap();
			Body body = method.retrieveActiveBody();
			LocalsClassifier lc = new LocalsClassifier(body);
			primLocals = lc.primLocals();
			nonPrimLocals = lc.nonPrimLocals();
			for(Local l : body.getLocals()){
				boolean isPrim = primLocals.contains(l);
				boolean isNonPrim = nonPrimLocals.contains(l);
				if(isPrim || isNonPrim){
					LocalVarNode node = new LocalVarNode(l, method);
					localToVarNode.put(l, node);
					if(isNonPrim)
						domV.add(node);
					if(isPrim)
						domU.add(node);
				}
			}

			stmtToCastNode = new HashMap();
			for(Unit unit : body.getUnits()){
				Stmt s = (Stmt) unit;
				if(s.containsInvokeExpr()){
					int numArgs = s.getInvokeExpr().getArgCount();
					growZIfNeeded(numArgs);
					domI.add(s);
				} else if(s instanceof AssignStmt) {
					Value rightOp = ((AssignStmt) s).getRightOp();
					if(rightOp instanceof AnyNewExpr) {
						SiteAllocNode n = new SiteAllocNode(s);
						domH.add(n);
						unit2Node.put(s, n);
					}
						//domH.add(s);
					else if(rightOp instanceof CastExpr){
						CastExpr castExpr = (CastExpr) rightOp;
						Type castType = castExpr.getCastType();
						if(castType instanceof RefLikeType){
							CastVarNode node =
								new CastVarNode(method, castExpr);
							domV.add(node);
							stmtToCastNode.put(s, node);
						}
					}
				}
			}						
		}

		void pass2()
		{
			int i = 0;
			if(thisVar != null){
				MmethArg(method, i++, thisVar);
				relVT.add(thisVar, method.getDeclaringClass().getType());
				relMV.add(method, thisVar);
			}
			
			if(paramVars != null){
				for(int j = 0; j < paramVars.length; j++){
					Type paramType = method.getParameterType(j);
					ParamVarNode node = paramVars[j];
					if(paramType instanceof RefLikeType){
						MmethArg(method, i, node);
						relVT.add(node, paramType);
						relMV.add(method, node);
					} else if(paramType instanceof PrimType){
						MmethPrimArg(method, i, node);
						relMU.add(method, node);
					} else
						assert false;
					i++;
				}
			}
			
			if(retVar != null){
				Type retType = method.getReturnType();
				if(retType instanceof RefLikeType){
					MmethRet(method, retVar);
					relVT.add(retVar, retType);
					relMV.add(method, retVar);
				} else if(retType instanceof PrimType){
					MmethPrimRet(method, retVar);
					relMU.add(method, retVar);
				} else
					assert false;
			}

			if(!method.isConcrete())
				return;
			
			Collection allocNodes = isStub ? stubSet : unit2Node.values();
			//System.out.println("PP "+method+" "+allocNodes.size());
			for(Object o : allocNodes){
				AllocNode an = (AllocNode) o;
				Type type = an.getType();

				relHT.add(an, type);
				relMH.add(method, an);
				
				Iterator<Type> typesIt = Program.g().getTypes().iterator();
				while(typesIt.hasNext()){
					Type varType = typesIt.next();
					if(canStore(type, varType))
						relHTFilter.add(an, varType);
				}
			}

			if(isStub) {
				for(StubAllocNode an : stubSet)
					Alloc(retVar, an);
				return;
			}

			for(Map.Entry<Local,LocalVarNode> e : localToVarNode.entrySet()){
				LocalVarNode varNode = e.getValue();
				if(nonPrimLocals.contains(e.getKey())){
					relVT.add(varNode, e.getKey().getType()/*UnknownType.v()*/);
					relMV.add(method, varNode);
				}
				if(primLocals.contains(e.getKey())){
					relMU.add(method, varNode);
				}
			}
			for(Map.Entry<Stmt,CastVarNode> e : stmtToCastNode.entrySet()){
				Type castType = ((CastExpr) ((AssignStmt) e.getKey()).getRightOp()).getCastType();
				relVT.add(e.getValue(), castType);
				relMV.add(method, e.getValue());
			}
			
			containerTag = new ContainerTag(method);
			Body body = method.retrieveActiveBody();
			for(Unit unit : body.getUnits()){
				handleStmt((Stmt) unit);
			}
		}

		LocalVarNode nodeFor(Immediate i)
		{
			if(i instanceof Constant)
				return null;
			return localToVarNode.get((Local) i);
		}
		
		void handleStmt(Stmt s)
		{
			if(s.containsInvokeExpr()){
				InvokeExpr ie = s.getInvokeExpr();
				SootMethod callee = ie.getMethod();
				int numArgs = ie.getArgCount();
				relMI.add(method, s);
				s.addTag(containerTag);


				if(ie instanceof SpecialInvokeExpr){
					relSpecIM.add(s,callee);
				}
				
				if(ie instanceof StaticInvokeExpr){
					relStatIM.add(s,callee);
				}
				
				if( (ie instanceof VirtualInvokeExpr) || (ie instanceof InterfaceInvokeExpr)){
					//VirtualInvokeExpr vie = (VirtualInvokeExpr) ie;
					relVirtIM.add(s,callee);
				}

				//handle receiver
				int j = 0;
				if(ie instanceof InstanceInvokeExpr){
					InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
					IinvkArg(s, j, nodeFor((Immediate) iie.getBase()));
					j++;
				}
				
				//handle args
				for(int i = 0; i < numArgs; i++,j++){
					Immediate arg = (Immediate) ie.getArg(i);
					Type argType = callee.getParameterType(i);
					if(argType instanceof RefLikeType)
						IinvkArg(s, j, nodeFor(arg));
					else if(argType instanceof PrimType)
						IinvkPrimArg(s, j, nodeFor(arg));
				}
				
				//return value
				if(s instanceof AssignStmt){
					Local lhs = (Local) ((AssignStmt) s).getLeftOp();
					Type retType = callee.getReturnType();
					if(retType instanceof RefLikeType)
						IinvkRet(s, nodeFor(lhs));
					else if(retType instanceof PrimType)
						IinvkPrimRet(s, nodeFor(lhs));
				}
			} else if(s.containsFieldRef()){
				AssignStmt as = (AssignStmt) s;
				Value leftOp = as.getLeftOp();
				FieldRef fr = s.getFieldRef();
				SootField field = fr.getField();
				Type fieldType = field.getType();
				if(leftOp instanceof Local){
					//load
					if(field.isStatic()){
						if(fieldType instanceof RefLikeType)
							LoadStat(nodeFor((Local) leftOp), field);
						else if(fieldType instanceof PrimType)
							LoadStatPrim(nodeFor((Local) leftOp), field);
					} else{
						Immediate base = (Immediate) ((InstanceFieldRef) fr).getBase();
						if(fieldType instanceof RefLikeType)
							Load(nodeFor((Local) leftOp), nodeFor(base), field);
						else if(fieldType instanceof PrimType)
							LoadPrim(nodeFor((Local) leftOp), nodeFor(base), field);
					}
				}else{
					//store
					assert leftOp == fr;
					Immediate rightOp = (Immediate) as.getRightOp();
					if(field.isStatic()){
						if(fieldType instanceof RefLikeType)
							StoreStat(field, nodeFor(rightOp));
						else if(fieldType instanceof PrimType)
							StoreStatPrim(field, nodeFor(rightOp));
					} else{
						Immediate base = (Immediate) ((InstanceFieldRef) fr).getBase();
						if(fieldType instanceof RefLikeType)
							Store(nodeFor(base), field, nodeFor(rightOp));
						else if(fieldType instanceof PrimType)
							StorePrim(nodeFor(base), field, nodeFor(rightOp));		
					}
				}
			} else if(s.containsArrayRef()) {
				AssignStmt as = (AssignStmt) s;
				Value leftOp = as.getLeftOp();
				ArrayRef ar = s.getArrayRef();
				Immediate base = (Immediate) ar.getBase();
				SparkField field = ArrayElement.v();
				if(leftOp instanceof Local){
					//array read
					Local l = (Local) leftOp;
					if(nonPrimLocals.contains(l))
						Load(nodeFor(l), nodeFor(base), field);
					if(primLocals.contains(l))
						LoadPrim(nodeFor(l), nodeFor(base), field);
				}else{
					//array write
					assert leftOp == ar;
					Value rightOp = as.getRightOp();
					if(rightOp instanceof Local){
						Local r = (Local) rightOp;
						if(nonPrimLocals.contains(r))
							Store(nodeFor(base), field, nodeFor(r));
						if(primLocals.contains(r))
							StorePrim(nodeFor(base), field, nodeFor(r));
					}
				}
			} else if(s instanceof AssignStmt) {
				AssignStmt as = (AssignStmt) s;
				Value leftOp = as.getLeftOp();
				Value rightOp = as.getRightOp();

				if(rightOp instanceof AnyNewExpr){
					AllocNode an = unit2Node.get(s);
					Alloc(nodeFor((Local) leftOp), an);
					s.addTag(containerTag);
				} else if(rightOp instanceof CastExpr){
					Type castType = ((CastExpr) rightOp).getCastType();
					Immediate op = (Immediate) ((CastExpr) rightOp).getOp();
					if(castType instanceof RefLikeType){
						CastVarNode castNode = stmtToCastNode.get(s);
						Assign(castNode, nodeFor(op));
						Assign(nodeFor((Local) leftOp), castNode);
					} else if(castType instanceof PrimType)
						AssignPrim(nodeFor((Local) leftOp), nodeFor(op));
				} else if(leftOp instanceof Local && rightOp instanceof Immediate){
					Local l = (Local) leftOp;
					Immediate r = (Immediate) rightOp;
					if(nonPrimLocals.contains(l))
						Assign(nodeFor(l), nodeFor(r));
					if(primLocals.contains(l))
						AssignPrim(nodeFor(l), nodeFor(r));
				} if(rightOp instanceof NegExpr){
					AssignPrim(nodeFor((Local) leftOp), nodeFor((Immediate) ((NegExpr) rightOp).getOp()));
				}else if(rightOp instanceof BinopExpr){
					LocalVarNode leftNode = nodeFor((Local) leftOp);
					BinopExpr binExpr = (BinopExpr) rightOp;
					Immediate op1 = (Immediate) binExpr.getOp1();
					if(op1 instanceof Local){
						Local l = (Local) op1;
						if(primLocals.contains(l))
							AssignPrim(leftNode, nodeFor(l));	
					}
					Immediate op2 = (Immediate) binExpr.getOp2();
					if(op2 instanceof Local){
						Local l = (Local) op2;
						if(primLocals.contains(l))
							AssignPrim(leftNode, nodeFor(l));
					}
				}
			}else if(s instanceof ReturnStmt){
				Type retType = method.getReturnType();
				Immediate retOp = (Immediate) ((ReturnStmt) s).getOp();
				if(retType instanceof RefLikeType)
					Assign(retVar, nodeFor(retOp));
				else if(retType instanceof PrimType)
					AssignPrim(retVar, nodeFor(retOp));
			}else if(s instanceof IdentityStmt){
				IdentityStmt is = (IdentityStmt) s;
				Local leftOp = (Local) is.getLeftOp();
				Value rightOp = is.getRightOp();
				if(rightOp instanceof ThisRef){
					Assign(nodeFor(leftOp), thisVar);
				} else if(rightOp instanceof ParameterRef){
					int index = ((ParameterRef) rightOp).getIndex();
					Type type = method.getParameterType(index);
					if(type instanceof RefLikeType)
						Assign(nodeFor(leftOp), paramVars[index]);
					else if(type instanceof PrimType)
						AssignPrim(nodeFor(leftOp), paramVars[index]);
				}
			}
		}
	}

	void populateCallgraph()
	{
		CallGraph cg = Program.g().scene().getCallGraph();
		ProgramRel relChaIM = (ProgramRel) ClassicProject.g().getTrgt("chaIM");
        relChaIM.zero();
		Iterator<Edge> edgeIt = cg.listener();
		while(edgeIt.hasNext()){
			Edge edge = edgeIt.next();
			if(!edge.isExplicit() && !edge.isThreadRunCall())
				continue;
			Stmt stmt = edge.srcStmt();
			//int stmtIdx = domI.getOrAdd(stmt);
			SootMethod tgt = (SootMethod) edge.tgt();
			SootMethod src = (SootMethod) edge.src();
			if(tgt.isAbstract())
				assert false : "tgt = "+tgt +" "+tgt.isAbstract();
			if(tgt.isPhantom())
				continue;
			//System.out.println("stmt: "+stmt+" tgt: "+tgt+ "abstract: "+ tgt.isAbstract());
			if(ignoreStubs){
				if(stubMethods.contains(tgt) || (src != null && stubMethods.contains(src)))
					continue;
			}
			if(!stubMethods.contains(src))
				relChaIM.add(stmt, tgt);
		}
		relChaIM.save();
	}

	void populateMethods()
	{
		DomM domM = (DomM) ClassicProject.g().getTrgt("M");
		Program program = Program.g();
		stubMethods = new NumberedSet(Scene.v().getMethodNumberer());
		Iterator<SootMethod> mIt = program.getMethods();
		while(mIt.hasNext()){
			SootMethod m = mIt.next();
			growZIfNeeded(m.getParameterCount());
			if(isStub(m)){
				stubMethods.add(m);
				//System.out.println("stubZZ: "+ m + " "+stubMethods.contains(m));
			}
			domM.add(m);
		}
		domM.save();

		ProgramRel relStub = (ProgramRel) ClassicProject.g().getTrgt("Stub");
        relStub.zero();
		for(Iterator it = stubMethods.iterator(); it.hasNext();){
			SootMethod stub = (SootMethod) it.next();
			relStub.add(stub);
		}
		relStub.save();
	}

	void populateMethodSigs()
	{
		DomS domS = (DomS) ClassicProject.g().getTrgt("S");
		for(Iterator it = Scene.v().getSubSigNumberer().iterator(); it.hasNext();){
			NumberedString s = (NumberedString)it.next();
			domS.add(s.getString());
		}
		domS.save();

	}
	
	void populateFields()
	{
		DomF domF = (DomF) ClassicProject.g().getTrgt("F");
		Program program = Program.g();
		domF.add(ArrayElement.v()); //first add array elem so that it gets index 0
		for(SootClass klass : program.getClasses()){
			for(SootField field : klass.getFields()){
				domF.add(field);
			}
		}
		domF.save();
	}

	public static Map<Type, TypeAllocNode> type2Node = new HashMap();
	
	void populateTypes()
	{
		DomT domT = (DomT) ClassicProject.g().getTrgt("T");
		//domH = (DomH) ClassicProject.g().getTrgt("H");
		Program program = Program.g();
        	Iterator<Type> typesIt = program.getTypes().iterator();
		while(typesIt.hasNext()){
			Type t = typesIt.next();
            		domT.add(t);
            		//domT.add(typesIt.next());
			//add by yu
			//TypeAllocNode n = new StubAllocNode(t);
			//type2Node.put(t, n);
			//domH.add(n);
		}
		domT.save();
	}

	private Map<SootMethod, RetVarNode> meth2Ret = new HashMap();
		
	void populateDomains(List<MethodPAGBuilder> mpagBuilders)
	{
		domZ = (DomZ) ClassicProject.g().getTrgt("Z");

		populateMethodSigs();
		populateMethods();
		populateFields();
		populateTypes();

		domH = (DomH) ClassicProject.g().getTrgt("H");
		domV = (DomV) ClassicProject.g().getTrgt("V");
		domI = (DomI) ClassicProject.g().getTrgt("I");
		domU = (DomU) ClassicProject.g().getTrgt("U");

		Iterator mIt = Program.g().scene().getReachableMethods().listener();
		while(mIt.hasNext()){
			SootMethod m = (SootMethod) mIt.next();
			if(ignoreStubs){
				//We have to do somthing for stub. Check it's return type T, if it's instanceof RefType,
				//for each subtype st of T do:
				// add a tuple<o,r> to Alloc relation. o is the TypeAllocNode corresponding to st.
				//and r is the return variable of the method.
				if(stubMethods.contains(m))
					continue;
			}
			MethodPAGBuilder mpagBuilder = new MethodPAGBuilder(m, stubMethods.contains(m));
			mpagBuilder.pass1();
			mpagBuilders.add(mpagBuilder);

		}

		domH.save();
		domZ.save();
		/*for(Iterator it = stubMethods.iterator(); it.hasNext();){
			SootMethod method = (SootMethod) it.next();
			Type retType = method.getReturnType();
			if(retType instanceof RefType){
				RetVarNode retVar = new RetVarNode(method);
				domV.add(retVar);
				meth2Ret.put(method, retVar);
			} 
		}*/

		domV.save();
		domI.save();
		domU.save();
		//add by yu.
                /*relAlloc = (ProgramRel) ClassicProject.g().getTrgt("Alloc");
		relAlloc.zero();

		relHT = (ProgramRel) ClassicProject.g().getTrgt("HT");
		relHT.zero();
		relMH = (ProgramRel) ClassicProject.g().getTrgt("MH");
		relMH.zero();

		for(Iterator it = stubMethods.iterator(); it.hasNext();){
			SootMethod stub = (SootMethod) it.next();
			if((stub.getReturnType() instanceof RefType)){
				RefType t = (RefType)stub.getReturnType();
				for(SootClass st: subTypesOf(t.getSootClass())){
					relAlloc.add(meth2Ret.get(stub), type2Node.get(st.getType()));
				        System.out.println("realstub:" + relHT + stub + "|||" + st.getType() + type2Node.get(st.getType()));
					relHT.add(type2Node.get(st.getType()), st.getType());
					//relHT.add(type2Node.get(st.getType()), t);
					relMH.add(stub, type2Node.get(st.getType()));
				}
			}
		}*/
	}

	void buildDispatchMap() 
	{
        	Map<SootClass, Set<SootMethod>> dispatchMap = new HashMap<SootClass, Set<SootMethod>>();
		Program program = Program.g();		
		int totalCls = program.getClasses().size();

		while(dispatchMap.size() < totalCls){//Not finish yet. 
			for(SootClass cl : program.getClasses()) {

				//no superclass?
				if(!cl.hasSuperclass()){
					Set methods = new HashSet<SootMethod>();
					for(SootMethod m: cl.getMethods()){
						if(!m.isConcrete()) continue;
						methods.add(m);
					}
					dispatchMap.put(cl, methods);

					continue;
				}
				//this is the right candidate.propagate the method from superclass.
				if( dispatchMap.get(cl)==null && dispatchMap.get(cl.getSuperclass()) != null){

					SootClass supercl = cl.getSuperclass();
					Set<SootMethod> clMethods = new HashSet<SootMethod>();
					for(SootMethod m: cl.getMethods()){
						if(!m.isConcrete()) continue;
						clMethods.add(m);
					}
					//propagate from superclass. 
					for(SootMethod sm: dispatchMap.get(cl.getSuperclass())){
						if(!sm.isConcrete()) continue;
						if(sm.isPrivate()) continue;
						//check whether exists the same subsignature in cl.
						boolean isOveride = false;
						for(SootMethod m: clMethods){
							if(m.getSubSignature().equals(sm.getSubSignature())){
								isOveride = true;
								//System.out.println("override:" + cl + m + " || " + supercl + sm);
								break;
							}
						}
						if(!isOveride) clMethods.add(sm);
					}

					dispatchMap.put(cl, clMethods);
				}
			}
			//System.out.println("mapsize:" + dispatchMap.size() + " VS " + totalCls);
		}

		//create dispatch tuple based on the map.
		Iterator iter = dispatchMap.keySet().iterator();
		while(iter.hasNext()){
			SootClass clazz = (SootClass)iter.next();
			Set cMeths = (Set)dispatchMap.get(clazz);
			for(Object o: cMeths){
                                SootMethod m = (SootMethod) o;
		                relDispatch.add(clazz.getType(), m.getSubSignature(), m);

			}
		}

	}


	boolean isStub(SootMethod method)
	{
		if(!method.isConcrete())
			return false;
		PatchingChain<Unit> units = method.retrieveActiveBody().getUnits();
		Unit unit = units.getFirst();
		while(unit instanceof IdentityStmt)
			unit = units.getSuccOf(unit);

		if(!(unit instanceof AssignStmt))
			return false;
		Value rightOp = ((AssignStmt) unit).getRightOp();
		if(!(rightOp instanceof NewExpr))
			return false;
		//System.out.println(method.retrieveActiveBody().toString());
		if(!((NewExpr) rightOp).getType().toString().equals("java.lang.RuntimeException"))
			return false;
		Local e = (Local) ((AssignStmt) unit).getLeftOp();
		
		//may be there is an assignment (if soot did not optimized it away)
		Local f = null;
		unit = units.getSuccOf(unit);
		if(unit instanceof AssignStmt){
			f = (Local) ((AssignStmt) unit).getLeftOp();
			if(!((AssignStmt) unit).getRightOp().equals(e))
				return false;
			unit = units.getSuccOf(unit);
		}
		//it should be the call to the constructor
		Stmt s = (Stmt) unit;
		if(!s.containsInvokeExpr())
			return false;
		if(!s.getInvokeExpr().getMethod().getSignature().equals("<java.lang.RuntimeException: void <init>(java.lang.String)>"))
			return false;
		unit = units.getSuccOf(unit);
		if(!(unit instanceof ThrowStmt))
			return false;
		Immediate i = (Immediate) ((ThrowStmt) unit).getOp();
		return i.equals(e) || i.equals(f);
	}

	void pass3()
	{
		Iterator<SootMethod> mIt = Program.g().getMethods();
		while(mIt.hasNext()){
			SootMethod m = mIt.next();
			relSubSig.add(m, m.getSubSignature());
		}
	}
	
	void populateRelations(List<MethodPAGBuilder> mpagBuilders)
	{
		openRels();
		for(MethodPAGBuilder mpagBuilder : mpagBuilders)
			mpagBuilder.pass2();

		pass3();
	        buildDispatchMap();
		saveRels();

		populateCallgraph();
	}

	final public boolean canStore(Type objType, Type varType) 
	{
		if(varType instanceof UnknownType) return true;
		if(!(varType instanceof RefLikeType)) return false;
        if(varType == objType) return true;
        if(varType.equals(objType)) return true;
        if(objType instanceof AnySubType) return true;
        if(varType instanceof NullType) return false;
        if(varType instanceof AnySubType) return false;
        return fh.canStoreType(objType, varType);
    }



	public void run()
	{
		Program program = Program.g();		
		//for(SootClass k : program.getClasses()) System.out.println("kk "+k + (k.hasSuperclass() ? k.getSuperclass() : ""));
		program.buildCallGraph();

		fh = Program.g().scene().getOrMakeFastHierarchy();
		List<MethodPAGBuilder> mpagBuilders = new ArrayList();
		populateDomains(mpagBuilders);
		populateRelations(mpagBuilders);

		fh = null;
	}
}
