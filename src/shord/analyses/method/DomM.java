package shord.analyses.method;

import soot.SootMethod;
import soot.SootClass;

import shord.program.Program;
import shord.program.visitors.IMethodVisitor;
import shord.project.analyses.ProgramDom;

import chord.project.Chord;

/**
 * Domain of methods.
 * <p>
 * The 0th element in this domain is the main method of the program.
 * <p>
 * The 1st element in this domain is the <tt>start()</tt> method of class <tt>java.lang.Thread</tt>,
 * if this method is reachable from the main method of the program.
 * <p>
 * The above two methods are the entry-point methods of the implicitly created main thread and each
 * explicitly created thread, respectively.  Due to Chord's emphasis on concurrency, these methods
 * are referenced frequently by various pre-defined program analyses expressed in Datalog, and giving
 * them special indices makes it convenient to reference them in those analyses.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 * @author Saswat Anand
 */
@Chord(name = "M")
public class DomM extends ProgramDom<SootMethod> implements IMethodVisitor {
    @Override
    public void init() {
        // Reserve index 0 for the main method of the program.
        // Reserve index 1 for the start() method of java.lang.Thread if it exists.
        Program program = Program.g();
        SootMethod mainMethod = program.getMainMethod();
        assert (mainMethod != null);
        getOrAdd(mainMethod);
    }

    @Override
    public void visit(SootClass c) { }

    @Override
    public void visit(SootMethod m) {
        getOrAdd(m);
    }
}
