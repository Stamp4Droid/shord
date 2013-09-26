package shord.analyses;

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
import stamp.analyses.iccg.*;
import stamp.harnessgen.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import shord.program.Program;

import soot.util.NumberedSet;

import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.analyses.ProgramDom;
import shord.project.ClassicProject;

import chord.project.Chord;
import stamp.analyses.SootUtils;

import java.util.jar.*;
import java.io.*;

/**
  * Search for implicit intent.
  **/

@Chord(name="base-java-iccg",
       produces={ "MC", "Callbacks", "MregI" },
       namesOfTypes = { "M", "T", "I"},
       types = { DomM.class, DomT.class, DomI.class},
       namesOfSigns = { "MC", "Callbacks", "MregI" },
       signs = { "M0,T0:M0_T0", "M0:M0", "M0,I0:M0_I0" }
       )
public class ICCGBuilder extends JavaAnalysis
{

    private ProgramRel relMC;
    private ProgramRel relCallbacks;
    private ProgramRel relMregI;

    private int maxArgs = -1;
    private FastHierarchy fh;
    public static NumberedSet stubMethods;

    public static final boolean ignoreStubs = false;

    private SootClass klass;

    private Map<String, XmlNode> components = new HashMap<String, XmlNode>();

    void openRels()
    {
        relMC = (ProgramRel) ClassicProject.g().getTrgt("MC");
        relCallbacks = (ProgramRel) ClassicProject.g().getTrgt("Callbacks");
        relMregI= (ProgramRel) ClassicProject.g().getTrgt("MregI");
        relMC.zero();
        relCallbacks.zero(); 
        relMregI.zero(); 
    }

    void saveRels() 
    {
        relMC.save();
        relCallbacks.save(); 
        relMregI.save(); 
    }

    public ICCGBuilder()
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
Collection<SootMethod> methodsCopy = new ArrayList(klass.getMethods());
for(SootMethod method : methodsCopy)
    visitMethod(method);
}

    private void fillCallback() {

        SootClass callback = Scene.v().getSootClass("edu.stanford.stamp.harness.Callback");
        List<SootClass> callbackList = SootUtils.subTypesOf(callback);
        for(SootClass subCallback:callbackList) {
            Collection<SootMethod> methodsCopy = new ArrayList(subCallback.getMethods());
            for(SootMethod method : methodsCopy) {
                if("void run()".equals(method.getSubSignature())) {
                   //android.os.Handler.Callback or edu.stanford.stamp.harness.Callback
                    relCallbacks.add(method);
                }

            }
        }

    }
	
    private void visitMethod(SootMethod method)
    {
	if(!method.isConcrete())
		return;

	if(components.get(this.klass.getName()) != null) {
		String[] str = { "void onPostResume()",
				"java.lang.CharSequence onCreateDescription()",
				"void onRestoreInstanceState(android.os.Bundle)",
				"void onPostCreate(android.os.Bundle)",
				"void onStart()",
				"void onCreate(android.os.Bundle)",
				"void onUserLeaveHint()",
				"void onResume()",
				"void onStop()",
				"void onPause()",
				"void onRestart()",
				"boolean onCreateThumbnail(android.graphics.Bitmap,android.graphics.Canvas)",
				"void onNewIntent(android.content.Intent)",
				"void onDestroy()",
				"void onSaveInstanceState(android.os.Bundle)"};
		List<String> circleList = Arrays.asList(str);
		if(circleList.contains(method.getSubSignature()))
			relMC.add(method, this.klass.getType());
	}

    }

    private void populateMregI() 
    {
        Iterator mIt = Program.g().scene().getReachableMethods().listener();
        while(mIt.hasNext()){
            SootMethod m = (SootMethod) mIt.next();
            if(ignoreStubs){
                if(stubMethods.contains(m))
                    continue;
            }

            if(!m.isConcrete()) continue;

            Body body = m.retrieveActiveBody();
            for(Unit unit : body.getUnits()) {
                Stmt stmt = (Stmt)unit;
                if(stmt.containsInvokeExpr()){
                    InvokeExpr ie = stmt.getInvokeExpr();

                    //i is a statement that registercallback.
                    if(ie.getMethod().getSignature().equals(
                        "<edu.stanford.stamp.harness.ApplicationDriver: void registerCallback(edu.stanford.stamp.harness.Callback)>")) {
			//FIX-ME: without this, we will have redundant edges.
                        if(m.getSignature().equals("<android.app.Activity: void <init>()>")) continue;
                        relMregI.add(m, stmt);
                    }

                }
            }


        }

    }

    public void run()
    {
        Program program = Program.g();
        program.buildCallGraph();
        fh = Program.g().scene().getOrMakeFastHierarchy();
        openRels();
        fillCallback();
        populateMregI();
        for(SootClass klass: Program.g().getClasses()){
            this.visit(klass);
        }
        saveRels();

        fh = null;

    }


}

