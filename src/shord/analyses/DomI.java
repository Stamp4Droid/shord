package shord.analyses;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import shord.program.visitors.IInvokeInstVisitor;
import shord.analyses.DomM;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramDom;

import chord.project.Chord;
import chord.project.Config;


/**
 * Domain of method invocation quads.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(name = "I")
public class DomI extends ProgramDom<Unit> implements IInvokeInstVisitor 
{
    @Override
    public void visit(SootClass c) { }

    @Override
    public void visit(SootMethod m) { }

    @Override
    public void visitInvokeInst(Unit q) {
        add(q);
    }
}
