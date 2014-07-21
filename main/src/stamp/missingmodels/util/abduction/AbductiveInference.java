package stamp.missingmodels.util.abduction;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import lpsolve.LpSolveException;
import stamp.missingmodels.util.abduction.LinearProgram.Coefficient;
import stamp.missingmodels.util.abduction.LinearProgram.ConstraintType;
import stamp.missingmodels.util.abduction.LinearProgram.LinearProgramResult;
import stamp.missingmodels.util.abduction.LinearProgram.ObjectiveType;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.AuxProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;
import stamp.missingmodels.util.cflsolver.graph.Graph.Vertex;

public class AbductiveInference {
	private final LinearProgram<Edge> lp = new LinearProgram<Edge>();
	private final Set<Edge> Edges = new HashSet<Edge>();
	private final LinkedList<Edge> worklist = new LinkedList<Edge>();
	
	private final ContextFreeGrammar contextFreeGrammar;
	private final Collection<Edge> baseEdges;
	private final Collection<Edge> initialEdges;
	
	public AbductiveInference(Graph graph, Collection<Edge> baseEdges, Collection<Edge> initialEdges) {
		this.contextFreeGrammar = graph.getContextFreeGrammar();
		this.baseEdges = baseEdges;
		this.initialEdges = initialEdges;
	}
	
	private void setObjective() {
		Set<Coefficient<Edge>> coefficients = new HashSet<Coefficient<Edge>>();
		for(Edge edge : this.baseEdges) {
			if(!this.Edges.contains(edge)) {
				continue;
			}
			if(edge.getInfo().weight == 0) {
				continue;
			}
			coefficients.add(new Coefficient<Edge>(edge, edge.getInfo().weight));
		}
		this.lp.setObjective(ObjectiveType.MINIMIZE, coefficients);
	}
	
	private void setInitialEdges() {
		for(Edge edge : this.initialEdges) {
			this.lp.addConstraint(ConstraintType.GEQ, 1.0, new Coefficient<Edge>(edge, 1.0));
		}
	}
	
	private void addEdge(Edge edge) {
		if(!this.Edges.contains(edge)) {
			this.Edges.add(edge);
			this.worklist.add(edge);
		}
	}

	private void addProduction(Edge target, Edge input) {
		this.lp.addConstraint(ConstraintType.LEQ, 0.0, new Coefficient<Edge>(target, 1.0), new Coefficient<Edge>(input, -1.0));
		this.addEdge(input);
	}

	private void addProduction(Edge target, Edge firstInput, Edge secondInput) {
		this.lp.addConstraint(ConstraintType.LEQ, 0.0, new Coefficient<Edge>(target, 1.0), new Coefficient<Edge>(firstInput, -1.0), new Coefficient<Edge>(secondInput, -1.0));
		this.addEdge(firstInput);
		this.addEdge(secondInput);
	}
	
	private void processProduction(UnaryProduction unaryProduction, Edge target) {
		// iterate over potential inputs
		Collection<Edge> potentialInputs = unaryProduction.isInputBackwards ? target.source.getIncomingEdges(unaryProduction.input) : target.source.getOutgoingEdges(unaryProduction.input);
		for(Edge input : potentialInputs) {
			// only add production if the sink is correct
			Vertex toCheck = unaryProduction.isInputBackwards ? input.source : input.sink;
			if(!target.sink.equals(toCheck)) {
				continue;
			}
			// only add production if input weight is positive
			if(input.getInfo().weight == 0) {
				continue;
			}
			// only add production if edge data are equal
			if(!input.field.produce(unaryProduction).equals(target.field)) {
				continue;
			}
			if(!input.context.produce(unaryProduction).equals(target.context)) {
				continue;
			}
			// add edge
			this.addProduction(target, input);
		}
	}

	private void processProduction(BinaryProduction binaryProduction, Edge target) {
		// iterate over potential first inputs
		Collection<Edge> potentialFirstInputs = binaryProduction.isFirstInputBackwards ? target.source.getIncomingEdges(binaryProduction.firstInput) : target.source.getOutgoingEdges(binaryProduction.firstInput);
		for(Edge firstInput : potentialFirstInputs) {
			// iterate over potential second inputs
			Collection<Edge> potentialSecondInputs = binaryProduction.isSecondInputBackwards ? target.sink.getOutgoingEdges(binaryProduction.secondInput) : target.sink.getIncomingEdges(binaryProduction.secondInput);
			for(Edge secondInput : potentialSecondInputs) {
				// only add production if the intermediate is correct
				Vertex firstIntermediate = binaryProduction.isFirstInputBackwards ? firstInput.source : firstInput.sink;
				Vertex secondIntermediate = binaryProduction.isSecondInputBackwards ? secondInput.sink : secondInput.source;
				if(!firstIntermediate.equals(secondIntermediate)) {
					continue;
				}
				// only add production for inputs with positive weight
				boolean includeFirstInput = firstInput.getInfo().weight > 0;
				boolean includeSecondInput = secondInput.getInfo().weight > 0;
				// only add production if edge data are equal
				if(!target.field.equals(firstInput.field.produce(binaryProduction, secondInput.field))) {
					continue;
				}
				if(!target.context.equals(firstInput.context.produce(binaryProduction, secondInput.context))) {
					continue;
				}
				// add edge
				if(includeFirstInput && includeSecondInput) {
					this.addProduction(target, firstInput, secondInput);
				} else if(includeFirstInput) {
					this.addProduction(target, firstInput);
				} else if(includeSecondInput) {
					this.addProduction(target, secondInput);
				}
			}
		}
	}

	private void processProduction(AuxProduction auxProduction, Edge target) {
		// iterate over potential inputs
		Collection<Edge> potentialInputs = auxProduction.isInputBackwards ? target.source.getIncomingEdges(auxProduction.input) : target.source.getOutgoingEdges(auxProduction.input);
		for(Edge input : potentialInputs) {
			// only add production if intermediate matches
			Vertex toCheck = auxProduction.isInputBackwards ? input.source : input.sink;
			if(!target.sink.equals(toCheck)) {
				continue;
			}
			// iterate over potential aux inputs
			Vertex auxIntermediate = auxProduction.isAuxInputFirst ? target.source : target.sink;
			Collection<Edge> potentialAuxInputs = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? auxIntermediate.getOutgoingEdges(auxProduction.auxInput) : auxIntermediate.getIncomingEdges(auxProduction.auxInput);
			for(Edge auxInput : potentialAuxInputs) {
				// only add production for inputs with positive weight
				boolean includeInput = input.getInfo().weight > 0;
				boolean includeAuxInput = auxInput.getInfo().weight > 0;
				// only add production if edge data are equal
				if(!target.field.equals(input.field.produce(auxProduction, auxInput.field))) {
					continue;
				}
				if(!target.context.equals(input.context.produce(auxProduction, auxInput.context))) {
					continue;
				}
				// add edge
				if(includeInput && includeAuxInput) {
					this.addProduction(target, input, auxInput);
				} else if(includeInput) {
					this.addProduction(target, input);
				} else if(includeAuxInput) {
					this.addProduction(target, auxInput);
				}
			}
		}
	}

	private void processEdge(Edge edge) {
		if(this.baseEdges.contains(edge)) {
			return;
		}
		
		for(UnaryProduction unaryProduction : this.contextFreeGrammar.unaryProductionsByTarget.get(edge.symbolInt)) {
			this.processProduction(unaryProduction, edge);
		}
		for(BinaryProduction binaryProduction : this.contextFreeGrammar.binaryProductionsByTarget.get(edge.symbolInt)) {
			this.processProduction(binaryProduction, edge);
		}
		for(AuxProduction auxProduction : this.contextFreeGrammar.auxProductionsByTarget.get(edge.symbolInt)) {
			this.processProduction(auxProduction, edge);
		}
	}
	
	// returns 1 if the edge is in the cut, 0 if the edge is not in the cut
	private Map<EdgeStruct,Boolean> result = null;
	public Map<EdgeStruct,Boolean> solve() throws LpSolveException {
		if(this.result == null) {
			// STEP 1: Break initial edges
			this.setInitialEdges();
			
			// STEP 2: Cut edges
			for(Edge edge : this.initialEdges) {
				this.addEdge(edge);
			}
			while(!this.worklist.isEmpty()) {
				this.processEdge(this.worklist.removeFirst());
			}
			
			// STEP 3: Set objective
			this.setObjective();
			
			// STEP 4: Solve the linear program
			LinearProgramResult<Edge> solution = this.lp.solve();
			
			// STEP 5: Set up the result
			this.result = new HashMap<EdgeStruct,Boolean>();
			for(Edge edge : solution.variableValues.keySet()) {
				if(this.baseEdges.contains(edge)) {
					//System.out.println(edge.getStruct() + ": " + solution.variableValues.get(edge));
					this.result.put(edge.getStruct(), solution.variableValues.get(edge) > 0.5);
				}
			}
		}
		
		return this.result;
	}
}