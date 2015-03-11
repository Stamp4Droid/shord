package stamp.missingmodels.util.cflsolver.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.SymbolMap;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Edge.Field;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.Filter;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphEdgeFilter;
import stamp.missingmodels.util.cflsolver.core.Graph.SimpleGraphBuilder;
import stamp.missingmodels.util.cflsolver.core.Graph.VertexMap;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.Relation;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;

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
	public Filter<Edge> readFilter(VertexMap vertices, SymbolMap symbols) {
		try {
			List<EdgeStruct> edges = new ArrayList<EdgeStruct>();
			
			BufferedReader brPt = new BufferedReader(new FileReader(new File(this.directory, "ptd.txt")));
			brPt.readLine();
			String line;
			while((line = brPt.readLine()) != null) {
				String[] tupleStr = line.split("\\s+");
				edges.add(new EdgeStruct("H" + tupleStr[1], "V" + tupleStr[0], "Flow", Field.DEFAULT_FIELD.field, (short)0));
			}
			brPt.close();

			BufferedReader brPrim = new BufferedReader(new FileReader(new File(this.directory, "Lable2Prim.txt")));
			while((line = brPrim.readLine()) != null) {
				String[] tupleStr = line.split("\\s+");
				edges.add(new EdgeStruct("L" + tupleStr[1], "U" + tupleStr[2], "Label2Prim", Field.DEFAULT_FIELD.field, (short)0));
			}
			brPrim.close();
			
			return new GraphEdgeFilter(vertices, symbols, edges);
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("File for relation ptd not found!");
		}
	}		
}
