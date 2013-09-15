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

        System.out.println("myparser:" + components);
        //plot every node to graph + unknown.
        Iterator iter = components.entrySet().iterator(); 
        while (iter.hasNext()) { 
            Map.Entry entry = (Map.Entry) iter.next(); 
            String key = (String)entry.getKey(); 
            XmlNode val = (XmlNode)entry.getValue(); 
            System.out.println("nodeinfo:" + val.getName() +" "+ val.getType()+" " + val.getPermission()+ " " + val.getMain());
            //ICCGNode iNode = new ICCGNode(val.getName().replace(".",""));
            ICCGNode iNode = new ICCGNode(val.getName());
            iNode.setMain(val.getMain());
            //iNode.setPermission(val.getPermission());
            if (val.getMain()) {
                iNode.setShape("diamond");
            } else if("activity".equals(val.getType())) {
                iNode.setShape("ellipse");
            } else if("service".equals(val.getType())) {
                iNode.setShape("circle");
            } else if("receiver".equals(val.getType())) {
                iNode.setShape("triangle");
            }
            iccg.addNode(iNode);
        } 

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
        List<SootClass> callbackList = subTypesOf(callback);
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

	if(components.get(this.klass.getName()) != null) 
		relMC.add(method, this.klass.getType());

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
                        System.out.println("addMregI---" + m + "||" + ie );
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
