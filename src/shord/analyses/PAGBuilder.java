package shord.analyses;

import soot.RefLikeType;
import soot.SootMethod;
import soot.Scene;
import soot.SootField;
import soot.Local;
import soot.Immediate;
import soot.Value;
import soot.Unit;
import soot.Body;
import soot.Type;
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
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.spark.pag.ArrayElement;

import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.analyses.ProgramDom;
import shord.project.ClassicProject;
import shord.program.Program;

import chord.project.Chord;

import java.util.*;

@Chord(name="base-java", 
	   produces={"M", "Z", "I", "H", "V", "T", "F", 
				 "MobjValAsgnInst", "MobjVarAsgnInst", 
				 "MgetInstFldInst", "MputInstFldInst", 
				 "MgetStatFldInst", "MputStatFldInst", 
				 "MmethArg", "MmethRet", 
				 "IinvkRet", "IinvkArg", 
				 "VT", "chaIM"},
       namesOfTypes = { "M", "Z", "I", "H", "V", "T", "F",
						"MobjValAsgnInst", "MobjVarAsgnInst", 
						"MgetInstFldInst", "MputInstFldInst", 
						"MgetStatFldInst", "MputStatFldInst", 
						"MmethArg", "MmethRet", 
						"IinvkRet", "IinvkArg", 
						"VT", "chaIM" },
       types = { DomM.class, DomZ.class, DomI.class, DomH.class, DomV.class, DomT.class, DomF.class,
				 ProgramRel.class, ProgramRel.class,
				 ProgramRel.class, ProgramRel.class,
				 ProgramRel.class, ProgramRel.class,
				 ProgramRel.class, ProgramRel.class,
				 ProgramRel.class, ProgramRel.class,
				 ProgramRel.class, ProgramRel.class },
	   namesOfSigns = { "MobjValAsgnInst", "MobjVarAsgnInst", 
						"MgetInstFldInst", "MputInstFldInst", 
						"MgetStatFldInst", "MputStatFldInst", 
						"MmethArg", "MmethRet", 
						"IinvkRet", "IinvkArg", 
						"VT", "chaIM" },
	   signs = { "M0,V0,H0:M0_V0_H0", "M0,V0,V1:M0_V0xV1",
				 "M0,V0,V1,F0:F0_M0_V0xV1", "M0,V0,F0,V1:F0_M0_V0xV1",
				 "M0,V0,F0:F0_M0_V0", "M0,F0,V0:F0_M0_V0",
				 "M0,Z0,V0:M0_V0_Z0", "M0,Z0,V1:M0_V1_Z0",
				 "I0,Z0,V0:I0_V0_Z0", "I0,Z0,V1:I0_V1_Z0",
				 "V0,T0:T0_V0", "I0,M0:I0_M0" }
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

	private ProgramRel relVT;

	private DomV domV;
	private DomH domH;
	private DomZ domZ;

	private int maxArgs = -1;

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
		relMgetInstFldInst.add(m, l, b, f);
	}

	void MputInstFldInst(SootMethod m, LocalVarNode b, SparkField f, LocalVarNode r)
	{
		if(b == null || r == null)
			return;
		relMputInstFldInst.add(m, b, f, r);
	}

	void MgetStatFldInst(SootMethod m, LocalVarNode l, SootField f)
	{
		if(l == null)
			return;
		relMgetStatFldInst.add(m, l, f);
	}

	void MputStatFldInst(SootMethod m, SootField f, LocalVarNode r)
	{
		if(r == null)
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

		MethodPAGBuilder(SootMethod method)
		{
			this.method = method;

			growZIfNeeded(method.getParameterCount());
			
			int i = 0;
			if(!method.isStatic()) {
				thisVar = new ThisVarNode(method);
				add(thisVar, method.getDeclaringClass().getType());
				MmethArg(method, i++, thisVar);
 			}
			int count = method.getParameterCount();
			paramVars = new ParamVarNode[count];
			int j = 0;
			for(Type pType : method.getParameterTypes()){
				ParamVarNode node = null;
				if(pType instanceof RefLikeType){
					node = new ParamVarNode(method, j);
					add(node, pType);
					MmethArg(method, i, node);
				}
				paramVars[j++] = node;
				i++;
			}
			Type retType = method.getReturnType();
			if(retType instanceof RefLikeType) {
				retVar = new RetVarNode(method);
				add(retVar, retType);
				MmethRet(method, retVar);
			}
		}
		
		void add(VarNode node, Type type)
		{
			domV.add(node);
			relVT.add(node, type);
		}

		void visitBody()
		{
			localToVarNode = new HashMap();
			Body body = method.retrieveActiveBody();
			for(Local l : body.getLocals()){
				if(!(l.getType() instanceof RefLikeType)) 
					continue;
				LocalVarNode node = new LocalVarNode(l);
				add(node, l.getType());
				localToVarNode.put(l, node);
			}
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
				int numArgs = ie.getArgCount();
				growZIfNeeded(numArgs);

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
					IinvkArg(s, j, nodeFor(arg));
				}
				
				//return value
				if(s instanceof AssignStmt){
					IinvkRet(s, nodeFor((Local) ((AssignStmt) s).getLeftOp()));
				}
			}else if(s.containsFieldRef()){
				AssignStmt as = (AssignStmt) s;
				Value leftOp = as.getLeftOp();
				FieldRef fr = s.getFieldRef();
				SootField field = fr.getField();
				if(leftOp instanceof Local){
					//load
					if(field.isStatic()){
						MgetStatFldInst(method, nodeFor((Local) leftOp), field);
					} else{
						MgetInstFldInst(method, nodeFor((Local) leftOp), nodeFor((Immediate) ((InstanceFieldRef) fr).getBase()), field);
					}
				}else{
					//store
					assert leftOp == fr;
					Immediate rightOp = (Immediate) as.getRightOp();
					if(field.isStatic()){
						MputStatFldInst(method, field, nodeFor(rightOp));
					} else{
						MputInstFldInst(method, nodeFor((Immediate) ((InstanceFieldRef) fr).getBase()), field, nodeFor(rightOp));
					}
				}
			}else if(s.containsArrayRef()){
				AssignStmt as = (AssignStmt) s;
				Value leftOp = as.getLeftOp();
				ArrayRef ar = s.getArrayRef();
				Immediate base = (Immediate) ar.getBase();
				SparkField field = ArrayElement.v();
				if(leftOp instanceof Local){
					//array read
					MgetInstFldInst(method, nodeFor((Local) leftOp), nodeFor(base), field);
				}else{
					//array write
					assert leftOp == ar;
					Immediate rightOp = (Immediate) as.getRightOp();
					MputInstFldInst(method, nodeFor(base), field, nodeFor(rightOp));
				}
			}else if(s instanceof AssignStmt){
				AssignStmt as = (AssignStmt) s;
				Value leftOp = as.getLeftOp();
				Value rightOp = as.getRightOp();
				if(rightOp instanceof AnyNewExpr){
					domH.add(s);
					MobjValAsgnInst(method, nodeFor((Local) leftOp), s);
				} else if(rightOp instanceof CastExpr){
					Immediate op = (Immediate) ((CastExpr) rightOp).getOp();
					MobjVarAsgnInst(method, nodeFor((Local) leftOp), nodeFor(op));
				} else if(leftOp instanceof Local && rightOp instanceof Immediate){
					MobjVarAsgnInst(method, nodeFor((Local) leftOp), nodeFor((Immediate) rightOp));
				}
			}else if(s instanceof ReturnStmt){
				MobjVarAsgnInst(method, retVar, nodeFor((Immediate) ((ReturnStmt) s).getOp()));
			}else if(s instanceof IdentityStmt){
				IdentityStmt is = (IdentityStmt) s;
				Local leftOp = (Local) is.getLeftOp();
				Value rightOp = is.getRightOp();
				if(rightOp instanceof ThisRef){
					MobjVarAsgnInst(method, nodeFor(leftOp), thisVar);
				} else if(rightOp instanceof ParameterRef){
					int index = ((ParameterRef) rightOp).getIndex();
					MobjVarAsgnInst(method, nodeFor(leftOp), paramVars[index]);
				}
			}
		}
	}

	void populateCallgraph()
	{
        DomI domI = (DomI) ClassicProject.g().getTrgt("I");
        DomM domM = (DomM) ClassicProject.g().getTrgt("M");

		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> edgeIt = cg.listener();
		while(edgeIt.hasNext()){
			Edge edge = edgeIt.next();
			if(!edge.isExplicit())
				continue;
			Stmt stmt = edge.srcStmt();
			int stmtIdx = domI.getOrAdd(stmt);
			//SootMethod tgt = (SootMethod) edge.tgt();
			//relChaIM.add(stmtIdx, domM.indexOf(tgt));
		}
		domI.save();

		ProgramRel relChaIM = (ProgramRel) ClassicProject.g().getTrgt("chaIM");
        relChaIM.zero();
		edgeIt = cg.listener();
		while(edgeIt.hasNext()){
			Edge edge = edgeIt.next();
			if(!edge.isExplicit())
				continue;
			Stmt stmt = edge.srcStmt();
			//int stmtIdx = domI.getOrAdd(stmt);
			SootMethod tgt = (SootMethod) edge.tgt();
			System.out.println("stmt: "+stmt+" tgt: "+tgt);
			relChaIM.add(stmt, tgt);
		}
		relChaIM.save();
	}

	void populateMethods()
	{
		DomM domM = (DomM) ClassicProject.g().getTrgt("M");
		Program program = Program.g();
		Iterator mIt = program.getMethods().listener();
		while(mIt.hasNext()){
			SootMethod m = (SootMethod) mIt.next();
			domM.add(m);
		}
		domM.save();
	}
	
	void populateFields()
	{
		DomF domF = (DomF) ClassicProject.g().getTrgt("F");
		Program program = Program.g();
        Iterator<SootField> fieldsIt = program.getFields().iterator();
		while(fieldsIt.hasNext()){
            domF.add(fieldsIt.next());
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

	public void run()
	{
		Program program = Program.g();
		program.build();

		populateMethods();
		populateFields();
		populateTypes();
		populateCallgraph();

		domH = (DomH) ClassicProject.g().getTrgt("H");
		domV = (DomV) ClassicProject.g().getTrgt("V");
		domZ = (DomZ) ClassicProject.g().getTrgt("Z");
		relVT = (ProgramRel) ClassicProject.g().getTrgt("VT");
        relVT.zero();

		Iterator mIt = program.getMethods().listener();
		while(mIt.hasNext()){
			SootMethod m = (SootMethod) mIt.next();
			MethodPAGBuilder mpagBuilder = new MethodPAGBuilder(m);
			if(m.isConcrete()){
				mpagBuilder.visitBody();
			}
		}
		domH.save();
		domZ.save();
		domV.save();
		relVT.save();
	}
}