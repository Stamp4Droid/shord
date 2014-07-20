package chord.analyses.provenance.kcfa;

import shord.analyses.DomC;
import shord.project.analyses.ProgramRel;
import chord.project.Chord;

@Chord(
	    name = "RelC",
	    consumes = { "C" },
	    sign = "C0:C0"
	)
public class RelC extends ProgramRel{

	@Override
	public void fill() {
		DomC domC = (DomC) doms[0];
        for(int i = 0;i < domC.size();i ++){
        	this.add(i);
        }
	}
	
}
