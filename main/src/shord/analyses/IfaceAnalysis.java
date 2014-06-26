package shord.analyses;

import soot.SootClass;
import soot.SootMethod;
import soot.Scene;

import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.ClassicProject;

import chord.project.Chord;

import java.util.*;

/**
 * Populate base relations required for interface extraction 
 * @author Saswat Anand
 */

@Chord(name="iface-java",
	   consumes={"M"},
       produces={"GetIntentExtraMeths"},
       namesOfSigns = {"GetIntentExtraMeths"},
       signs = {"M0:M0"}
       )
public class IfaceAnalysis extends JavaAnalysis
{
    private List<String> getIntentExtraMeths = Arrays.asList(new String[] {
			"int getIntExtra(java.lang.String,int)", 
			"float getFloatExtra(java.lang.String,float)", 
			"double getDoubleExtra(java.lang.String,double)", 
			"long getLongExtra(java.lang.String,long)", 
			"short getShortExtra(java.lang.String,short)", 
			"char getCharExtra(java.lang.String,char)", 
			"byte getByteExtra(java.lang.String,byte)",
			"boolean getBooleanExtra(java.lang.String,boolean)",
			"java.lang.String getStringExtra(java.lang.String,java.lang.String)", 
			"boolean[] getBooleanArrayExtra(java.lang.String)", 
			"android.os.Bundle getBundleExtra(java.lang.String)", 
			"byte[] getByteArrayExtra(java.lang.String)",
			"char[] getCharArrayExtra(java.lang.String)",  
			"java.lang.CharSequence[] getCharSequenceArrayExtra(java.lang.String)", 
			"java.util.ArrayList getCharSequenceArrayListExtra(java.lang.String)", 
			"java.lang.CharSequence getCharSequenceExtra(java.lang.String)", 
			"double[] getDoubleArrayExtra(java.lang.String)", 
			"float[] getFloatArrayExtra(java.lang.String)", 
			"int[] getIntArrayExtra(java.lang.String)", 
			"java.util.ArrayList getIntegerArrayListExtra(java.lang.String)",
			"long[] getLongArrayExtra(java.lang.String)", 
			"android.os.Parcelable[] getParcelableArrayExtra(java.lang.String)", 
			"java.util.ArrayList getParcelableArrayListExtra(java.lang.String)", 
			"android.os.Parcelable getParcelableExtra(java.lang.String)",
			"java.io.Serializable getSerializableExtra(java.lang.String)", 
			"short[] getShortArrayExtra(java.lang.String)", 
			"java.lang.String[] getStringArrayExtra(java.lang.String)", 
			"java.util.ArrayList getStringArrayListExtra(java.lang.String)" 
		});
	
			
	private void populateExtraMeths()
	{
        ProgramRel relExtraMeth = (ProgramRel) ClassicProject.g().getTrgt("GetIntentExtraMeths");
        relExtraMeth.zero();

		SootClass intentClass = Scene.v().getSootClass("android.content.Intent");
		for(String methName : getIntentExtraMeths){
			SootMethod m = intentClass.getMethod(methName);
			relExtraMeth.add(m);
		}

		relExtraMeth.save();
	}

    public void run()
    {
        populateExtraMeths();
    }
}