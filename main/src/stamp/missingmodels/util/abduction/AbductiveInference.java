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
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;
import stamp.missingmodels.util.cflsolver.graph.Graph.Vertex;

public class AbductiveInference {
	private final LinearProgram<CutEdge> lp = new LinearProgram<CutEdge>();
	private final Set<Edge> cutEdges = new HashSet<Edge>();
	private final LinkedList<Edge> worklist = new LinkedList<Edge>();
	
	private final ContextFreeGrammar contextFreeGrammar;
	private final Collection<Edge> baseEdges;
	private final Collection<Edge> initialCutEdges;
	private final int numCuts;
	
	private static class CutEdge {
		private final Edge edge;
		private final int cutNum;
		
		private CutEdge(Edge edge, int cutNum) {
			this.edge = edge;
			this.cutNum = cutNum;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + cutNum;
			result = prime * result + ((edge == null) ? 0 : edge.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CutEdge other = (CutEdge) obj;
			if (cutNum != other.cutNum)
				return false;
			if (edge == null) {
				if (other.edge != null)
					return false;
			} else if (!edge.equals(other.edge))
				return false;
			return true;
		}
	}
	
	public AbductiveInference(Graph graph, Collection<Edge> baseEdges, Collection<Edge> initialCutEdges, int numCuts) {
		this.contextFreeGrammar = graph.getContextFreeGrammar();
		this.baseEdges = baseEdges;
		this.initialCutEdges = initialCutEdges;
		this.numCuts = numCuts;
	}

	public AbductiveInference(Graph graph, Collection<Edge> baseEdges, Collection<Edge> initialCutEdges) {
		this(graph, baseEdges, initialCutEdges, 1);
	}
	
	private void setObjective() {
		Set<Coefficient<CutEdge>> coefficients = new HashSet<Coefficient<CutEdge>>();
		for(Edge edge : this.baseEdges) {
			if(!this.cutEdges.contains(edge)) {
				continue;
			}
			if(edge.getInfo().weight == 0) {
				continue;
			}
			for(int i=0; i<this.numCuts; i++) {
				coefficients.add(new Coefficient<CutEdge>(new CutEdge(edge, i), edge.getInfo().weight));
			}
		}
		this.lp.setObjective(ObjectiveType.MINIMIZE, coefficients);
	}
	
	private void setBaseEdgeExclusivity() {
		if(this.numCuts <= 1) {
			return;
		}
		for(Edge edge : this.baseEdges) {
			Set<Coefficient<CutEdge>> coefficients = new HashSet<Coefficient<CutEdge>>();
			for(int i=0; i<this.numCuts; i++) {
				coefficients.add(new Coefficient<CutEdge>(new CutEdge(edge, i), edge.getInfo().weight));
			}
			this.lp.addConstraint(ConstraintType.LEQ, 1.0, coefficients);
		}
	}
	
	private void setInitialCutEdges() {
		for(Edge edge : this.initialCutEdges) {
			for(int i=0; i<this.numCuts; i++) {
				this.lp.addConstraint(ConstraintType.GEQ, 1.0, new Coefficient<CutEdge>(new CutEdge(edge, i), 1.0));
			}
		}
	}
	
	private void addCutEdge(Edge edge) {
		if(!this.cutEdges.contains(edge)) {
			this.cutEdges.add(edge);
			this.worklist.add(edge);
		}
	}

	private void addProduction(Edge target, Edge input) {
		for(int i=0; i<this.numCuts; i++) {
			this.lp.addConstraint(ConstraintType.LEQ, 0.0, new Coefficient<CutEdge>(new CutEdge(target, i), 1.0), new Coefficient<CutEdge>(new CutEdge(input, i), -1.0));
		}
		this.addCutEdge(input);
	}

	private void addProduction(Edge target, Edge firstInput, Edge secondInput) {
		for(int i=0; i<this.numCuts; i++) {
			this.lp.addConstraint(ConstraintType.LEQ, 0.0, new Coefficient<CutEdge>(new CutEdge(target, i), 1.0), new Coefficient<CutEdge>(new CutEdge(firstInput, i), -1.0), new Coefficient<CutEdge>(new CutEdge(secondInput, i), -1.0));
		}
		this.addCutEdge(firstInput);
		this.addCutEdge(secondInput);
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

	private void cutEdge(Edge edge) {
		if(this.baseEdges.contains(edge)) {
			return;
		}
		
		for(UnaryProduction unaryProduction : this.contextFreeGrammar.unaryProductionsByTarget.get(edge.symbolInt)) {
			this.processProduction(unaryProduction, edge);
		}
		for(BinaryProduction binaryProduction : this.contextFreeGrammar.binaryProductionsByTarget.get(edge.symbolInt)) {
			this.processProduction(binaryProduction, edge);
		}
	}
	
	// returns 1 if the edge is in the cut, 0 if the edge is not in the cut
	private Map<EdgeStruct,Integer> result = null;
	public Map<EdgeStruct,Integer> solve() throws LpSolveException {
		if(this.result == null) {
			// STEP 1: Break initial edges
			this.setInitialCutEdges();
			
			// STEP 2: Cut edges
			for(Edge edge : this.initialCutEdges) {
				this.addCutEdge(edge);
			}
			while(!this.worklist.isEmpty()) {
				this.cutEdge(this.worklist.removeFirst());
			}
			
			// STEP 3: Set objective
			this.setObjective();
			
			// STEP 4: Make cuts on base edges exclusive
			this.setBaseEdgeExclusivity();
			
			// STEP 5: Solve the linear program
			LinearProgramResult<CutEdge> solution = this.lp.solve();
			
			// STEP 6: Set up the result
			this.result = new HashMap<EdgeStruct,Integer>();
			for(Edge edge : this.baseEdges) {
				this.result.put(edge.getStruct(), -1);
			}
			for(CutEdge edge : solution.variableValues.keySet()) {
				//System.out.println(edge + ": " + solution.variableValues.get(edge));
				if(this.baseEdges.contains(edge.edge) && solution.variableValues.get(edge) > 0.0) {
					this.result.put(edge.edge.getStruct(), edge.cutNum);
				}
			}
		}
		
		return this.result;
	}
}
