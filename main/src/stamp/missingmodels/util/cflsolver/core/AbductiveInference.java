package stamp.missingmodels.util.cflsolver.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.LinearProgram.Coefficient;
import stamp.missingmodels.util.cflsolver.core.LinearProgram.ConstraintType;
import stamp.missingmodels.util.cflsolver.core.LinearProgram.LinearProgramResult;
import stamp.missingmodels.util.cflsolver.core.LinearProgram.ObjectiveType;
import stamp.missingmodels.util.cflsolver.core.Util.AndFilter;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;

public class AbductiveInference extends ProductionIterator<Map<EdgeStruct,Boolean>> {
	private LinearProgram<Edge> lp;

	public AbductiveInference(ContextFreeGrammarOpt contextFreeGrammar) {
		super(contextFreeGrammar);
	}

	private void setObjective(Set<Edge> baseEdges, Set<Edge> edges) {
		Collection<Coefficient<Edge>> coefficients = new HashSet<Coefficient<Edge>>();
		for(Edge edge : baseEdges) {
			if(!edges.contains(edge)) {
				continue;
			}
			if(edge.weight == (short)0) {
				continue;
			}
			coefficients.add(new Coefficient<Edge>(edge, edge.weight));
		}
		this.lp.setObjective(ObjectiveType.MINIMIZE, coefficients);
	}

	private void setInitialEdges(Set<Edge> initialEdges) {
		for(Edge edge : initialEdges) {
			this.lp.addConstraint(ConstraintType.GEQ, 1.0, new Coefficient<Edge>(edge, 1.0));
		}
	}

	@Override
	protected void addProduction(Edge target, Edge input) {
		this.lp.addConstraint(ConstraintType.LEQ, 0.0, new Coefficient<Edge>(target, 1.0), new Coefficient<Edge>(input, -1.0));
	}

	@Override
	protected void addProduction(Edge target, Edge firstInput, Edge secondInput) {
		this.lp.addConstraint(ConstraintType.LEQ, 0.0, new Coefficient<Edge>(target, 1.0), new Coefficient<Edge>(firstInput, -1.0), new Coefficient<Edge>(secondInput, -1.0));
	}
	
	@Override
	protected void preprocess(Set<Edge> baseEdges, Set<Edge> initialEdges) {
		// STEP 0: Setup
		this.lp = new SCIPLinearProgram<Edge>();
		
		// STEP 1: Break initial edges
		this.setInitialEdges(initialEdges);
	}
	
	@Override
	protected Map<EdgeStruct,Boolean> postprocess(Set<Edge> baseEdges, Set<Edge> initialEdges, Set<Edge> edges) {
		// STEP 1: Set objective (order important!)
		this.setObjective(baseEdges, edges);

		// STEP 2: Solve the linear program
		LinearProgramResult<Edge> solution = this.lp.solve();
		
		// STEP 3: Set up the result
		// returns 1 if the edge is in the cut, 0 if the edge is not in the cut
		Map<EdgeStruct,Boolean> result = new HashMap<EdgeStruct,Boolean>();
		for(Edge edge : solution.keySet()) {
			if(baseEdges.contains(edge)) {
				result.put(edge.getStruct(), solution.get(edge) > 0.5);
			}
		}

		return result;
	}

	public MultivalueMap<EdgeStruct,Integer> process(Filter<EdgeStruct> baseEdgeFilter, Filter<EdgeStruct> initialEdgeFilter, Graph graph, Filter<Edge> filter, int numCuts) {
		Filter<EdgeStruct> baseEdgeFilterCur = baseEdgeFilter;
		MultivalueMap<EdgeStruct,Integer> allResults = new MultivalueMap<EdgeStruct,Integer>();
		for(int i=0; i<numCuts; i++) {			
			// STEP 1: Run the abductive inference algorithm
			final Map<EdgeStruct,Boolean> result = this.process(baseEdgeFilterCur, initialEdgeFilter, graph, filter);
			for(EdgeStruct edge : result.keySet()) {
				if(result.get(edge)) {
					allResults.add(edge, i);
				}
			}
			
			// STEP 2: Update the filter to exclude new edges
			baseEdgeFilterCur = new AndFilter<EdgeStruct>(baseEdgeFilterCur, new Filter<EdgeStruct>() {
				@Override
				public boolean filter(EdgeStruct edge) {
					return !result.containsKey(edge) || !result.get(edge);
				}
			});
		}		
		return allResults;
	}
}

