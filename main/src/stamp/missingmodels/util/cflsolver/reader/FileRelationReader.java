package stamp.missingmodels.util.cflsolver.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.SymbolMap;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.Relation;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;

public class FileRelationReader implements RelationReader {
	private final File directory;
	
	public FileRelationReader(File directory) {
		this.directory = directory;
	}

	@Override
	public Iterable<EdgeStruct> readGraph(RelationManager relations, SymbolMap symbols) {
		Set<EdgeStruct> edges = new HashSet<EdgeStruct>();
		for(File relationFile : this.directory.listFiles()) {
			try {
				String relationName = relationFile.getName().split("\\.")[0];
				BufferedReader br = new BufferedReader(new FileReader(relationFile));
				br.readLine();
				String line;
				while((line = br.readLine()) != null) {
					String[] tupleStr = line.split("\\s+");
					int[] tuple = new int[tupleStr.length];
					for(int i=0; i<tuple.length; i++) {
						tuple[i] = Integer.parseInt(tupleStr[i]);
					}
					for(Relation relation : relations.getRelationsByName(relationName)) {
						for(EdgeStruct edge : RelationManager.readRelation(relation, tuple)) {
							edges.add(edge);
						}
					}
				}
				br.close();
			} catch(Exception e) {}
		}
		return edges;
	}
}
