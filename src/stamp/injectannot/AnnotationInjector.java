package stamp.injectannot;

import soot.*;
import soot.jimple.*;
import soot.util.*;

import shord.project.analyses.JavaAnalysis;
import shord.program.Program;

import chord.project.Chord;

import java.util.*;
import java.io.*;

@Chord(name="inject-annot")
public class AnnotationInjector extends JavaAnalysis
{
	static abstract class Visitor
	{
		private PrintWriter writer;

		protected abstract void visit(SootClass klass);
		
		protected void writeAnnotation(String methSig, String from, String to)
		{
			writer.println(methSig + " " + from + " " + to);
		}
	}
	
	private Class[] visitorClasses = new Class[]{
		ContentProviderAnnotation.class
		,NativeMethodAnnotation.class
	};

	private PrintWriter writer;

	public void run()
	{
		try{			
			String stampOutDir = System.getProperty("stamp.out.dir");
			File annotFile = new File(stampOutDir, "stamp_annotations.txt");
			writer = new PrintWriter(new FileWriter(annotFile, true));

			for(Class visitorClass : visitorClasses){
				Visitor v = (Visitor) visitorClass.newInstance();
				v.writer = writer;

				for(SootClass klass : Program.g().getClasses()){
					v.visit(klass);
				}
			}
			writer.close();
		}catch(Exception e){
			throw new Error(e);
		}		
	}
	
	
}