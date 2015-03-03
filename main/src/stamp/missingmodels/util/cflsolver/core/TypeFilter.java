package stamp.missingmodels.util.cflsolver.core;

import java.util.HashSet;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.Symbol;

public interface TypeFilter {
	public boolean filter(Symbol symbol, Vertex source, Vertex sink);
	
	public static class GraphTypeFilter implements TypeFilter {
		private final Set<Integer> filter = new HashSet<Integer>();
		private final int flowSymbolId;
		private final int numVertices;
		
		public GraphTypeFilter(Graph g) {
			this.flowSymbolId = g.getContextFreeGrammarOpt().getSymbols().get("Flow").id;
			this.numVertices = g.getNumVertices();
			
			final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt("ptd");
			rel.load();
			
			Iterable<int[]> res = rel.getAryNIntTuples();
			for(int[] tuple : res) {
				String sourceName = "H" + Integer.toString(tuple[1]);
				String sinkName = "V" + Integer.toString(tuple[0]);
				if(g.containsVertex(sourceName) && g.containsVertex(sinkName)) {
					int sourceId = g.getVertex(sourceName).id;
					int sinkId = g.getVertex(sinkName).id;
					this.filter.add(this.numVertices*sinkId + sourceId);
				}
			}
			
			rel.close();
		}

		@Override
		public boolean filter(Symbol symbol, Vertex source, Vertex sink) {
			return symbol.id != this.flowSymbolId || this.filter.contains(this.numVertices*sink.id + source.id);
		}
	}
}
