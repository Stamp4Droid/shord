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
import stamp.analyses.ImplicitIntentDef;
import stamp.analyses.ReachingDefsAnalysis;


/**
  * Search for implicit intent.
  **/
public class IcdfAnnotation extends AnnotationInjector.Visitor
{


    private SootClass klass;

	protected void postVisit()
	{
	}

    public IcdfAnnotation()
    {
    }

	protected void visit(SootClass klass)
    {
        this.klass = klass;
        Collection<SootMethod> methodsCopy = new ArrayList(klass.getMethods());
        for(SootMethod method : methodsCopy)
            visitMethod(method);
    }
	
    private void visitMethod(SootMethod method)
    {
        if(!method.isConcrete())
            return;

        Body body = method.retrieveActiveBody();
        UnitGraph g = new ExceptionalUnitGraph(body);
        ImplicitIntentDef sld = new ImplicitIntentDef(g, new SimpleLiveLocals(g));
        //Running transitive reaching def to grep the values of intent filter.
        ReachingDefsAnalysis.runReachingDef(body);

        Chain<Local> locals = body.getLocals();
        Chain<Unit> units = body.getUnits();
        Iterator<Unit> uit = units.snapshotIterator();
        while(uit.hasNext()){
            Stmt stmt = (Stmt) uit.next();

            if(stmt.containsInvokeExpr()){
                InvokeExpr ie = stmt.getInvokeExpr();
                String methSig = ie.getMethod().getSignature();
                //System.out.println("exit method:" + ie.getMethod().getSignature());
                //list of apis to send intent.
                if (
                    methSig.equals("<android.app.Activity: void startActivity(android.content.Intent)>")
                    || methSig.equals("<android.content.ContextWrapper: void sendBroadcast(android.content.Intent)>")
                    //shall we mark bindservice?|| methSig.equals("<android.content.ContextWrapper: boolean bindService(android.content.Intent,android.content.ServiceConnection,int)>") 
                    || methSig.equals("<android.app.Activity: void startActivities(android.content.Intent[])>")
                    || methSig.equals("<android.content.ContextWrapper: android.content.ComponentName startService(android.content.Intent)>")
                    || methSig.equals("<android.content.ContextWrapper: void sendBroadcast(android.content.Intent,java.lang.String)>")
                    || methSig.equals("<android.content.ContextWrapper: void sendStickyBroadcast(android.content.Intent)>")
                    || methSig.equals("<android.content.ContextWrapper: void sendOrderedBroadcast(android.content.Intent,java.lang.String)>")
                    || methSig.equals("<android.content.ContextWrapper: void sendOrderedBroadcast(android.content.Intent,java.lang.String,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)>")
                    || methSig.equals("<android.content.ContextWrapper: void sendStickyOrderedBroadcast(android.content.Intent,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)>")
                    || methSig.equals("<android.app.Activity: void startIntentSender(android.content.IntentSender,android.content.Intent,int,int,int)>")
                    || methSig.equals("<android.app.Activity: void startActivityForResult(android.content.Intent,int)>")
                    || methSig.equals("<android.app.Activity: boolean startActivityIfNeeded(android.content.Intent,int)>")
                    || methSig.equals("<android.app.Activity: boolean startNextMatchingActivity(android.content.Intent)>")
                    || methSig.equals("<android.app.Activity: void startActivityFromChild(android.app.Activity,android.content.Intent,int)>")
                    || methSig.equals("<android.app.Activity: void startActivityFromFragment(android.app.Fragment,android.content.Intent,int)>")
                    || methSig.equals("<android.app.Activity: void startIntentSenderForResult(android.content.IntentSender,int,android.content.Intent,int,int,int)>")
                    || methSig.equals("<android.app.Activity: void startIntentSenderFromChild(android.app.Activity,android.content.IntentSender,int,android.content.Intent,int,int,int)>")
                ){

                    boolean isImplicit = sld.checkImplicit(stmt);
                    //Output intent results.
                    System.out.println("Implicit intent?" + isImplicit + "# Class: " + this.klass + "# Method: " 
                                        + method + "# Stmt: " + stmt);
                }
                ///param values for setAction, setCategory and those related APIs.
                if (
                    methSig.equals("<android.content.Intent: void <init>(java.lang.String,android.net.Uri,android.content.Context,java.lang.Class)>")
                    || methSig.equals("<android.content.Intent: android.content.Intent setAction(java.lang.String)>")
                    || methSig.equals("<android.content.Intent: android.content.Intent addCategory(java.lang.String)>")
                    || methSig.equals("<android.net.Uri: android.net.Uri parse(java.lang.String)>")
                    || methSig.equals("<android.content.Intent: void <init>(java.lang.String,android.net.Uri)>")
                    || methSig.equals("<android.content.Intent: void <init>(java.lang.String)>")
                    ) {
                    //Output intent filter, target info.
                    System.out.println("Intent filter:" + " Class:" + this.klass + " Method:" + method 
                                        + " Stmt:" + stmt + " Defs:" + stmt.getTags());

                }

            }
        }
    }
}
