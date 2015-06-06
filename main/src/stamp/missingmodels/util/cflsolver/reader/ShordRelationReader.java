package stamp.missingmodels.util.cflsolver.reader;

import java.util.ArrayList;
import java.util.List;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.SymbolMap;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.Relation;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;

public class ShordRelationReader implements RelationReader {
	@Override
	public Iterable<EdgeStruct> readGraph(RelationManager relations, SymbolMap symbols) {
		List<EdgeStruct> edges = new ArrayList<EdgeStruct>();
		for(int i=0; i<symbols.getNumSymbols(); i++) {
			String symbol = symbols.get(i).symbol;
			for(Relation relation : relations.getRelationsBySymbol(symbol)) {
				final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relation.getName());
				rel.load();
				for(int[] tuple : rel.getAryNIntTuples()) {
					for(EdgeStruct edge : RelationManager.readRelation(relation, tuple)) {
						edges.add(edge);
					}
				}
				rel.close();
			}
		}
		return edges;
	}
}