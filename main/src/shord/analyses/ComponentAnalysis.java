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
       produces={"COMP", "MC"},
       namesOfTypes = {"COMP", "M"},
       types = {DomComp.class, DomM.class},
       namesOfSigns = { "MC"},
       signs = { "M0,COMP0:M0_COMP0"}
       )
public class ComponentAnalysis extends JavaAnalysis
{

    private	DomM domM;
	private ProgramRel relMC;
	private SootClass klass;

	public static Map<String, XmlNode> components = new HashMap<String, XmlNode>();

    public static String pkgName = ""; 

    void openRels()
    {
        relMC = (ProgramRel) ClassicProject.g().getTrgt("MC");
        relMC.zero();
    }

    void saveRels() 
    {
        relMC.save();
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

            if(circleList.contains(method.getSubSignature())){
                String compKey = getCompKey(klass.getName());

                assert(compKey != null);
                if(domM.contains(method)) 
                    relMC.add(method, compKey);

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

	private void populateDomComp() 
	{
		DomComp domComp = (DomComp) ClassicProject.g().getTrgt("COMP");
        for(Object node : components.keySet()) {
            domComp.add((String)node);
        }
        domComp.save();
	}

    public void run()
    {
        populateDomComp();
        openRels();
        domM = (DomM) ClassicProject.g().getTrgt("M");

        for(SootClass klass: Program.g().getClasses()){
            this.visit(klass);
        }
        saveRels();
    }


}
