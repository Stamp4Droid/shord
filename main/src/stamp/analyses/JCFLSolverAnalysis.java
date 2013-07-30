package stamp.analyses;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.grammars.C11;
import stamp.missingmodels.util.ConversionUtils;
import stamp.missingmodels.util.FileManager;
import stamp.missingmodels.util.FileManager.FileType;
import stamp.missingmodels.util.FileManager.StampFile;
import stamp.missingmodels.util.Relation;
import stamp.missingmodels.util.StubLookup;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.missingmodels.util.viz.jcflsolver.JCFLRelationFile;
import stamp.missingmodels.viz.flow.FlowWriter;
import stamp.missingmodels.viz.flow.FlowWriter.AllStubInputsFile;
import stamp.missingmodels.viz.flow.FlowWriter.StubInputsFile;
import chord.project.Chord;

/*
 * An analysis that runs the JCFLSolver to do the taint analysis.
 */

@Chord(name = "jcflsolver")
public class JCFLSolverAnalysis extends JavaAnalysis {
	/*
	 * The following code is for running the JCFLSolver analysis.
	 */
	private void fillTerminalEdges(Graph g, StubLookup stubLookup) {
		for(int k=0; k<g.numKinds(); k++) {
			if(g.isTerminal(k)) {
				if(ConversionUtils.getChordRelationsFor(g.kindToSymbol(k)).isEmpty()) {
					System.out.println("No edges found for relation " + g.kindToSymbol(k) + "...");
				}
				for(Relation rel : ConversionUtils.getChordRelationsFor(g.kindToSymbol(k))) {
					rel.addEdges(g.kindToSymbol(k), g, stubLookup);
				}
			}
		}
	}
	
	private static Graph g = new C11();
	private static StubLookup s = new StubLookup();
	
	public static Graph g() {
	    return g;
	}
	
	public static StubLookup s() {
	    return s;
	}

	public static void printRelCounts(Graph g) {
		System.out.println("Printing final relation counts...");
		for(int k=0; k<g.numKinds(); k++) {
			System.out.println(g.kindToSymbol(k) + ": " + g.getEdges(k).size());
		}
	}
	
	@Override public void run() {
		fillTerminalEdges(g, s);
		g.algo.process();

		printRelCounts(g);

		File outputDir = new File(System.getProperty("stamp.out.dir") + File.separator + "cfl");
		File scratchDir = new File(System.getProperty("stamp.out.dir") + File.separator + "cfl");
		
		Set<StampFile> files = new HashSet<StampFile>();
		files.add(new JCFLRelationFile(FileType.OUTPUT, g, "Src2Sink", true));
		files.add(new AllStubInputsFile(g, s));
		files.add(new StubInputsFile(g, s));
		files.addAll(FlowWriter.viz(g, s));
		try {
			FileManager manager = new FileManager(outputDir, scratchDir, true);
			for(StampFile file : files) {
				manager.write(file);
			}
		} catch(IOException e) { e.printStackTrace(); }
	}
}
