package stamp.analysis;

import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.PackManager;
import soot.MethodOrMethodContext;

import chord.project.Chord; 
import shord.project.analyses.JavaAnalysis;

import java.util.*;

/*
 * @author Saswat Anand
 */
@Chord(name="iccg-ondemand-java")
public class IccgOnDemand extends JavaAnalysis
{
    private List<String> iccMeths = Arrays.asList(new String[] {
        "<android.content.ContextWrapper: void startActivity(android.content.Intent)>",
        "<android.app.Activity: void startActivityForResult(android.content.Intent,int)>",
        "<android.app.Activity: void startActivityForResult(android.content.Intent,int,android.os.Bundle)>",
        "<android.app.Activity: boolean startActivityIfNeeded(android.content.Intent,int)>",
        "<android.app.Activity: boolean startActivityIfNeeded(android.content.Intent,int,android.os.Bundle)>",
        "<android.app.Activity: boolean startNextMatchingActivity(android.content.Intent)>",
        "<android.app.Activity: boolean startNextMatchingActivity(android.content.Intent,android.os.Bundle)>",
        "<android.app.Activity: void startActivity(android.content.Intent)>"
		});


	public void run() 
	{
		setup();
		
		Set<SootMethod> meths = new HashSet();
		for(String methSig : iccMeths){
			if(!Scene.v().containsMethod(methSig))
				continue;
			SootMethod m = Scene.v().getMethod(methSig);
			meths.add(m);
		}

		for(Iterator<MethodOrMethodContext> it = Scene.v().getReachableMethods().listener(); it.hasNext();){
			Method m = (SootMethod) it.next();
			if(meths.contains(m))
				System.out.println("R: "+m);
			
		}

	}
	
	private void setup()
	{
		//run spark
		Transform sparkTransform = PackManager.v().getTransform( "cg.spark" );
		String defaultOptions = sparkTransform.getDefaultOptions();
		StringBuilder options = new StringBuilder();
		options.append("enabled:true");
		options.append(" verbose:true");
		options.append(" cs-demand:true");
		//options.append(" dump-answer:true");
		options.append(" "+defaultOptions);
		System.out.println("spark options: "+options.toString());
		sparkTransform.setDefaultOptions(options.toString());
		sparkTransform.apply();		
	}
}