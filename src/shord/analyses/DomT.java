package shord.analyses;

import soot.Type;
import shord.program.Program;
import shord.project.analyses.ProgramDom;

import chord.project.Chord;

import java.util.Iterator;
/**
 * Domain of classes.
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
@Chord(name = "T")
public class DomT extends ProgramDom<Type> {
    @Override
    public void fill() {
        Program program = Program.g();
        Iterator<Type> typesIt = program.getTypes().iterator();
		while(typesIt.hasNext())
            add(typesIt.next());
    }
}
