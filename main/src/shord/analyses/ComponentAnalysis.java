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
import stamp.analyses.ReachingDefsAnalysis;
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
 * Generating relations related to ICCG.
 * @author Yu Feng (yufeng@cs.stanford.edu)
 */

@Chord(name="comp-java",
       produces={"MC", "IntentTgtField", "ActionField", "DataTypeField", "Callbacks", "MregI" },
       namesOfTypes = {"COMP", "M", "I"},
       types = {DomComp.class, DomM.class, DomI.class},
       namesOfSigns = { "MC", "IntentTgtField", "ActionField", "DataTypeField", "Callbacks", "MregI" },
       signs = { "M0,COMP0:M0_COMP0", "F0:F0", "F0:F0", "F0:F0", "M0:M0", "M0,I0:M0_I0"}
       )
public class ComponentAnalysis extends JavaAnalysis
{

    private	DomM domM;
	private ProgramRel relMC;
    private ProgramRel relCallbacks;
    private ProgramRel relMregI;
	private SootClass klass;

	public static Map<String, XmlNode> components = new HashMap<String, XmlNode>();

    public static String pkgName = ""; 


    private static String[] subLaunchArray = {
        "void startActivity(android.content.Intent)",
        "void sendBroadcast(android.content.Intent)",
        "boolean bindService(android.content.Intent,android.content.ServiceConnection,int)",
        "android.content.ComponentName startService(android.content.Intent)",
        "void sendBroadcast(android.content.Intent,java.lang.String)",
        "void sendStickyBroadcast(android.content.Intent)",
        "void sendOrderedBroadcast(android.content.Intent,java.lang.String)",
        "void sendOrderedBroadcast(android.content.Intent,java.lang.String,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)",
        "void sendStickyOrderedBroadcast(android.content.Intent,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)",
        "void sendBroadcast(android.content.Intent)",
        "android.content.ComponentName startService(android.content.Intent)",
        "void sendBroadcast(android.content.Intent,java.lang.String)",
        "void sendStickyBroadcast(android.content.Intent)",
        "void sendOrderedBroadcast(android.content.Intent,java.lang.String)",
        "void sendOrderedBroadcast(android.content.Intent,java.lang.String,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)",
        "void sendStickyOrderedBroadcast(android.content.Intent,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)",
        "void startActivities(android.content.Intent[])",
        "void startIntentSender(android.content.IntentSender,android.content.Intent,int,int,int)",
        "void startActivityForResult(android.content.Intent,int)",
        "boolean startActivityIfNeeded(android.content.Intent,int)",
        "boolean startNextMatchingActivity(android.content.Intent)",
        "void startActivity(android.content.Intent)",
        "void setResult(int,android.content.Intent)",
        "void startActivityFromChild(android.app.Activity,android.content.Intent,int)",
        "void startActivityFromFragment(android.app.Fragment,android.content.Intent,int)",
        "void startIntentSenderForResult(android.content.IntentSender,int,android.content.Intent,int,int,int)",
        "void startIntentSenderFromChild(android.app.Activity,android.content.IntentSender,int,android.content.Intent,int,int,int)",
        "android.widget.TabHost$TabSpec setContent(android.content.Intent)"
	};

    public static List launchList = new ArrayList<String>(Arrays.asList(subLaunchArray));

    void openRels()
    {
        relMC = (ProgramRel) ClassicProject.g().getTrgt("MC");
        relMC.zero();

        relCallbacks = (ProgramRel) ClassicProject.g().getTrgt("Callbacks");
        relMregI= (ProgramRel) ClassicProject.g().getTrgt("MregI");
        relCallbacks.zero(); 
        relMregI.zero(); 
    }

    void saveRels() 
    {
        relMC.save();
        relCallbacks.save(); 
        relMregI.save(); 
    }

    public ComponentAnalysis()
    {
        String stampOutDir = System.getProperty("stamp.out.dir");
        //parse manifest.xml
        String manifestDir = stampOutDir + "/apktool-out";
        File manifestFile = new File(manifestDir, "AndroidManifest.xml");
        ParseManifest pmf = new ParseManifest();
        pmf.extractComponents(manifestFile, components);
	    pkgName = pmf.getPkgName();
        System.out.println("Current components: " + components);
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

        if(components.get(this.klass.getName()) != null) {

            String[] str = { 
                    "void onCreate(android.os.Bundle)",
                    "void onStart()",
                    "void onRestart()",
                    "void onResume()",
                    "void onPause()",
                    "void onStop()",
                    "void onDestroy()",

                    "void onReceive(android.content.Context,android.content.Intent)",

                    "void onCreate()",
                    "void onLowMemory()",
                    "void onRebind(android.content.Intent)",
                    "void onStart(android.content.Intent,int)",
                    "void onStartCommand(android.content.Intent,int,int)",

                    "void onConfigurationChanged(android.content.res.Configuration)",

                    "void onPostResume()",
                    "java.lang.CharSequence onCreateDescription()",
                    "void onRestoreInstanceState(android.os.Bundle)",
                    "void onPostCreate(android.os.Bundle)",
                    "void onUserLeaveHint()",
                    "boolean onCreateThumbnail(android.graphics.Bitmap,android.graphics.Canvas)",
                    "void onNewIntent(android.content.Intent)",
                    "void onSaveInstanceState(android.os.Bundle)"
                    
                    };

            List<String> circleList = Arrays.asList(str);

            //if(circleList.contains(method.getSubSignature())){
                String compKey = getCompKey(klass.getName());

                assert(compKey != null);
                if(domM.contains(method)) 
                    relMC.add(method, compKey);

           // }

        }

    }

    private void populateMregI() {

		Iterator mIt = Program.g().scene().getReachableMethods().listener();
		while(mIt.hasNext()){
			SootMethod method = (SootMethod) mIt.next();
	        if(!method.isConcrete())
                continue;

            Body body = method.retrieveActiveBody();
            for(Unit unit : body.getUnits()) {
                Stmt stmt = (Stmt)unit;
                if(stmt.containsInvokeExpr()){
                    InvokeExpr ie = stmt.getInvokeExpr();

                    //i is a statement that registercallback.
                    if(ie.getMethod().getSignature().equals(
                    "<edu.stanford.stamp.harness.ApplicationDriver: void registerCallback(edu.stanford.stamp.harness.Callback)>")) {
						/*
                        if(method.getSignature().equals("<android.app.Activity: void <init>()>") 
                        || method.getSignature().equals("<android.app.Service: void <init>()>")
                        || method.getSignature().equals("<android.content.BroadcastReceiver: void <init>()>"))
                            continue;
						*/
                        relMregI.add(method, stmt);
                    }

                }
            }
        }

    }

    private void populateCallback() {
        SootClass callback = Scene.v().getSootClass("edu.stanford.stamp.harness.Callback");
        for(SootClass subCallback : SootUtils.subTypesOf(callback)) {
            for(SootMethod method : subCallback.getMethods()){
                if("void run()".equals(method.getSubSignature()))
                    relCallbacks.add(method);
			}
        }

    }

    //find the actual string object in domComp.
    public static String getCompKey(String val)
    {
        String compKey = null;
        for(Object node : components.keySet()) {
            if(val.equals(node)){
                compKey = (String)node;
                break;
            }
        }

        return compKey;
    }

	//target name and action in intent object.
	private void populateIntentActionTgt() 
	{
		ProgramRel relIntentTgtField = (ProgramRel) ClassicProject.g().getTrgt("IntentTgtField");
        relIntentTgtField.zero();
		ProgramRel relActionField = (ProgramRel) ClassicProject.g().getTrgt("ActionField");
        relActionField.zero();
		ProgramRel relDataTypeField = (ProgramRel) ClassicProject.g().getTrgt("DataTypeField");
        relDataTypeField.zero();

		SootClass klass = Program.g().scene().getSootClass("android.content.Intent");
		SootField nameField = klass.getFieldByName("name");
		SootField actionField = klass.getFieldByName("action");
		SootField dataTypeField = klass.getFieldByName("type");

		relIntentTgtField.add(nameField);
		relIntentTgtField.save();

		relActionField.add(actionField);
		relActionField.save();

        relDataTypeField.add(dataTypeField);
        relDataTypeField.save();

	}

    public void run()
    {
        populateIntentActionTgt();
        openRels();
        domM = (DomM) ClassicProject.g().getTrgt("M");
        populateCallback(); 
        populateMregI(); 

        for(SootClass klass: Program.g().getClasses()){
            this.visit(klass);
        }
        saveRels();
    }


}
