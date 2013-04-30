package shord.analyses;

import chord.project.Chord;
import shord.project.analyses.JavaAnalysis;

import shord.program.Program;

import soot.SootClass;

@Chord(name="test-java")
public class Test extends JavaAnalysis
{
	public void run()
	{
		Program program = Program.g();
		program.build();
		//for(SootClass klass : program.getClasses())
		//	System.out.println(klass.getName());
	}
}