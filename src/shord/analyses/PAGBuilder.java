package shord.analyses;

import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Local;
import soot.Immediate;
import soot.Value;
import soot.Unit;
import soot.Body;
import soot.Type;
import soot.RefLikeType;
import soot.PrimType;
import soot.VoidType;
import soot.NullType;
import soot.AnySubType;
import soot.UnknownType;
import soot.FastHierarchy;
import soot.jimple.Constant;
import soot.jimple.Stmt;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.AnyNewExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import soot.jimple.ArrayRef;
import soot.jimple.BinopExpr;
import soot.jimple.NegExpr;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.spark.pag.ArrayElement;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.analyses.ProgramDom;
import shord.project.ClassicProject;
import shord.program.Program;

import chord.project.Chord;

import java.util.*;

@Chord(name="base-java", 
	   produces={"M", "Z", "I", "H", "V", "T", "F", "U",
				 "MobjValAsgnInst", "MobjVarAsgnInst", 
				 "MgetInstFldInst", "MputInstFldInst", 
				 "MgetStatFldInst", "MputStatFldInst", 
				 "MmethArg", "MmethRet", 
				 "IinvkRet", "IinvkArg", 
				 "VT", "chaIM",
				 "HT", "HTFilter",
				 "MI", "MH",
				 "MprimDataDep", 
				 "MgetInstFldPrimInst", "MputInstFldPrimInst",
				 "MgetStatFldPrimInst", "MputStatFldPrimInst",
				 "MmethPrimArg", "MmethPrimRet", 
				 "IinvkPrimRet", "IinvkPrimArg" },
       namesOfTypes = { "M", "Z", "I", "H", "V", "T", "F", "U" },
       types = { DomM.class, DomZ.class, DomI.class, DomH.class, DomV.class, DomT.class, DomF.class, DomU.class },
	   namesOfSigns = { "MobjValAsgnInst", "MobjVarAsgnInst", 
						"MgetInstFldInst", "MputInstFldInst", 
						"MgetStatFldInst", "MputStatFldInst", 
						"MmethArg", "MmethRet", 
						"IinvkRet", "IinvkArg", 
						"VT", "chaIM",
						"HT", "HTFilter",
						"MI", "MH",
						"MprimDataDep", 
						"MgetInstFldPrimInst", "MputInstFldPrimInst",
						"MgetStatFldPrimInst", "MputStatFldPrimInst",
						"MmethPrimArg", "MmethPrimRet", 
						"IinvkPrimRet", "IinvkPrimArg" },
	   signs = { "M0,V0,H0:M0_V0_H0", "M0,V0,V1:M0_V0xV1",
				 "M0,V0,V1,F0:F0_M0_V0xV1", "M0,V0,F0,V1:F0_M0_V0xV1",
				 "M0,V0,F0:F0_M0_V0", "M0,F0,V0:F0_M0_V0",
				 "M0,Z0,V0:M0_V0_Z0", "M0,Z0,V1:M0_V1_Z0",
				 "I0,Z0,V0:I0_V0_Z0", "I0,Z0,V1:I0_V1_Z0",
				 "V0,T0:T0_V0", "I0,M0:I0_M0",
				 "H0,T0:H0_T0", "H0,T0:H0_T0",
				 "M0,I0:M0_I0", "M0,H0:M0_H0",
				 "M0,U0,U1:M0_U0xU1",
				 "M0,U0,V0,F0:M0_U0_V0_F0", "M0,V0,F0,U0:M0_U0_V0_F0",
				 "M0,U0,F0:M0_U0_F0", "M0,F0,U0:M0_U0_F0",
				 "M0,Z0,U0:M0_U0_Z0", "M0,Z0,U0:M0_U0_Z0",
				 "I0,Z0,U0:I0_U0_Z0", "I0,Z0,U0:I0_U0_Z0" }
	   )
public class PAGBuilder extends JavaAnalysis
{
	private ProgramRel relMobjValAsgnInst;//(m:M,l:V,h:H)
	private ProgramRel relMobjVarAsgnInst;//(m:M,l:V,r:V)
	private ProgramRel relMgetInstFldInst;//(m:M,l:V,b:V,f:F)
	private ProgramRel relMputInstFldInst;//(m:M,b:V,f:F,r:V)
	private ProgramRel relMgetStatFldInst;//(m:M,l:V,f:F)
	private ProgramRel relMputStatFldInst;//(m:M,f:F,r:V)

    private ProgramRel relMmethArg;//(m:M,z:Z,v:V)
    private ProgramRel relMmethRet;//(m:M,z:Z,v:V)
    private ProgramRel relIinvkRet;//(i:I,n:Z,v:V)
    private ProgramRel relIinvkArg;//(i:I,n:Z,v:V)

	private ProgramRel relMprimDataDep;//(m:M,l:U,r:U1)
	private ProgramRel relMgetInstFldPrimInst;//(m:M,l:U,b:V,f:F)
	private ProgramRel relMputInstFldPrimInst;//(m:M,b:V,f:F,r:U)
	private ProgramRel relMgetStatFldPrimInst;//(m:M,l:U,f:F)
	private ProgramRel relMputStatFldPrimInst;//(m:M,f:F,r:U)

    private ProgramRel relMmethPrimArg;//(m:M,z:Z,u:U)
    private ProgramRel relMmethPrimRet;//(m:M,z:Z,u:U)
    private ProgramRel relIinvkPrimRet;//(i:I,n:Z,u:U)
    private ProgramRel relIinvkPrimArg;//(i:I,n:Z,u:U)

	private ProgramRel relVT;
	private ProgramRel relHT;
	private ProgramRel relHTFilter;
	private ProgramRel relMI;
	private ProgramRel relMH;

	private DomV domV;
	private DomU domU;
	private DomH domH;
	private DomZ domZ;
	private DomI domI;

	private int maxArgs = -1;
	private FastHierarchy fh;

	void openRels()
	{
		relMobjValAsgnInst = (ProgramRel) ClassicProject.g().getTrgt("MobjValAsgnInst");
		relMobjValAsgnInst.zero();
		relMobjVarAsgnInst = (ProgramRel) ClassicProject.g().getTrgt("MobjVarAsgnInst");
		relMobjVarAsgnInst.zero();
		relMgetInstFldInst = (ProgramRel) ClassicProject.g().getTrgt("MgetInstFldInst");
		relMgetInstFldInst.zero();
		relMputInstFldInst = (ProgramRel) ClassicProject.g().getTrgt("MputInstFldInst");
		relMputInstFldInst.zero();
		relMgetStatFldInst = (ProgramRel) ClassicProject.g().getTrgt("MgetStatFldInst");
		relMgetStatFldInst.zero();
		relMputStatFldInst = (ProgramRel) ClassicProject.g().getTrgt("MputStatFldInst");
		relMputStatFldInst.zero();
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
		relMprimDataDep = (ProgramRel) ClassicProject.g().getTrgt("MprimDataDep");
		relMprimDataDep.zero();
		relMgetInstFldPrimInst = (ProgramRel) ClassicProject.g().getTrgt("MgetInstFldPrimInst");
		relMgetInstFldPrimInst.zero();
		relMputInstFldPrimInst = (ProgramRel) ClassicProject.g().getTrgt("MputInstFldPrimInst");
		relMputInstFldPrimInst.zero();
		relMgetStatFldPrimInst = (ProgramRel) ClassicProject.g().getTrgt("MgetStatFldPrimInst");
		relMgetStatFldPrimInst.zero();
		relMputStatFldPrimInst = (ProgramRel) ClassicProject.g().getTrgt("MputStatFldPrimInst");
		relMputStatFldPrimInst.zero();
		relMmethPrimArg = (ProgramRel) ClassicProject.g().getTrgt("MmethPrimArg");
		relMmethPrimArg.zero();
		relMmethPrimRet = (ProgramRel) ClassicProject.g().getTrgt("MmethPrimRet");
		relMmethPrimRet.zero();
		relIinvkPrimRet = (ProgramRel) ClassicProject.g().getTrgt("IinvkPrimRet");
		relIinvkPrimRet.zero();
		relIinvkPrimArg = (ProgramRel) ClassicProject.g().getTrgt("IinvkPrimArg");
		relIinvkPrimArg.zero();
	}
	
	void saveRels()
	{
		relMobjValAsgnInst.save();
		relMobjVarAsgnInst.save();
		relMgetInstFldInst.save();
		relMputInstFldInst.save();
		relMgetStatFldInst.save();
		relMputStatFldInst.save();
		relMmethArg.save();
		relMmethRet.save();
		relIinvkRet.save();
		relIinvkArg.save();
		relVT.save();
		relHT.save();
		relHTFilter.save();
		relMI.save();
		relMH.save();
		relMprimDataDep.save();
		relMgetInstFldPrimInst.save();
		relMputInstFldPrimInst.save();
		relMgetStatFldPrimInst.save();
		relMputStatFldPrimInst.save();
		relMmethPrimArg.save();
		relMmethPrimRet.save();
		relIinvkPrimRet.save();
		relIinvkPrimArg.save();
	}

	void MobjValAsgnInst(SootMethod m, LocalVarNode l, Stmt h)
	{
		assert l != null;
		relMobjValAsgnInst.add(m, l, h);
	}

	void MobjVarAsgnInst(SootMethod m, VarNode l, VarNode r)
	{
		if(l == null || r == null)
			return;
		relMobjVarAsgnInst.add(m, l, r);
	}

	void MgetInstFldInst(SootMethod m, LocalVarNode l, LocalVarNode b, SparkField f)
	{
		if(b == null || l == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relMgetInstFldInst.add(m, l, b, f);
	}

	void MputInstFldInst(SootMethod m, LocalVarNode b, SparkField f, LocalVarNode r)
	{
		if(b == null || r == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relMputInstFldInst.add(m, b, f, r);
	}

	void MgetStatFldInst(SootMethod m, LocalVarNode l, SootField f)
	{
		if(l == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relMgetStatFldInst.add(m, l, f);
	}

	void MputStatFldInst(SootMethod m, SootField f, LocalVarNode r)
	{
		if(r == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relMputStatFldInst.add(m, f, r);
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

	void MprimDataDep(SootMethod m, VarNode l, VarNode r)
	{
		if(l == null || r == null)
			return;
		relMprimDataDep.add(m, l, r);
	}

	void MgetInstFldPrimInst(SootMethod m, LocalVarNode l, LocalVarNode b, SparkField f)
	{
		if(b == null || l == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relMgetInstFldPrimInst.add(m, l, b, f);
	}

	void MputInstFldPrimInst(SootMethod m, LocalVarNode b, SparkField f, LocalVarNode r)
	{
		if(b == null || r == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relMputInstFldPrimInst.add(m, b, f, r);
	}

	void MgetStatFldPrimInst(SootMethod m, LocalVarNode l, SootField f)
	{
		if(l == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relMgetStatFldPrimInst.add(m, l, f);
	}

	void MputStatFldPrimInst(SootMethod m, SootField f, LocalVarNode r)
	{
		if(r == null)
			return;
		if(f instanceof SootField && (((SootField) f).getDeclaringClass().isPhantom()))
		   return;
		relMputStatFldPrimInst.add(m, f, r);
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

		MethodPAGBuilder(SootMethod method)
		{
			this.method = method;
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
				} else if(retType instanceof PrimType) {
					domU.add(retVar);
				} else
					assert false;
			} 

			if(!method.isConcrete())
				return;

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
					if(rightOp instanceof AnyNewExpr)
						domH.add(s);
					else if(rightOp instanceof CastExpr){
						Type castType = ((CastExpr) rightOp).getCastType();
						if(castType instanceof RefLikeType){
							CastVarNode node = new CastVarNode();
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
			}
			
			if(paramVars != null){
				for(int j = 0; j < paramVars.length; j++){
					Type paramType = method.getParameterType(j);
					ParamVarNode node = paramVars[j];
					if(paramType instanceof RefLikeType){
						MmethArg(method, i, node);
						relVT.add(node, paramType);
					} else if(paramType instanceof PrimType){
						MmethPrimArg(method, i, node);
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
				} else if(retType instanceof PrimType){
					MmethPrimRet(method, retVar);
				} else
					assert false;
			}

			if(!method.isConcrete())
				return;
			for(Map.Entry<Local,LocalVarNode> e : localToVarNode.entrySet()){
				if(nonPrimLocals.contains(e.getKey()))
					relVT.add(e.getValue(), UnknownType.v());
			}
			for(Map.Entry<Stmt,CastVarNode> e : stmtToCastNode.entrySet()){
				Type castType = ((CastExpr) ((AssignStmt) e.getKey()).getRightOp()).getCastType();
				relVT.add(e.getValue(), castType);
			}

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
							MgetStatFldInst(method, nodeFor((Local) leftOp), field);
						else if(fieldType instanceof PrimType)
							MgetStatFldPrimInst(method, nodeFor((Local) leftOp), field);
					} else{
						Immediate base = (Immediate) ((InstanceFieldRef) fr).getBase();
						if(fieldType instanceof RefLikeType)
							MgetInstFldInst(method, nodeFor((Local) leftOp), nodeFor(base), field);
						else if(fieldType instanceof PrimType)
							MgetInstFldPrimInst(method, nodeFor((Local) leftOp), nodeFor(base), field);
					}
				}else{
					//store
					assert leftOp == fr;
					Immediate rightOp = (Immediate) as.getRightOp();
					if(field.isStatic()){
						if(fieldType instanceof RefLikeType)
							MputStatFldInst(method, field, nodeFor(rightOp));
						else if(fieldType instanceof PrimType)
							MputStatFldPrimInst(method, field, nodeFor(rightOp));
					} else{
						Immediate base = (Immediate) ((InstanceFieldRef) fr).getBase();
						if(fieldType instanceof RefLikeType)
							MputInstFldInst(method, nodeFor(base), field, nodeFor(rightOp));
						else if(fieldType instanceof PrimType)
							MputInstFldPrimInst(method, nodeFor(base), field, nodeFor(rightOp));		
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
						MgetInstFldInst(method, nodeFor(l), nodeFor(base), field);
					if(primLocals.contains(l))
						MgetInstFldPrimInst(method, nodeFor(l), nodeFor(base), field);
				}else{
					//array write
					assert leftOp == ar;
					Value rightOp = as.getRightOp();
					if(rightOp instanceof Local){
						Local r = (Local) rightOp;
						if(nonPrimLocals.contains(r))
							MputInstFldInst(method, nodeFor(base), field, nodeFor(r));
						if(primLocals.contains(r))
							MputInstFldPrimInst(method, nodeFor(base), field, nodeFor(r));
					}
				}
			} else if(s instanceof AssignStmt) {
				AssignStmt as = (AssignStmt) s;
				Value leftOp = as.getLeftOp();
				Value rightOp = as.getRightOp();

				if(rightOp instanceof AnyNewExpr){
					MobjValAsgnInst(method, nodeFor((Local) leftOp), s);
					relHT.add(s, rightOp.getType());
					relMH.add(method, s);
					Iterator<Type> typesIt = Program.g().getTypes().iterator();
					while(typesIt.hasNext()){
						Type varType = typesIt.next();
						//if(!(varType instanceof RefLikeType)
						//	continue;
						if(canStore(rightOp.getType(), varType))
							relHTFilter.add(s, varType);
					}
				} else if(rightOp instanceof CastExpr){
					Type castType = ((CastExpr) rightOp).getCastType();
					Immediate op = (Immediate) ((CastExpr) rightOp).getOp();
					if(castType instanceof RefLikeType){
						CastVarNode castNode = stmtToCastNode.get(s);
						MobjVarAsgnInst(method, castNode, nodeFor(op));
						MobjVarAsgnInst(method, nodeFor((Local) leftOp), castNode);
					} else if(castType instanceof PrimType)
						MprimDataDep(method, nodeFor((Local) leftOp), nodeFor(op));
				} else if(leftOp instanceof Local && rightOp instanceof Immediate){
					Local l = (Local) leftOp;
					Immediate r = (Immediate) rightOp;
					if(nonPrimLocals.contains(l))
						MobjVarAsgnInst(method, nodeFor(l), nodeFor(r));
					if(primLocals.contains(l))
						MprimDataDep(method, nodeFor(l), nodeFor(r));
				} if(rightOp instanceof NegExpr){
					MprimDataDep(method, nodeFor((Local) leftOp), nodeFor((Immediate) ((NegExpr) rightOp).getOp()));
				}else if(rightOp instanceof BinopExpr){
					LocalVarNode leftNode = nodeFor((Local) leftOp);
					BinopExpr binExpr = (BinopExpr) rightOp;
					Immediate op1 = (Immediate) binExpr.getOp1();
					if(op1 instanceof Local){
						Local l = (Local) op1;
						if(primLocals.contains(l))
							MprimDataDep(method, leftNode, nodeFor(l));	
					}
					Immediate op2 = (Immediate) binExpr.getOp2();
					if(op2 instanceof Local){
						Local l = (Local) op2;
						if(primLocals.contains(l))
							MprimDataDep(method, leftNode, nodeFor(l));
					}
				}
			}else if(s instanceof ReturnStmt){
				Type retType = method.getReturnType();
				Immediate retOp = (Immediate) ((ReturnStmt) s).getOp();
				if(retType instanceof RefLikeType)
					MobjVarAsgnInst(method, retVar, nodeFor(retOp));
				else if(retType instanceof PrimType)
					MprimDataDep(method, retVar, nodeFor(retOp));
			}else if(s instanceof IdentityStmt){
				IdentityStmt is = (IdentityStmt) s;
				Local leftOp = (Local) is.getLeftOp();
				Value rightOp = is.getRightOp();
				if(rightOp instanceof ThisRef){
					MobjVarAsgnInst(method, nodeFor(leftOp), thisVar);
				} else if(rightOp instanceof ParameterRef){
					int index = ((ParameterRef) rightOp).getIndex();
					Type type = method.getParameterType(index);
					if(type instanceof RefLikeType)
						MobjVarAsgnInst(method, nodeFor(leftOp), paramVars[index]);
					else if(type instanceof PrimType)
						MprimDataDep(method, nodeFor(leftOp), paramVars[index]);
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
			if(!edge.isExplicit())
				continue;
			Stmt stmt = edge.srcStmt();
			//int stmtIdx = domI.getOrAdd(stmt);
			SootMethod tgt = (SootMethod) edge.tgt();
			//System.out.println("stmt: "+stmt+" tgt: "+tgt);
			relChaIM.add(stmt, tgt);
		}
		relChaIM.save();
	}

	void populateMethods()
	{
		DomM domM = (DomM) ClassicProject.g().getTrgt("M");
		Program program = Program.g();
		Iterator<SootMethod> mIt = program.getMethods();
		while(mIt.hasNext()){
			SootMethod m = mIt.next();
			domM.add(m);
		}
		domM.save();
	}
	
	void populateFields()
	{
		DomF domF = (DomF) ClassicProject.g().getTrgt("F");
		Program program = Program.g();
		for(SootClass klass : program.getClasses()){
			for(SootField field : klass.getFields()){
				domF.add(field);
			}
		}
		domF.add(ArrayElement.v());
		domF.save();
	}
	
	void populateTypes()
	{
		DomT domT = (DomT) ClassicProject.g().getTrgt("T");
		Program program = Program.g();
        Iterator<Type> typesIt = program.getTypes().iterator();
		while(typesIt.hasNext())
            domT.add(typesIt.next());
		domT.save();
	}
		
	void populateDomains(List<MethodPAGBuilder> mpagBuilders)
	{
		populateMethods();
		populateFields();
		populateTypes();

		domH = (DomH) ClassicProject.g().getTrgt("H");
		domV = (DomV) ClassicProject.g().getTrgt("V");
		domZ = (DomZ) ClassicProject.g().getTrgt("Z");
		domI = (DomI) ClassicProject.g().getTrgt("I");
		domU = (DomU) ClassicProject.g().getTrgt("U");

		Iterator mIt = Program.g().scene().getReachableMethods().listener();
		while(mIt.hasNext()){
			SootMethod m = (SootMethod) mIt.next();
			MethodPAGBuilder mpagBuilder = new MethodPAGBuilder(m);
			mpagBuilder.pass1();
			mpagBuilders.add(mpagBuilder);
		}

		domH.save();
		domZ.save();
		domV.save();
		domI.save();
		domU.save();
	}
	
	void populateRelations(List<MethodPAGBuilder> mpagBuilders)
	{
		openRels();
		for(MethodPAGBuilder mpagBuilder : mpagBuilders)
			mpagBuilder.pass2();
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