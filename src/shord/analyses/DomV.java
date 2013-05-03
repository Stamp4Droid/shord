package shord.analyses;

import soot.Local;

import shord.project.analyses.ProgramDom;
import shord.program.Program;
import shord.program.visitors.IMethodVisitor;

import chord.project.Chord;


public class DomV extends ProgramDom<Local> implements IMethodVisitor 
{
    @Override
	public void visit(SootMethod m) {
        if (!m.isConcrete())
            return;
		for(Local l : m.retrieveActiveBody().getLocals()){
			add(l);
		}
	}
}