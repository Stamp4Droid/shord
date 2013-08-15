package stamp.injectannot;

import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.AbstractJasminClass;

import java.io.File;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.jimple.internal.ImmediateBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JimpleLocalBox;
import soot.jimple.internal.VariableBox;
import soot.tagkit.LinkTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import java.util.*;
import stamp.analyses.StringLocalDefs;
import stamp.analyses.ReachingDefsAnalysis;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
  * attach param values to smsnumber.
  **/
public class SmsDestAnnotation extends AnnotationInjector.Visitor
{

    private final Map<String,SootMethod> srcLabelToLabelMethod = new HashMap();

    private final Map<String,String> methodSig = new HashMap();

    private SootClass klass;
    private int newLocalCount;

	protected void postVisit()
	{
	}

    public SmsDestAnnotation()
    {
        String sigLoc = System.getProperty("stamp.dir") + "/models/soot_methods.txt";
        try {
            FileInputStream in = new FileInputStream(sigLoc);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            while ((strLine = br.readLine()) != null) {
                String[] sigArray = strLine.split("#");
                methodSig.put(sigArray[0], sigArray[1] + "#" + sigArray[2]);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
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
        //run String analysis.
        UnitGraph g = new ExceptionalUnitGraph(body);
        StringLocalDefs sld = new StringLocalDefs(g, new SimpleLiveLocals(g));
        ReachingDefsAnalysis.runReachingDef(body);

        Chain<Local> locals = body.getLocals();
        Chain<Unit> units = body.getUnits();
        Iterator<Unit> uit = units.snapshotIterator();
        while(uit.hasNext()){
            Stmt stmt = (Stmt) uit.next();

            //invocation statements
            if(stmt.containsInvokeExpr()){
                InvokeExpr ie = stmt.getInvokeExpr();
                String methodRefStr = ie.getMethodRef().toString();
                String argAndSink = methodSig.get(ie.getMethod().getSignature());

                if (argAndSink != null) {
                    int argNum = Integer.parseInt(argAndSink.split("#")[0]);
                    String sinkStr = argAndSink.split("#")[1];

                    Map<Value, Set<String>> valueMap = sld.getDefsOfAt(null, stmt);
                    if (valueMap.get(ie.getArg(argNum)) != null) {
                        StringConstant arg = StringConstant.v(valueMap.get(ie.getArg(argNum)).toString());
                        System.out.println("String analysis: " + " | stmt: " + stmt + " | tags: " + stmt.getTags() 
                                        + " | string value: " + arg + " |Class: " + this.klass 
                                        + " |Method: " + method + " | body: " + body);

                        Local newArg = insertLabelIfNecessary(arg, locals, units, stmt, sinkStr, argNum);
                        if(newArg != null){
                            ie.setArg(argNum, newArg);
                        }
                    } else {
                       System.out.println("String analysis(missed): " + " | stmt: " + stmt + " | tags: " + stmt.getTags() 
                                        + " | string Map: " + valueMap+ " |Class: " + this.klass + " |Method: " 
                                        + method  +  " | body: " + body);
                    }
                }

            }
        }
    }

    private Local insertLabelIfNecessary(StringConstant strConst, Chain<Local> locals, Chain<Unit> units, 
                                         Unit currentStmt, String sinkStr, int argNum)
    {
        String str = strConst.value;

        SootMethod meth = getOrCreateLabelMethodFor(str, sinkStr);
        Local temp = Jimple.v().newLocal("stamp$stamp$tmp"+newLocalCount++, 
        RefType.v("java.lang.String"));
        locals.add(temp);
        Stmt stmt = (Stmt) currentStmt;
        InvokeExpr ie = stmt.getInvokeExpr();

        Stmt toInsert = Jimple.v().newAssignStmt(temp, Jimple.v().newStaticInvokeExpr(meth.makeRef(), ie.getArg(argNum)));
        units.insertBefore(toInsert, currentStmt);
        return temp;
    }


    private SootMethod getOrCreateLabelMethodFor(String label, String sinkStr)
    {
        String methName = "stamp$stamp$annotate" + newLocalCount++;
        SootMethod meth = srcLabelToLabelMethod.get(label);
        //if ( klass.declaresMethodByName(methName) ) {
        if(meth == null){
            RefType stringType = RefType.v("java.lang.String");
            List paramTypes = Arrays.asList(new Type[]{stringType});
            ///String methName = "stamp$stamp$annotate";
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

            System.out.println("%%% "+meth.getSignature());
            label = "strvalue(" +label+ ")";
            if ("*".equals(sinkStr)) {
                writeAnnotation(methName+":(Ljava/lang/String;)Ljava/lang/String;@"+klass.getName(), "$"+label, "-1");
                writeAnnotation(methName+":(Ljava/lang/String;)Ljava/lang/String;@"+klass.getName(), "!"+label, "-1");
            } else {
                writeAnnotation(methName+":(Ljava/lang/String;)Ljava/lang/String;@"+klass.getName(), sinkStr+label, "-1");
            }

        }
       /* else {
            System.out.println("methodname..." + methName);
            meth = klass.getMethodByName(methName);
        }
        */

        return meth;
    }

}
