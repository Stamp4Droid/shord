package stamp.missingmodels.util.temp;

import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.GraphBuilder;

public abstract class RelationReader {
	public abstract void addEdge(String[] tuple, GraphBuilder gb);

	/*
	public static RelationReader getReaderByName(String name) {
		switch(name) { 
		case "Alloc":
			return allocRelationReader;
		case "Assign":
			return assignRelationReader;
		case "Load":
			return loadRelationReader;
		case "param":
			return paramRelationReader;
		case "return":
			return returnRelationReader;
		case "Store":
			return storeRelationReader;
		}
		throw new RuntimeException("Invalid relation!");
	}
	*/
	
	public static RelationReader getReaderByName(String name) {
		return null;
	}
	
	public static RelationReader allocRelationReader = new RelationReader() {
		@Override
		public void addEdge(String[] tuple, GraphBuilder gb) {
			gb.addEdge("H" + tuple[1], "V" + tuple[0], "alloc");
		}
	};
	
	public static RelationReader assignRelationReader = new RelationReader() {
		@Override
		public void addEdge(String[] tuple, GraphBuilder gb) {
			gb.addEdge("V" + tuple[1], "V" + tuple[0], "assign");
		}
	};
	
	public static RelationReader loadRelationReader = new RelationReader() {
		@Override
		public void addEdge(String[] tuple, GraphBuilder gb) {
			gb.addEdge("V" + tuple[1], "V" + tuple[0], "load", new Field(Integer.parseInt(tuple[2])));
		}
	};
	
	public static RelationReader paramRelationReader = new RelationReader() {
		@Override
		public void addEdge(String[] tuple, GraphBuilder gb) {
			gb.addEdge("V" + tuple[1], "V" + tuple[0], "param", new Context(Integer.parseInt(tuple[2]), true), new EdgeInfo(1));
		}
	};
	
	public static RelationReader returnRelationReader = new RelationReader() {
		@Override
		public void addEdge(String[] tuple, GraphBuilder gb) {
			gb.addEdge("V" + tuple[1], "V" + tuple[0], "return", new Context(Integer.parseInt(tuple[2]), false), new EdgeInfo(1));
		}
	};
	
	public static RelationReader storeRelationReader = new RelationReader() {
		@Override
		public void addEdge(String[] tuple, GraphBuilder gb) {
			gb.addEdge("V" + tuple[2], "V" + tuple[0], "store", new Field(Integer.parseInt(tuple[1])));
		}
	};
}
