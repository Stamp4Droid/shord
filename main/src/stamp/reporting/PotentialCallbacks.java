package stamp.reporting;

import soot.SootClass;
import soot.SootMethod;
import soot.Modifier;
import soot.util.NumberedString;

import java.util.*;

import shord.project.ClassicProject;
import shord.program.Program;
import shord.project.analyses.ProgramRel;

import stamp.srcmap.SourceInfo;

public class PotentialCallbacks extends XMLReport
{
	private Map<SootMethod, List<SootMethod>> frameworkMethodToCallbacks = new HashMap();
	private Map<NumberedString, SootMethod> sigToMethod;
	private Set<NumberedString> sigs;

    public PotentialCallbacks()
	{
		super("Possibly-missing Callback Methods");
    }

    public void generate()
	{
        Program program = Program.g();
		for(SootClass c : program.getClasses()) {
			if(SourceInfo.isFrameworkClass(c))
				continue;
			sigs = new HashSet();
			sigToMethod = new HashMap();

            for(SootMethod m : c.getMethods()) {
				if(uninteresting(m))
					continue;
				NumberedString s = m.getNumberedSubSignature();
				sigs.add(s);
				sigToMethod.put(s, m);
			}

			findCallbacks(c);
		}

		ProgramRel relReachableM = (ProgramRel) ClassicProject.g().getTrgt("out_reachableM");
		relReachableM.load();

		//IndexSet<jq_Method> inScopeMethods = program.getMethods();
		for(Map.Entry<SootMethod, List<SootMethod>> entry : frameworkMethodToCallbacks.entrySet()){
			SootMethod fmeth = entry.getKey();
			Category cat = null;
			for(SootMethod cb : entry.getValue()){
				boolean flag = true;
				//if(inScopeMethods.contains(cb))
				flag = !relReachableM.contains(cb);
				if(flag){
					if(cat == null)
						cat = makeOrGetPkgCat(fmeth);
					cat.newTuple().addValue(cb, true, "method");
				}
			}
		}
		relReachableM.close();
	}

	private void addCallback(SootMethod frameworkMethod, SootMethod callback)
	{
		List<SootMethod> callbacks = frameworkMethodToCallbacks.get(frameworkMethod);
		if(callbacks == null){
			callbacks = new ArrayList();
			frameworkMethodToCallbacks.put(frameworkMethod, callbacks);
		}
		callbacks.add(callback);
	}

	private void findCallbacks(SootClass klass)
	{
		if(!klass.hasSuperclass())
			return;
		SootClass superClass = klass.getSuperclass();

		if(SourceInfo.isFrameworkClass(superClass)){
			if(!superClass.getName().equals("java.lang.Object")) {
				for(SootMethod superMeth : superClass.getMethods()){
					if(!canBeOverriden(superMeth))
						continue;
					NumberedString superMethSig = superMeth.getNumberedSubSignature();
					if(sigs.contains(superMethSig)){
						addCallback(superMeth, sigToMethod.get(superMethSig));
					}
				}
			}
		}
		findCallbacks(superClass);

		for(SootClass iface : klass.getInterfaces()){
			if(SourceInfo.isFrameworkClass(iface)){
				for(SootMethod superMeth : iface.getMethods()){
					NumberedString superMethSig = superMeth.getNumberedSubSignature();
					if(sigs.contains(superMethSig)){
						addCallback(superMeth, sigToMethod.get(superMethSig));
					}
				}
			}
			findCallbacks(iface);
		}
	}
	
	private static boolean uninteresting(SootMethod m)
	{
		return !m.isConcrete() ||
			m.isPrivate() || 
			m.isStatic() || 
			m.getName().equals("<init>");
	}
	
	private static boolean canBeOverriden(SootMethod m)
	{
		if(uninteresting(m) || Modifier.isFinal(m.getModifiers()))
			return false;
		return false;
	}

}
