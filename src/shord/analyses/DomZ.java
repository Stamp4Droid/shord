package shord.analyses;

import shord.project.analyses.ProgramDom;

import chord.project.Chord;

/**
 * Domain of classes.
 * 
 * @author Saswat Anand
 */
@Chord(name = "Z")
public class DomZ extends ProgramDom<Integer> {
	public static final int MAX_Z = 127;

    @Override
    public void fill() {
		for(int i = 0; i < MAX_Z; i++)
			add(new Integer(i));
    }
}
