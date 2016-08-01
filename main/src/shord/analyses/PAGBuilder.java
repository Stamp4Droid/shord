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
import soot.jimple.StringConstant;
import soot.jimple.ClassConstant;
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
import stamp.analyses.SootUtils;
import stamp.harnessgen.*;

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
				 "Dispatch", "Launch", 
                 "classT", "TargetHT", 
                 "sub", "staticTM", 
                 "staticTF", "clinitTM",
				 "TgtAction",
	             "Stub", "COMP", "TgtDataType", "Service", "Receiver", "BootCompleted", "CompIntentAction", "Priority",
                 "Activity", "SmsReceived", "PickWifi", "UmsConnected", "ConnectChg", "BatteryLow", "PhoneState", 
                 "SmsSent", "NewOutCall", "SigStr", "PkgAdd", "PkgChg", "PkgRemove", "PkgReplace", "PkgInstall", "InstallAPK"},

       namesOfTypes = { "M", "Z", "I", "H", "V", "T", "F", "U", "S", "COMP" },
       types = { DomM.class, DomZ.class, DomI.class, DomH.class, DomV.class, DomT.class, DomF.class, DomU.class, DomS.class, DomComp.class},
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
						"Dispatch", "Launch", 
                        "classT", "TargetHT",
                        "sub", "staticTM", 
                        "staticTF", "clinitTM",
						"TgtAction",
                        "Stub", "TgtDataType", "Service", "Receiver", "BootCompleted", "CompIntentAction", "Priority",
                        "Activity", "SmsReceived", "PickWifi", "UmsConnected", "ConnectChg", "BatteryLow", "PhoneState", 
                        "SmsSent", "NewOutCall", "SigStr", "PkgAdd", "PkgChg", "PkgRemove", "PkgReplace", "PkgInstall", "InstallAPK"},

	   signs = { "V0,H0:V0_H0", "V0,V1:V0xV1",
				 "V0,V1,F0:F0_V0xV1", "V0,F0,V1:F0_V0xV1",
				 "V0,F0:F0_V0", "F0,V0:V0_F0",
				 "M0,Z0,V0:M0_V0_Z0", "M0,Z0,V0:M0_V0_Z0",
				 "I0,Z0,V0:I0_V0_Z0", "I0,Z0,V0:I0_V0_Z0",
				 "V0,T0:T0_V0", "I0,M0:I0_M0",
				 "H0,T0:H0_T0", "H0,T0:H0_T0",
				 "M0,I0:M0_I0", "M0,H0:M0_H0",
				 "M0,V0:V0_M0", "M0,U0:M0_U0",
				 "U0,U1:U0xU1",
				 "U0,V0,F0:U0_V0_F0", "V0,F0,U0:U0_V0_F0",
				 "U0,F0:U0_F0", "F0,U0:U0_F0",
				 "M0,Z0,U0:M0_U0_Z0", "M0,Z0,U0:M0_U0_Z0",
				 "I0,Z0,U0:I0_U0_Z0", "I0,Z0,U0:I0_U0_Z0",
				 "I0,M0:I0_M0", "I0,M0:I0_M0",
				 "I0,M0:I0_M0", "M0,S0:M0_S0",
				 "T0,S0,M0:T0_M0_S0", "V0,M0:V0_M0", 
                 "T0:T0", "H0,COMP0:H0_COMP0",
                 "T0,T1:T0_T1", "T0,M0:T0_M0",
                 "T0,F0:F0_T0", "T0,M0:T0_M0",
				 "COMP0,H0:COMP0_H0",
                 "M0:M0", "COMP0,H0:COMP0_H0", "COMP0:COMP0", "COMP0:COMP0", "S0:S0", "COMP0,S0:COMP0_S0", "COMP0,Z0:COMP0_Z0",
                 "COMP0:COMP0", "S0:S0","S0:S0","S0:S0","S0:S0","S0:S0","S0:S0",
                 "S0:S0","S0:S0","S0:S0","S0:S0","S0:S0","S0:S0","S0:S0","S0:S0","COMP0:COMP0"}
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
	private ProgramRel relLaunch;//(v:V,m:M)
	private ProgramRel relTgtAction;//(v:V,h:H)
	private ProgramRel relTgtDataType;//(v:V,h:H)
	private ProgramRel relTargetHT;//(h:H,t:T)


	private ProgramRel relVT;
	private ProgramRel relHT;
	private ProgramRel relHTFilter;
	private ProgramRel relMI;
	private ProgramRel relMH;
	private ProgramRel relMV;
	private ProgramRel relMU;
	private ProgramRel relCompIntentAct;
	private ProgramRel relPriority;

	private DomV domV;
	private DomU domU;
	private DomH domH;
	private DomZ domZ;
	private DomI domI;

    private	DomComp domComp;

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
		relLaunch = (ProgramRel) ClassicProject.g().getTrgt("Launch");
		relLaunch.zero();
		relTgtAction = (ProgramRel) ClassicProject.g().getTrgt("TgtAction");
		relTgtAction.zero();
		relTgtDataType = (ProgramRel) ClassicProject.g().getTrgt("TgtDataType");
		relTgtDataType.zero();
		relTargetHT= (ProgramRel) ClassicProject.g().getTrgt("TargetHT");
		relTargetHT.zero();

		relCompIntentAct = (ProgramRel) ClassicProject.g().getTrgt("CompIntentAction");
        relCompIntentAct.zero();
		relPriority = (ProgramRel) ClassicProject.g().getTrgt("Priority");
        relPriority.zero();
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
		relLaunch.save();
		relTgtAction.save();
		relTgtDataType.save();
		relTargetHT.save();
        relCompIntentAct.save();
        relPriority.save(); 
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


	public static GlobalStringNode gStringNode  = new GlobalStringNode();

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
	    private boolean parseAction = false;
		private Map<Unit, AllocNode> unit2Node = new HashMap();
		private Map<String, AllocNode> action2Node = new HashMap();
		private Map<String, AllocNode> dataType2Node = new HashMap();
		//private Map<Unit, SiteAllocNode> unit2Node = new HashMap();

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
					for(SootClass st: SootUtils.subTypesOf(t.getSootClass())){
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

					if(rightOp instanceof StringConstant) {
						String str = ((StringConstant)rightOp).value;
						if(ICCGBuilder.components.get(str.replaceAll("/",".")) != null){
							StringConstNode n = new StringConstNode(s);
							domH.add(n);
							unit2Node.put(s, n);
						}else if(str.matches("[a-zA-Z]+\\.[a-zA-Z]+.*")){//end with uppercase word.
						//if(str.matches("android.intent.action.*|android.provider.*|.*[A-Z]+$")){
							StringConstNode n = new StringConstNode(s);
							domH.add(n);
							unit2Node.put(s, n);
							//save for targetAction rel.
							action2Node.put(str, n);
						}else if("application/vnd.android.package-archive".equals(str)){ //install apk by data type: 
                            //application/vnd.android.package-archive
    						StringConstNode n = new StringConstNode(s);
							domH.add(n);
							unit2Node.put(s, n);
							//save for targetType rel.
							dataType2Node.put(str, n);
                        }else{
                            //use a global alloc.
							unit2Node.put(s, PAGBuilder.gStringNode);
                        }
					}

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

			//parseAction? in components
			if(!parseAction){//do it only once.
				parseAction = true;
				Iterator iter = ICCGBuilder.components.entrySet().iterator();
				String pkgName = ICCGBuilder.pkgName;
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String key = (String)entry.getKey();
					XmlNode val = (XmlNode)entry.getValue();
					String nodeName = val.getName();

					if(nodeName.indexOf(".") == 0) nodeName = pkgName +  nodeName;
					if(nodeName.indexOf(".") == -1) nodeName = pkgName + "." +  nodeName;
					for(String actionName : val.getActionList()){
						//<nodeName, actionName>
						if(action2Node.get(actionName) != null)
							relTgtAction.add(ICCGBuilder.getCompKey(nodeName), action2Node.get(actionName));
					}
				}

			}

			if(dataType2Node.get("application/vnd.android.package-archive") != null)
			    relTgtDataType.add(PAGBuilder.gInstallAPK, dataType2Node.get("application/vnd.android.package-archive"));
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


		        DomM domM = (DomM) ClassicProject.g().getTrgt("M");
                if(domM.contains(callee)){
                    if(ie instanceof SpecialInvokeExpr){
                        relSpecIM.add(s,callee);
                    }
                    
                    if(ie instanceof StaticInvokeExpr){
                        relStatIM.add(s,callee);
                    }
                    
                    if( (ie instanceof VirtualInvokeExpr) || (ie instanceof InterfaceInvokeExpr)){
                        relVirtIM.add(s,callee);
                    }
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

				//String methSig = callee.getSignature();
				String methSubSig = callee.getSubSignature();
				if(ICCGBuilder.launchList.contains(methSubSig)) {
                    if(methSubSig.equals("void setResult(int,android.content.Intent)")){
					    relLaunch.add(nodeFor(((Immediate)ie.getArg(1))), method);
                    }else{
					    relLaunch.add(nodeFor(((Immediate)ie.getArg(0))), method);
                    }
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

				if(rightOp instanceof StringConstant) {
					String str = ((StringConstant)rightOp).value;
					if(ICCGBuilder.components.get(str.replaceAll("/",".")) != null){
                        if(Scene.v().containsClass(str)){
                            relTargetHT.add(unit2Node.get(s), ICCGBuilder.getCompKey(str));
                        }
						Alloc(nodeFor((Local) leftOp), unit2Node.get(s));
					}else if(str.matches("[a-zA-Z]+\\.[a-zA-Z]+.*")){//end with uppercase word.
						Alloc(nodeFor((Local) leftOp), unit2Node.get(s));
					} else if("application/vnd.android.package-archive".equals(str)){ //install apk by data type: 
						Alloc(nodeFor((Local) leftOp), unit2Node.get(s));
                    }else {
						Alloc(nodeFor((Local) leftOp), PAGBuilder.gStringNode);
                    }

				}

				if(rightOp instanceof ClassConstant) {
					String str = ((ClassConstant)rightOp).value;
					if(ICCGBuilder.components.get(str.replaceAll("/", ".")) != null){
						SootClass clazz = Scene.v().getSootClass("edu.stanford.stamp.harness.Main");
                        String clazzName = str.replaceAll("/", "\\$");
                        if(clazz.declaresFieldByName(clazzName)) {
                            SootField field = clazz.getFieldByName(clazzName);
                            LoadStat(nodeFor((Local) leftOp), field);
                        } else {
                            System.out.println("fatal error in harness..." + clazzName);
                        }
					}
				}

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
		Program prog = Program.g();
		CallGraph cg = Program.g().scene().getCallGraph();
		ProgramRel relChaIM = (ProgramRel) ClassicProject.g().getTrgt("chaIM");
        relChaIM.zero();
		Iterator<Edge> edgeIt = cg.listener();
		while(edgeIt.hasNext()){
			Edge edge = edgeIt.next();
			//if(!edge.isExplicit() && !edge.isThreadRunCall())
            //try saswat's trick
			if(!edge.isExplicit())
				continue;
			Stmt stmt = edge.srcStmt();
			//int stmtIdx = domI.getOrAdd(stmt);
			SootMethod tgt = (SootMethod) edge.tgt();
			SootMethod src = (SootMethod) edge.src();
			if(tgt.isAbstract())
				assert false : "tgt = "+tgt +" "+tgt.isAbstract();
			if(tgt.isPhantom())
				continue;

			if(prog.exclude(tgt) || (src != null && prog.exclude(src)))
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
		domM.add(program.getMainMethod());
		while(mIt.hasNext()){
			SootMethod m = mIt.next();
			growZIfNeeded(m.getParameterCount());
			if(isStub(m)){
				stubMethods.add(m);
				//System.out.println("stubZZ: "+ m + " "+stubMethods.contains(m));
			}

			if(program.exclude(m))
				continue;

            //System.out.println("adding valid method: " + m);
			domM.add(m);
		}
		domM.save();

		ProgramRel relStub = (ProgramRel) ClassicProject.g().getTrgt("Stub");
        relStub.zero();
		for(Iterator it = stubMethods.iterator(); it.hasNext();){
			SootMethod stub = (SootMethod) it.next();
			if(program.exclude(stub))
                continue;

			relStub.add(stub);
		}
		relStub.save();
	}

	void populateMethodSigs()
	{
		Program program = Program.g();
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
		Program prog = Program.g();
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

	    domH.add(PAGBuilder.gStringNode);
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

			if(prog.exclude(m))
				continue;

			MethodPAGBuilder mpagBuilder = new MethodPAGBuilder(m, stubMethods.contains(m));
			mpagBuilder.pass1();
			mpagBuilders.add(mpagBuilder);

		}

		domH.save();
		domZ.save();
		domV.save();
		domI.save();
		domU.save();
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
		DomM domM = (DomM) ClassicProject.g().getTrgt("M");
		Iterator iter = dispatchMap.keySet().iterator();
		while(iter.hasNext()){
			SootClass clazz = (SootClass)iter.next();
			Set cMeths = (Set)dispatchMap.get(clazz);
			for(Object o: cMeths){
                if(!domM.contains(o)) continue;
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
		DomM domM = (DomM) ClassicProject.g().getTrgt("M");
		Iterator<SootMethod> mIt = Program.g().getMethods();
		while(mIt.hasNext()){
			SootMethod m = mIt.next();
            if(domM.contains(m))
			    relSubSig.add(m, m.getSubSignature());
		}
	}
	
	void populateRelations(List<MethodPAGBuilder> mpagBuilders)
	{
		domZ = (DomZ) ClassicProject.g().getTrgt("Z");
        for(Object node : ICCGBuilder.components.keySet()) {
            XmlNode xml = ICCGBuilder.components.get(node);
            for(String pri : xml.getFilterList()) {
                int priInt = Integer.parseInt(pri);
                domZ.add(priInt);
            }
        }
        domZ.save();
        ////////////////////

		openRels();
		for(MethodPAGBuilder mpagBuilder : mpagBuilders)
			mpagBuilder.pass2();

		pass3();
	    buildDispatchMap();

        for(Object node : ICCGBuilder.components.keySet()) {
            XmlNode xml = ICCGBuilder.components.get(node);
            String strNode = (String)node;

            /////////////ACTION
            if(xml.getActionList().contains(boot)) 
                relCompIntentAct.add(strNode, boot);
            if(xml.getActionList().contains(smsRecv)) 
                relCompIntentAct.add(strNode, smsRecv);
            if(xml.getActionList().contains(pickWifi)) 
                relCompIntentAct.add(strNode, pickWifi);
            if(xml.getActionList().contains(umsConn)) 
                relCompIntentAct.add(strNode, umsConn);
            if(xml.getActionList().contains(connChg)) 
                relCompIntentAct.add(strNode, connChg);
            if(xml.getActionList().contains(batLow)) 
                relCompIntentAct.add(strNode, batLow);
            if(xml.getActionList().contains(phoneState)) 
                relCompIntentAct.add(strNode, phoneState);
            if(xml.getActionList().contains(smsSent)) 
                relCompIntentAct.add(strNode, smsSent);
            if(xml.getActionList().contains(newCall)) 
                relCompIntentAct.add(strNode, newCall);
            if(xml.getActionList().contains(sigStr)) 
                relCompIntentAct.add(strNode, sigStr);
            if(xml.getActionList().contains(pkgAdd)) 
                relCompIntentAct.add(strNode, pkgAdd);
            if(xml.getActionList().contains(pkgChg)) 
                relCompIntentAct.add(strNode, pkgChg);
            if(xml.getActionList().contains(pkgRemove)) 
                relCompIntentAct.add(strNode, pkgRemove);
            if(xml.getActionList().contains(pkgRep)) 
                relCompIntentAct.add(strNode, pkgRep);
            if(xml.getActionList().contains(pkgInstall)) 
                relCompIntentAct.add(strNode, pkgInstall);
            ///////////PRIORITY
            for(String pri : xml.getFilterList()) {
                int priInt = Integer.parseInt(pri);
                relPriority.add(strNode, priInt);
            }
            //////////////////
        }

		saveRels();
        populateMisc();
		populateCallgraph();
	}

    void populateMisc()
    {
        ProgramRel relClassT = (ProgramRel) ClassicProject.g().getTrgt("classT");
        relClassT.zero();
        ProgramRel relSub = (ProgramRel) ClassicProject.g().getTrgt("sub");
        relSub.zero();
        ProgramRel relStaticTM = (ProgramRel) ClassicProject.g().getTrgt("staticTM");
        relStaticTM.zero();
        ProgramRel relStaticTF = (ProgramRel) ClassicProject.g().getTrgt("staticTF");
        relStaticTF.zero();
        ProgramRel relClinitTM = (ProgramRel) ClassicProject.g().getTrgt("clinitTM");
        relClinitTM.zero();

		Program program = Program.g();		
        for(SootClass klass : program.getClasses()){
            Type type = klass.getType();

            relClassT.add(type);

            for(SootField field : klass.getFields())
                if(field.isStatic())
                    relStaticTF.add(type, field);

            for(SootMethod meth : klass.getMethods())
                if(meth.isStatic() && !program.exclude(meth))//m is a static method defined in t.
                    relStaticTM.add(type, meth);

            if(klass.declaresMethodByName("<clinit>")) {
                SootMethod clinitM = klass.getMethodByName("<clinit>");
                if(!program.exclude(clinitM))
                    relClinitTM.add(type, clinitM);
            }

            for(SootClass clazz : SootUtils.subTypesOf(klass))
                relSub.add(clazz.getType(), type);//clazz is subtype of klass
        }

        relClassT.save();
        relSub.save();
        relStaticTM.save();
        relStaticTF.save();
        relClinitTM.save();
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

    //add a node to represent install apk
    static String gInstallAPK = "INSTALL_APK";
    static String boot = "android.intent.action.BOOT_COMPLETED";
    static String smsRecv = "android.provider.Telephony.SMS_RECEIVED";
    static String pickWifi = "android.net.wifi.PICK_WIFI_WORK";
    static String umsConn = "android.intent.action.UMS_CONNECTED";
    static String connChg = "android.net.conn.CONNECTIVITY_CHANGE";
    static String batLow = "android.intent.action.BATTERY_LOW";
    static String phoneState = "android.intent.action.PHONE_STATE";
    static String smsSent = "android.provider.Telephony.SMS_SENT";
    static String newCall = "android.intent.action.NEW_OUTGOING_CALL";
    static String sigStr = "android.intent.action.SIG_STR";
    static String pkgAdd = "android.intent.action.PACKAGE_ADDED";
    static String pkgChg = "android.intent.action.PACKAGE_CHANGED";
    static String pkgRemove = "android.intent.action.PACKAGE_REMOVED";
    static String pkgRep = "android.intent.action.PACKAGE_REPLACED";
    static String pkgInstall = "android.intent.action.PACKAGE_INSTALL";

	private void populateDomComp() 
	{
		DomComp domComp = (DomComp) ClassicProject.g().getTrgt("COMP");
		DomS domS = (DomS) ClassicProject.g().getTrgt("S");
        //additional rel by yu.
	    ProgramRel relService = (ProgramRel) ClassicProject.g().getTrgt("Service");
		ProgramRel relReceiver = (ProgramRel) ClassicProject.g().getTrgt("Receiver");
		ProgramRel relActivity = (ProgramRel) ClassicProject.g().getTrgt("Activity");
		ProgramRel relCompIntentAct = (ProgramRel) ClassicProject.g().getTrgt("CompIntentAction");
		ProgramRel relBootComplete = (ProgramRel) ClassicProject.g().getTrgt("BootCompleted");
		ProgramRel relSmsRecv = (ProgramRel) ClassicProject.g().getTrgt("SmsReceived");
		ProgramRel relPickWifi = (ProgramRel) ClassicProject.g().getTrgt("PickWifi");
		ProgramRel relUmsConn = (ProgramRel) ClassicProject.g().getTrgt("UmsConnected");
		ProgramRel relConnChg = (ProgramRel) ClassicProject.g().getTrgt("ConnectChg");
		ProgramRel relBatLow = (ProgramRel) ClassicProject.g().getTrgt("BatteryLow");
		ProgramRel relPhoneState = (ProgramRel) ClassicProject.g().getTrgt("PhoneState");
		ProgramRel relSmsSent = (ProgramRel) ClassicProject.g().getTrgt("SmsSent");
		ProgramRel relNewCall = (ProgramRel) ClassicProject.g().getTrgt("NewOutCall");
		ProgramRel relSigStr = (ProgramRel) ClassicProject.g().getTrgt("SigStr");
		ProgramRel relPkgAdd = (ProgramRel) ClassicProject.g().getTrgt("PkgAdd");
		ProgramRel relPkgChg = (ProgramRel) ClassicProject.g().getTrgt("PkgChg");
		ProgramRel relPkgRemove = (ProgramRel) ClassicProject.g().getTrgt("PkgRemove");
		ProgramRel relPkgRep = (ProgramRel) ClassicProject.g().getTrgt("PkgReplace");
		ProgramRel relPkgInstall = (ProgramRel) ClassicProject.g().getTrgt("PkgInstall");
		ProgramRel relCompAPK = (ProgramRel) ClassicProject.g().getTrgt("InstallAPK");

        domS.add(boot);
        domS.add(smsRecv);
        domS.add(pickWifi);
        domS.add(umsConn);
        domS.add(connChg);
        domS.add(batLow);
        domS.add(phoneState);
        domS.add(smsSent);
        domS.add(newCall);
        domS.add(sigStr);
        domS.add(pkgAdd);
        domS.add(pkgChg);
        domS.add(pkgRemove);
        domS.add(pkgRep);
        domS.add(pkgInstall);

        for(Object node : ICCGBuilder.components.keySet()) {
            domComp.add((String)node);
            domS.add((String)node);
            XmlNode xml = ICCGBuilder.components.get(node);
        }

		domS.save();
        domComp.add(gInstallAPK);
        domComp.save();

        ////////////
        relCompAPK.zero();
        relCompAPK.add(gInstallAPK);
        relCompAPK.save();
        /////////////
	    relService.zero();
        relReceiver.zero();
        relActivity.zero();
        relCompIntentAct.zero();
        relBootComplete.zero();
        relSmsRecv.zero();
        relPickWifi.zero();
        relUmsConn.zero();
        relConnChg.zero();
        relBatLow.zero();
        relPhoneState.zero();
        relSmsSent.zero();
        relNewCall.zero();
        relSigStr.zero();
        relPkgAdd.zero();
        relPkgChg.zero();
        relPkgRemove.zero();
        relPkgRep.zero();
        relPkgInstall.zero();

        //////////////////////////////////
        relBootComplete.add(boot);
        relSmsRecv.add(smsRecv);
        relPickWifi.add(pickWifi);
        relUmsConn.add(umsConn);
        relConnChg.add(connChg);
        relBatLow.add(batLow);
        relPhoneState.add(phoneState);
        relSmsSent.add(smsSent);
        relNewCall.add(newCall);
        relSigStr.add(sigStr);
        relPkgAdd.add(pkgAdd);
        relPkgChg.add(pkgChg);
        relPkgRemove.add(pkgRemove);
        relPkgRep.add(pkgRep);
        relPkgInstall.add(pkgInstall);

        for(Object node : ICCGBuilder.components.keySet()) {
            System.out.println("Component: " + node);
            System.out.println("Value: " + ICCGBuilder.components.get(node));
            XmlNode xml = ICCGBuilder.components.get(node);
            System.out.println("------------------------------------");
            if("service".equals(xml.getType()))
		        relService.add(node);
            else if("receiver".equals(xml.getType()))
		        relReceiver.add(node);
            else 
                relActivity.add(node);
        }

        relService.save();
		relReceiver.save();
		relActivity.save();
		relBootComplete.save();
        relSmsRecv.save();
        relPickWifi.save();
        relUmsConn.save();
        relConnChg.save();
        relBatLow.save();
        relPhoneState.save();
        relSmsSent.save();
        relNewCall.save();
        relSigStr.save();
        relPkgAdd.save();
        relPkgChg.save();
        relPkgRemove.save();
        relPkgRep.save();
        relPkgInstall.save();
	}

	public void run()
	{
		Program program = Program.g();		
		//for(SootClass k : program.getClasses()) System.out.println("kk "+k + (k.hasSuperclass() ? k.getSuperclass() : ""));
		program.buildCallGraph();

		fh = Program.g().scene().getOrMakeFastHierarchy();
	    populateDomComp();
		List<MethodPAGBuilder> mpagBuilders = new ArrayList();
		populateDomains(mpagBuilders);

		populateRelations(mpagBuilders);

		fh = null;
	}
}
