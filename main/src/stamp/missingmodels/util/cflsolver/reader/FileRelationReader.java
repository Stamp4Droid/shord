package stamp.missingmodels.util.cflsolver.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver.TypeFilter;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar;
import stamp.missingmodels.util.jcflsolver2.RelationManager;
import stamp.missingmodels.util.jcflsolver2.RelationManager.Relation;
import stamp.missingmodels.util.jcflsolver2.RelationManager.RelationReader;

public class FileRelationReader implements RelationReader {
	private final File directory;
	
	public FileRelationReader(File directory) {
		this.directory = directory;
	}

	@Override
	public Graph readGraph(RelationManager relations, ContextFreeGrammar contextFreeGrammar) {
		GraphBuilder gb = new GraphBuilder(contextFreeGrammar);
		for(File relationFile : this.directory.listFiles()) {
			try {
				String relationName = relationFile.getName().split("\\.")[0];
				BufferedReader br = new BufferedReader(new FileReader(relationFile));
				readRelation(relations, br, gb, relationName);
			} catch(Exception e) {}
		}
		return gb.toGraph();
	}
	
	private static void readRelation(RelationManager relations, BufferedReader br, GraphBuilder gb, String relationName) throws IOException {
		br.readLine();
		String line;
		while((line = br.readLine()) != null) {
			String[] tupleStr = line.split("\\s+");
			int[] tuple = new int[tupleStr.length];
			for(int i=0; i<tuple.length; i++) {
				tuple[i] = Integer.parseInt(tupleStr[i]);
			}
			for(Relation relation : relations.getRelationsByName(relationName)) {
				relation.addEdge(gb, tuple);
			}
		}
	}

	@Override
	public TypeFilter readTypeFilter(ContextFreeGrammar contextFreeGrammar) {
		TypeFilter t = new TypeFilter(contextFreeGrammar);
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(this.directory, "ptd.txt")));
			br.readLine();
			String line;
			while((line = br.readLine()) != null) {
				String[] tupleStr = line.split("\\s+");
				t.add("H" + tupleStr[1], "V" + tupleStr[0]);
			}
			br.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return t;
	}		
}
