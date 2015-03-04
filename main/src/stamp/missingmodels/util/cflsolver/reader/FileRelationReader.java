package stamp.missingmodels.util.cflsolver.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.SymbolMap;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.SimpleGraphBuilder;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.Relation;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.TypeFilter;

public class FileRelationReader implements RelationReader {
	private final File directory;
	
	public FileRelationReader(File directory) {
		this.directory = directory;
	}

	@Override
	public Graph readGraph(RelationManager relations, SymbolMap symbols) {
		SimpleGraphBuilder gb = new SimpleGraphBuilder(symbols);
		for(File relationFile : this.directory.listFiles()) {
			try {
				String relationName = relationFile.getName().split("\\.")[0];
				BufferedReader br = new BufferedReader(new FileReader(relationFile));
				readRelation(relations, br, gb, relationName);
			} catch(Exception e) {}
		}
		return gb.getGraph();
	}
	
	private static void readRelation(RelationManager relations, BufferedReader br, SimpleGraphBuilder gb, String relationName) throws IOException {
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
	public TypeFilter readTypeFilter(SymbolMap symbols) {
		/*
		TypeFilter t = new TypeFilter(contextFreeGrammar.getOpt());
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
		*/
		return null;
	}		
}
