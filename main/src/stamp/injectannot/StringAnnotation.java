package stamp.injectannot;

import soot.*;
import soot.jimple.*;
import soot.util.*;

import java.io.File;
import java.util.*;
import stamp.harnessgen.*;

public class StringAnnotation extends AnnotationInjector.Visitor
{
    private final Map<String,SootMethod> srcLabelToLabelMethod = new HashMap();

    private Map<String, XmlNode> components = new HashMap<String, XmlNode>();

    private SootClass klass;
    private int newLocalCount;

    public StringAnnotation()
    {
        String stampOutDir = System.getProperty("stamp.out.dir");
        //parse manifest.xml
        String manifestDir = stampOutDir + "/apktool-out";
        File manifestFile = new File(manifestDir, "AndroidManifest.xml");
        new ParseManifest().extractComponents(manifestFile, components);

    }
	
    protected void visit(SootClass klass)
    {
      		this.klass = klass;
		this.srcLabelToLabelMethod.clear();
		this.newLocalCount = 0;
		Collection<SootMethod> methodsCopy = new ArrayList(klass.getMethods());
		for(SootMethod method : methodsCopy)
			visitMethod(method);
    }
	
    private void visitMethod(SootMethod method)
    {
		if(!method.isConcrete())
			return;
		Body body = method.retrieveActiveBody();
		Chain<Local> locals = body.getLocals();
		Chain<Unit> units = body.getUnits();
		Iterator<Unit> uit = units.snapshotIterator();
		while(uit.hasNext()){
			Stmt stmt = (Stmt) uit.next();
	    
			if(stmt instanceof AssignStmt){
				Value rhs = ((AssignStmt) stmt).getRightOp();
				if(rhs instanceof StringConstant){
					insertStringIfNecessary((StringConstant) rhs, locals, units, stmt);
				}
				//if(rhs.getType().toString().equals("java.lang.Class")) {
				if(rhs instanceof ClassConstant){
					insertClassIfNecessary((ClassConstant) rhs, locals, units, stmt);
				}

			}
		}
    }
    
    private Local insertStringIfNecessary(StringConstant strConst, Chain<Local> locals, Chain<Unit> units, Unit currentStmt)
    {
		//check whether string is one of action name, compnoent name.
		String str = strConst.value;
		if(components.get(str) == null)
			return null;

		///System.out.println("detect component: " + str);

		SootMethod meth = getOrCreateStringMethodFor(str);
		Local temp = Jimple.v().newLocal("stamp$stamp$tmp"+newLocalCount++, 
										 RefType.v("java.lang.String"));
		locals.add(temp);
		Stmt toInsert = Jimple.v().newAssignStmt(temp, Jimple.v().newStaticInvokeExpr(meth.makeRef(), strConst));

		AssignStmt as = (AssignStmt) currentStmt;
		Value leftOp = as.getLeftOp();
		Value rightOp = as.getRightOp();
		Stmt toAssign = Jimple.v().newAssignStmt(leftOp, temp); 

		units.insertBefore(toInsert, currentStmt);
		units.insertBefore(toAssign, currentStmt);
		units.remove(currentStmt);
		return temp;
    }
	
     private Local insertClassIfNecessary(ClassConstant clsConst, Chain<Local> locals, Chain<Unit> units, Unit currentStmt)
    {
		//check whether string is one of action name, compnoent name.
		String str = clsConst.value;
		if(components.get(str) == null)
			return null;

		///System.out.println("detect component: " + str);

		SootMethod meth = getOrCreateClassMethodFor(str);
		Local temp = Jimple.v().newLocal("stamp$stamp$tmpcls"+newLocalCount++, 
										 RefType.v("java.lang.Class"));
		locals.add(temp);
		Stmt toInsert = Jimple.v().newAssignStmt(temp, Jimple.v().newStaticInvokeExpr(meth.makeRef(), StringConstant.v(str)));
		units.insertBefore(toInsert, currentStmt);
		return temp;
    }
	   
    private SootMethod getOrCreateStringMethodFor(String label)
    {
		SootMethod meth = srcLabelToLabelMethod.get(label);
		if(meth == null){
			RefType stringType = RefType.v("java.lang.String");
			List paramTypes = Arrays.asList(new Type[]{stringType});
			String methName = "stamp$stamp$"+srcLabelToLabelMethod.size();
			meth = new SootMethod(methName, paramTypes, stringType, Modifier.STATIC | Modifier.PRIVATE);
			klass.addMethod(meth);
			srcLabelToLabelMethod.put(label, meth);
			
			JimpleBody body = Jimple.v().newBody(meth);
			meth.setActiveBody(body);
			
			Local param = Jimple.v().newLocal("l0", stringType);
			body.getLocals().add(param);
			
			Chain units = body.getUnits();
			units.add(Jimple.v().newIdentityStmt(param, 
					Jimple.v().newParameterRef(stringType, 0)));
			
			Local ret = Jimple.v().newLocal("l1", stringType);
			body.getLocals().add(ret);
			units.add(Jimple.v().newAssignStmt(ret,
											   Jimple.v().newNewExpr(stringType)));
			
			SootMethodRef mref = Scene.v().getMethod("<java.lang.String: void <init>(java.lang.String)>").makeRef();
			units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(ret, mref, param)));
			
			units.add(Jimple.v().newReturnStmt(ret));
		}
		
		return meth;
    }

    private SootMethod getOrCreateClassMethodFor(String label)
    {
		SootMethod meth = srcLabelToLabelMethod.get(label);
		if(meth == null){
			RefType stringType = RefType.v("java.lang.String");
			RefType classType = RefType.v("java.lang.Class");
			List paramTypes = Arrays.asList(new Type[]{stringType});
			String methName = "stamp$stamp$"+srcLabelToLabelMethod.size();
			meth = new SootMethod(methName, paramTypes, stringType, Modifier.STATIC | Modifier.PRIVATE);
			klass.addMethod(meth);
			srcLabelToLabelMethod.put(label, meth);
			
			JimpleBody body = Jimple.v().newBody(meth);
			meth.setActiveBody(body);
			
			Local param = Jimple.v().newLocal("l0", stringType);
			body.getLocals().add(param);
			
			Chain units = body.getUnits();
			units.add(Jimple.v().newIdentityStmt(param, 
					Jimple.v().newParameterRef(stringType, 0)));

			Local ret = Jimple.v().newLocal("l1", classType);
			Local newStr = Jimple.v().newLocal("l2", stringType);
			body.getLocals().add(ret);
			/*units.add(Jimple.v().newAssignStmt(ret,
											   Jimple.v().newNewExpr(classType)));*/
			body.getLocals().add(newStr);
			units.add(Jimple.v().newAssignStmt(newStr,
											   Jimple.v().newNewExpr(stringType)));
			
			SootMethodRef mref = Scene.v().getMethod("<java.lang.String: void <init>(java.lang.String)>").makeRef();
			SootMethodRef cref = Scene.v().getMethod("<java.lang.Class: java.lang.Class forName(java.lang.String)>").makeRef();
			units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(newStr, mref, param)));
			//units.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(ret, cref, newStr)));
			//units.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(cref, newStr)));

			/*units.add(Jimple.v().newAssignStmt(ret,
							Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(cref, newStr))));*/

			
			units.add(Jimple.v().newReturnStmt(ret));
		}
		
		return meth;
    }


}
