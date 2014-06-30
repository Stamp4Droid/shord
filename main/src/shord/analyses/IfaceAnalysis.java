package shord.analyses;

import soot.SootClass;
import soot.SootMethod;
import soot.Scene;

import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.ClassicProject;
import shord.program.Program;

import chord.project.Chord;
import chord.util.tuple.object.Pair;

import java.util.*;

/**
 * Populate base relations required for interface extraction 
 * @author Saswat Anand
 */

@Chord(name="iface-java-1",
	   consumes={"M"},
       produces={"IntentExtraMeths", "PutSimpleIntentExtraMeths", "PutCompositeIntentExtraMeths"},
       namesOfSigns = {"IntentExtraMeths", "PutSimpleIntentExtraMeths", "PutCompositeIntentExtraMeths"},
       signs = {"M0:M0", "M0:M0", "M0:M0"}
       )
public class IfaceAnalysis extends JavaAnalysis
{
    public static final List<String> intentExtraMeths = Arrays.asList(new String[] {
			//get methods
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
			"java.util.ArrayList getStringArrayListExtra(java.lang.String)", 

			//set methods
			"android.content.Intent setData(android.net.Uri)",
			"android.content.Intent setDataAndNormalize(android.net.Uri)",
			"android.content.Intent setDataAndType(android.net.Uri,java.lang.String)",
			"android.content.Intent setDataAndTypeAndNormalize(android.net.Uri,java.lang.String)"
		});

	public static final List<String> putSimpleIntentExtraMeths = Arrays.asList(new String[] {
			//put methods for non-composite types
			"android.content.Intent putExtra(java.lang.String,int)", 
			"android.content.Intent putExtra(java.lang.String,float)", 
			"android.content.Intent putExtra(java.lang.String,double)", 
			"android.content.Intent putExtra(java.lang.String,long)", 
			"android.content.Intent putExtra(java.lang.String,short)", 
			"android.content.Intent putExtra(java.lang.String,char)", 
			"android.content.Intent putExtra(java.lang.String,byte)",
			"android.content.Intent putExtra(java.lang.String,boolean)",
			"android.content.Intent putExtra(java.lang.String,java.lang.String)", 
			"android.content.Intent putExtra(java.lang.String,boolean[])", 
			"android.content.Intent putExtra(java.lang.String,byte[])",
			"android.content.Intent putExtra(java.lang.String,char[])",  
			"android.content.Intent putExtra(java.lang.String,java.lang.CharSequence)", 
			"android.content.Intent putExtra(java.lang.String,double[])", 
			"android.content.Intent putExtra(java.lang.String,float[])", 
			"android.content.Intent putExtra(java.lang.String,int[])", 
			"android.content.Intent putExtra(java.lang.String,long[])", 
			"android.content.Intent putExtra(java.lang.String,short[])", 
			"android.content.Intent putExtra(java.lang.String,java.lang.String[])"
		});

	public static final List<String> putIntentExtrasMeths = Arrays.asList(new String[] {
			"android.content.Intent putExtras(android.os.Bundle)",
			"android.content.Intent putExtras(android.content.Intent)"
		});
		
	public static final List<String> putCompositeIntentExtraMeths = Arrays.asList(new String[] {
			"android.content.Intent putExtra(java.lang.String,android.os.Bundle)", 
			"android.content.Intent putExtra(java.lang.String,android.os.Parcelable)",
			"android.content.Intent putExtra(java.lang.String,java.io.Serializable)",

			"android.content.Intent putStringArrayListExtra(java.lang.String,java.util.ArrayList)", 
			"android.content.Intent putCharSequenceArrayListExtra(java.lang.String,java.util.ArrayList)", 
			"android.content.Intent putIntegerArrayListExtra(java.lang.String,java.util.ArrayList)",
			"android.content.Intent putParcelableArrayListExtra(java.lang.String,java.util.ArrayList)"
		});

	private void populateExtraMeths()
	{
        ProgramRel relExtraMeth = (ProgramRel) ClassicProject.g().getTrgt("IntentExtraMeths");
        relExtraMeth.zero();

		SootClass intentClass = Scene.v().getSootClass("android.content.Intent");
		for(String methName : intentExtraMeths){
			SootMethod m = intentClass.getMethod(methName);
			relExtraMeth.add(m);
		}
		for(String methName : putIntentExtrasMeths){
			SootMethod m = intentClass.getMethod(methName);
			relExtraMeth.add(m);
		}

		for(String methName : putSimpleIntentExtraMeths){
			SootMethod m = intentClass.getMethod(methName);
			relExtraMeth.add(m);
		}

		for(String methName : putCompositeIntentExtraMeths){
			SootMethod m = intentClass.getMethod(methName);
			relExtraMeth.add(m);
		}
		relExtraMeth.save();

		ProgramRel relCompositeExtraMeth = (ProgramRel) ClassicProject.g().getTrgt("PutCompositeIntentExtraMeths");
        relCompositeExtraMeth.zero();
		for(String methName : putCompositeIntentExtraMeths){
			SootMethod m = intentClass.getMethod(methName);
			relCompositeExtraMeth.add(m);
		}
		relCompositeExtraMeth.save();

		ProgramRel relSimpleExtraMeth = (ProgramRel) ClassicProject.g().getTrgt("PutSimpleIntentExtraMeths");
        relSimpleExtraMeth.zero();
		for(String methName : putSimpleIntentExtraMeths){
			SootMethod m = intentClass.getMethod(methName);
			relSimpleExtraMeth.add(m);
		}
		relSimpleExtraMeth.save();

	}

    public void run()
    {
        populateExtraMeths();
    }
}