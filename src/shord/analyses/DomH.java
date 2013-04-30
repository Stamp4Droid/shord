package shord.analyses.invk;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import shord.program.visitors.INewInstVisitor;
import shord.analyses.method.DomM;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramDom;

import chord.project.Chord;
import chord.project.Config;


/**
 * Domain of method invocation quads.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(name = "H")
public class DomH extends ProgramDom<Unit> implements INewInstVisitor
{
    @Override
    public void visit(SootClass c) { }

    @Override
    public void visit(SootMethod m) { }

    @Override
    public void visitNewInst(Unit q) {
        add(q);
    }
}
