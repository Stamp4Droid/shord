package stamp.missingmodels.util.cflsolver.core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.AuxProduction;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Edge.Field;
import stamp.missingmodels.util.cflsolver.core.Graph.EdgeTransformer;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.core.Graph.TransformFilter;
import stamp.missingmodels.util.cflsolver.core.Util.AndFilter;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;

public abstract class ProductionIterator<Result> {
	private final ContextFreeGrammarOpt contextFreeGrammar;

	private Set<Edge> baseEdges;
	private Set<Edge> initialEdges;
	private HashSet<Edge> edges;
	private LinkedList<Edge> worklist;

	protected ProductionIterator(ContextFreeGrammarOpt contextFreeGrammar) {
		this.contextFreeGrammar = contextFreeGrammar;
	}
	
	protected abstract void addProduction(Edge target, Edge input);
	protected abstract void addProduction(Edge target, Edge firstIput, Edge secondInput);
	protected abstract void preprocess(Set<Edge> baseEdges, Set<Edge> initialEdges);
	protected abstract Result postprocess(Set<Edge> baseEdges, Set<Edge> initialEdges, Set<Edge> edges);

	private void addEdge(Edge edge) {
		if(!this.edges.contains(edge)) {
			this.edges.add(edge);
			this.worklist.add(edge);
		}
	}

	private void addProductionHelper(Edge target, Edge input) {
		this.addProduction(target, input);
		this.addEdge(input);
		
	}

	private void addProductionHelper(Edge target, Edge firstInput, Edge secondInput) {
		this.addProduction(target, firstInput, secondInput);
		this.addEdge(firstInput);
		this.addEdge(secondInput);
	}

	private void processProduction(UnaryProduction unaryProduction, Edge target) {
		// iterate over potential inputs
		Iterable<Edge> potentialInputs = unaryProduction.isInputBackwards ? target.source.getIncomingEdges(unaryProduction.input.id) : target.source.getOutgoingEdges(unaryProduction.input.id);
		for(Edge input : potentialInputs) {
			// only add production if the sink is correct
			Vertex toCheck = unaryProduction.isInputBackwards ? input.source : input.sink;
			if(!target.sink.equals(toCheck)) {
				continue;
			}
			// only add production if input weight is positive
			if(input.weight == (short)0) {
				continue;
			}
			// only add production if edge data are equal
			if(Field.produce(unaryProduction.ignoreFields, input.field).field != target.field.field) {
				continue;
			}
			// add edge
			this.addProductionHelper(target, input);
		}
	}

	private void processProduction(BinaryProduction binaryProduction, Edge target) {
		// iterate over potential first inputs
		Iterable<Edge> potentialFirstInputs = binaryProduction.isFirstInputBackwards ? target.source.getIncomingEdges(binaryProduction.firstInput.id) : target.source.getOutgoingEdges(binaryProduction.firstInput.id);
		for(Edge firstInput : potentialFirstInputs) {
			// iterate over potential second inputs
			Iterable<Edge> potentialSecondInputs = binaryProduction.isSecondInputBackwards ? target.sink.getOutgoingEdges(binaryProduction.secondInput.id) : target.sink.getIncomingEdges(binaryProduction.secondInput.id);
			for(Edge secondInput : potentialSecondInputs) {
				// only add production if the intermediate is correct
				Vertex firstIntermediate = binaryProduction.isFirstInputBackwards ? firstInput.source : firstInput.sink;
				Vertex secondIntermediate = binaryProduction.isSecondInputBackwards ? secondInput.sink : secondInput.source;
				if(!firstIntermediate.equals(secondIntermediate)) {
					continue;
				}
				// only add production for inputs with positive weight
				boolean includeFirstInput = firstInput.weight > (short)0;
				boolean includeSecondInput = secondInput.weight > (short)0;
				// only add production if edge data are equal
				Field field = Field.produce(binaryProduction.ignoreFields, firstInput.field, secondInput.field);
				if(field == null || field.field != target.field.field) {
					continue;
				}
				// add edge
				if(includeFirstInput && includeSecondInput) {
					this.addProductionHelper(target, firstInput, secondInput);
				} else if(includeFirstInput) {
					this.addProductionHelper(target, firstInput);
				} else if(includeSecondInput) {
					this.addProductionHelper(target, secondInput);
				}
			}
		}
	}

	private void processProduction(AuxProduction auxProduction, Edge target) {
		// iterate over potential inputs
		Iterable<Edge> potentialInputs = auxProduction.isInputBackwards ? target.source.getIncomingEdges(auxProduction.input.id) : target.source.getOutgoingEdges(auxProduction.input.id);
		for(Edge input : potentialInputs) {
			// only add production if intermediate matches
			Vertex toCheck = auxProduction.isInputBackwards ? input.source : input.sink;
			if(!target.sink.equals(toCheck)) {
				continue;
			}
			// iterate over potential aux inputs
			Vertex auxIntermediate = auxProduction.isAuxInputFirst ? target.source : target.sink;
			Iterable<Edge> potentialAuxInputs = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? auxIntermediate.getOutgoingEdges(auxProduction.auxInput.id) : auxIntermediate.getIncomingEdges(auxProduction.auxInput.id);
			for(Edge auxInput : potentialAuxInputs) {
				// only add production for inputs with positive weight
				boolean includeInput = input.weight > (short)0;
				boolean includeAuxInput = auxInput.weight > (short)0;
				// only add production if edge data are equal
				if(Field.produce(auxProduction.ignoreFields, input.field, auxInput.field).field != target.field.field) {
					continue;
				}
				// add edge
				if(includeInput && includeAuxInput) {
					this.addProductionHelper(target, input, auxInput);
				} else if(includeInput) {
					this.addProductionHelper(target, input);
				} else if(includeAuxInput) {
					this.addProductionHelper(target, auxInput);
				}
			}
		}
	}

	private void processEdge(Edge edge) {
		if(this.baseEdges.contains(edge)) {
			return;
		}

		for(UnaryProduction unaryProduction : this.contextFreeGrammar.unaryProductionsByTarget[edge.symbol.id]) {
			this.processProduction(unaryProduction, edge);
		}
		for(BinaryProduction binaryProduction : this.contextFreeGrammar.binaryProductionsByTarget[edge.symbol.id]) {
			this.processProduction(binaryProduction, edge);
		}
		for(AuxProduction auxProduction : this.contextFreeGrammar.auxProductionsByTarget[edge.symbol.id]) {
			this.processProduction(auxProduction, edge);
		}
	}
	
	public Result process(final Filter<EdgeStruct> baseEdgeFilter, Filter<EdgeStruct> initialEdgeFilter, Graph graph, Filter<Edge> filter) {
		// STEP 0: Setup
		this.edges = new HashSet<Edge>();
		this.worklist = new LinkedList<Edge>();

		// STEP 1: Compute transitive closure
		Graph weightedGraph = graph.transform(new EdgeTransformer(graph.getVertices(), graph.getSymbols()) {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edge) {
				gb.addOrUpdateEdge(new EdgeStruct(edge.sourceName, edge.sinkName, edge.symbol, edge.field, baseEdgeFilter.filter(edge) ? (short)1 : (short)0));
			}});
		Graph graphBar = weightedGraph.transform(new ReachabilitySolver(weightedGraph.getVertices(), this.contextFreeGrammar, filter));
		
		// STEP 2: Get base and initial edges
		this.baseEdges = new HashSet<Edge>();
		for(Edge edge : graphBar.getEdges(new TransformFilter<Edge,EdgeStruct>(baseEdgeFilter) {
			@Override
			public EdgeStruct transform(Edge x) {
				return x.getStruct(); }})) {
			this.baseEdges.add(edge);
		}
		System.out.println("Num base edges: " + this.baseEdges.size());
		Filter<EdgeStruct> initialEdgeFilterWeighted = new AndFilter<EdgeStruct>(initialEdgeFilter, new Filter<EdgeStruct>() {
			@Override
			public boolean filter(EdgeStruct edge) {
				return edge.weight > (short)0;
			}});
		this.initialEdges = new HashSet<Edge>();
		for(Edge edge : graphBar.getEdges(new TransformFilter<Edge,EdgeStruct>(initialEdgeFilterWeighted) {
			@Override
			public EdgeStruct transform(Edge x) {
				return x.getStruct(); }})) {
			this.initialEdges.add(edge);
		}
		System.out.println("Num initial edges: " + this.initialEdges.size());
		
		// STEP 3: Perform any preprocessing needed (base edges and initial edges are finalized here)
		this.preprocess(this.baseEdges, this.initialEdges);
		
		// STEP 4: Cut edges
		for(Edge edge : this.initialEdges) {
			this.addEdge(edge);
		}
		while(!this.worklist.isEmpty()) {
			this.processEdge(this.worklist.removeFirst());
		}
		
		// STEP 5: Return result
		return this.postprocess(this.baseEdges, this.initialEdges, this.edges);
	}
}

