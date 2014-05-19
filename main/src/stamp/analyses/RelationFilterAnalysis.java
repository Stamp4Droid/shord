package stamp.analyses;

import java.util.Random;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import chord.project.Chord;

@Chord(name = "relation-filter-java",
		consumes = { "param" },
		produces = { "paramFilter1", "paramFilter2", "paramFilter3", "paramFilter4", "paramFilter5", "paramFilter6" },
		namesOfTypes = { },
		types = { },
		namesOfSigns = { "paramFilter1", "paramFilter2", "paramFilter3", "paramFilter4", "paramFilter5", "paramFilter6" },
		signs = { "V0,V1,I0:V0xV1_I0", "V0,V1,I0:V0xV1_I0", "V0,V1,I0:V0xV1_I0", "V0,V1,I0:V0xV1_I0", "V0,V1,I0:V0xV1_I0", "V0,V1,I0:V0xV1_I0" }
		)
public class RelationFilterAnalysis extends JavaAnalysis {
	private static final double[] PROBABILITIES = {0.25, 0.5, 0.75, 0.85, 0.95, 1.0};
	private static Random random;

	private static void filter(String relationName, String filteredRelationName, double probability) {
		ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relationName);
		ProgramRel relFilter = (ProgramRel)ClassicProject.g().getTrgt(filteredRelationName);

		rel.load();
		relFilter.zero();

		for(int[] tuple : rel.getAryNIntTuples()) {
			if(random.nextDouble() < probability) {
				relFilter.add(tuple);
			}
		}

		rel.close();
		relFilter.save();
	}

	@Override
	public void run() {
		random = new Random(3141592);
		for(int i=1; i<=6; i++) {
			filter("param", "paramFilter" + Integer.toString(i), PROBABILITIES[i-1]);
		}
	}
}
