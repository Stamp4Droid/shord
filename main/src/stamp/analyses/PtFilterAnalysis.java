package stamp.analyses;

import java.util.Random;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import chord.project.Chord;

@Chord(name = "pt-filter-java",
	   consumes = { "pt" },
	   produces = { "ptFilter" },
	   namesOfTypes = { },
	   types = { },
	   namesOfSigns = { "ptFilter" },
	   signs = { "C0,V0,C1:C0xC1_V0" }
	   )
public class PtFilterAnalysis extends JavaAnalysis {
	private static final double PROBABILITY = 0.5;
	
	@Override
	public void run() {
		Random random = new Random();
		
		ProgramRel relPt = (ProgramRel)ClassicProject.g().getTrgt("pt");
		ProgramRel relPtFilter = (ProgramRel)ClassicProject.g().getTrgt("ptFilter");
		
		relPt.load();
		relPtFilter.zero();
		
		for(int[] tuple : relPt.getAryNIntTuples()) {
			if(random.nextDouble() < PROBABILITY) {
				relPtFilter.add(tuple);
			}
		}
		
		relPt.close();
		relPtFilter.save();
	}
}
