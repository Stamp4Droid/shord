package stamp.analyses;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.grammars.E12;
import stamp.missingmodels.util.ConversionUtils;
import stamp.missingmodels.util.FileManager;
import stamp.missingmodels.util.FileManager.FileType;
import stamp.missingmodels.util.FileManager.StampFile;
import stamp.missingmodels.util.Relation;
import stamp.missingmodels.util.StubLookup;
import stamp.missingmodels.util.StubLookup.StubModel;
import stamp.missingmodels.util.StubLookup.StubModelSet;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.missingmodels.util.viz.jcflsolver.JCFLRelationFile;
import stamp.missingmodels.viz.flow.FlowWriter.AllStubInputsFile;
import stamp.missingmodels.viz.flow.FlowWriter.StubInputsFile;
import stamp.missingmodels.viz.flow.FlowWriter.StubModelSetFile;
import chord.project.Chord;

/*
 * An analysis that runs the JCFLSolver to do the taint analysis.
 */

@Chord(name = "jcflsolver")
public class JCFLSolverAnalysis extends JavaAnalysis {
	/*
	 * The following code is for running the JCFLSolver analysis.
	 */
	private void fillTerminalEdges(Graph g, StubLookup stubLookup, StubModelSet stubModelSet) {
		for(int k=0; k<g.numKinds(); k++) {
			if(g.isTerminal(k)) {
				if(ConversionUtils.getChordRelationsFor(g.kindToSymbol(k)).isEmpty()) {
					System.out.println("No edges found for relation " + g.kindToSymbol(k) + "...");
				}
				for(Relation rel : ConversionUtils.getChordRelationsFor(g.kindToSymbol(k))) {
					rel.addEdges(g.kindToSymbol(k), g, stubLookup, stubModelSet);
				}
			}
		}
	}
	
	/*
	 * The following code loads a stub model set from a buffered reader.
	 */
	public static StubModelSet getStubModelSet(BufferedReader br) throws IOException {
		StubModelSet m = new StubModelSet();
		String line;
		while((line = br.readLine()) != null) {
			String[] tokens = line.split("#");
			if(tokens.length != 2) {
				throw new RuntimeException("Error parsing stub model " + line + ", not the right number of tokens!");
			}
			try {
				m.put(new StubModel(tokens[0]), Integer.parseInt(tokens[1]));
			} catch(NumberFormatException e) {
				e.printStackTrace();
				throw new RuntimeException("Error parsing stub model " + line + ", number format exception!");
			}
		}
		return m;
	}
	
	private static Graph g = new E12();
	private static StubLookup s = new StubLookup();
	private static StubModelSet m = new StubModelSet();
	
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
		fillTerminalEdges(g, s, m);
		g.algo.process();

		printRelCounts(g);

		File outputDir = new File(System.getProperty("stamp.out.dir") + File.separator + "cfl");
		File scratchDir = new File(System.getProperty("stamp.out.dir") + File.separator + "cfl");
		
		Set<StampFile> files = new HashSet<StampFile>();
		for(int i=0; i<g.numKinds(); i++) {
			if(g.isTerminal(i)) {
				files.add(new JCFLRelationFile(FileType.OUTPUT, g, g.kindToSymbol(i), false));
			}
		}
		files.add(new JCFLRelationFile(FileType.OUTPUT, g, "Src2Sink", true));
		files.add(new AllStubInputsFile(g, s));
		files.add(new StubInputsFile(g, s));
		files.add(new StubModelSetFile(new StubModelSet(g, s)));
		//files.addAll(FlowWriter.viz(g, s));
		try {
			FileManager manager = new FileManager(outputDir, scratchDir, true);
			for(StampFile file : files) {
				manager.write(file);
			}
		} catch(IOException e) { e.printStackTrace(); }
	}
}
