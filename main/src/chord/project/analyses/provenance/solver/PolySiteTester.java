package chord.project.analyses.provenance.solver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import chord.project.Chord;
import chord.project.analyses.provenance.LookUpRule;
import chord.project.analyses.provenance.Tuple;

@Chord(name = "provenance-test")
public class PolySiteTester extends JavaAnalysis {
	private List<LookUpRule> rules = new ArrayList<LookUpRule>();
	
	@Override
	public void run() {
		String chordIncu = System.getenv("CHORD_INCUBATOR");
		String kcfaConfig = chordIncu+File.separator+"src/chord/analyses/provenance/kcfa/pro-cspa-kcfa-dlog_XZ89_.config";
		String monoConfig = chordIncu+File.separator+"src/chord/analyses/provenance/monosite/polysite-dlog_XZ89_.config";
		createRules(kcfaConfig);
		createRules(monoConfig);
		Tuple query = new Tuple("polySite(4684)");
		Set<String> paramRs = new HashSet<String>();
		paramRs.add("CC");
		paramRs.add("CH");
		paramRs.add("CI");
		Set<Tuple> its = new HashSet<Tuple>();
		ProgramRel reachableCM = (ProgramRel)ClassicProject.g().getTrgt("reachableCM");
		ProgramRel rootCM = (ProgramRel)ClassicProject.g().getTrgt("rootCM");
		int[] a1 = {0,0};
		Tuple t1 = new Tuple(reachableCM,a1);
		int[] a2 = {2};
		Tuple t2 = new Tuple(rootCM,a2);
		its.add(t1);
		its.add(t2);
		Solver solver = new Solver(query,1,rules,true,paramRs,its);
		solver.run();
		System.out.println(solver.getF());
	}
	
	private void createRules(String ruleFile) {
		try {
			Scanner sc = new Scanner(new File(ruleFile));
			while (sc.hasNext()) {
				String line = sc.nextLine().trim();
				if (!line.equals("")) {
					LookUpRule rule = new LookUpRule(line);
					rules.add(rule);
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
}
