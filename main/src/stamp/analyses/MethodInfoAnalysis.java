package stamp.analyses;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import shord.project.analyses.JavaAnalysis;
import java.util.HashMap;
import java.util.Map;
import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.tagkit.Tag;
import soot.toolkits.graph.UnitGraph;
import chord.project.Chord;

/*
 * An analysis to collect and print out method information.
 */

@Chord(name = "methodinfo")
public class MethodInfoAnalysis extends JavaAnalysis {
    private static Map<String,MethodInfo> methodInfosBySig = new HashMap<String,MethodInfo>();

    public static class MethodInfo {
	public final String sig;
	public final int numArgs;

	public MethodInfo(SootMethod m) {
	    if(!m.hasActiveBody()) {
		throw new RuntimeException("Currently MethodInfo can only be built for non abstract methods!");
	    }
	    this.sig = m.getSignature();
	    this.numArgs = m.getParameterCount();
	}
    }

    @Override
    public void run() {
	for(SootClass cl : Scene.v().getClasses()) {
	    for(SootMethod m : cl.getMethods()) {
		if(m.hasActiveBody()) {
		    this.methodInfosBySig.put(m.getSignature(), new MethodInfo(m));
		}
	    }
	}
    }
}

