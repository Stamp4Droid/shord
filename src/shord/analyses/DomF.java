package shord.analyses;

import soot.SootField;
import shord.program.Program;
import shord.project.analyses.ProgramDom;

import chord.project.Chord;

import java.util.Iterator;
/**
 * Domain of classes.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(name = "F")
public class DomF extends ProgramDom<SootField> {
    @Override
    public void fill() {
        Program program = Program.g();
        Iterator<SootField> fieldsIt = program.getFields().iterator();
		while(fieldsIt.hasNext())
            add(fieldsIt.next());
    }
}
