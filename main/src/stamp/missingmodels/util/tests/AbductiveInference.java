package stamp.missingmodels.util.tests;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lpsolve.LpSolveException;
import stamp.missingmodels.util.tests.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.tests.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.tests.Graph.Edge;
import stamp.missingmodels.util.tests.LinearProgram.Coefficient;
import stamp.missingmodels.util.tests.LinearProgram.ConstraintType;
import stamp.missingmodels.util.tests.LinearProgram.LinearProgramResult;
import stamp.missingmodels.util.tests.LinearProgram.ObjectiveType;

public class AbductiveInference {
	private LinearProgram<Edge> lp = new LinearProgram<Edge>();
	
	private void addProduction(Edge target, Edge input) {
		this.lp.addConstraint(ConstraintType.LEQ, 0.0, new Coefficient<Edge>(input, 1.0), new Coefficient<Edge>(target, -1.0));
	}
	
	private void addProduction(Edge target, Edge firstInput, Edge secondInput) {
		this.lp.addConstraint(ConstraintType.LEQ, 1.0, new Coefficient<Edge>(firstInput, 1.0), new Coefficient<Edge>(secondInput, 1.0), new Coefficient<Edge>(target, -1.0));
	}
	
	private void setObjective(Collection<Edge> targetEdges) {
		Set<Coefficient<Edge>> coefficients = new HashSet<Coefficient<Edge>>();
		for(Edge edge : targetEdges) {
			if(edge.weight != 0) {
				coefficients.add(new Coefficient(edge, edge.weight));
			}
		}
		this.lp.setObjective(ObjectiveType.MINIMIZE, coefficients);
	}
	
	public Set<Edge> performInference(Graph graph, Collection<Edge> targetEdges) throws LpSolveException {
		this.setObjective(targetEdges);
		for(Edge edge : graph.getEdges()) {
			if(edge.weight > 0) {
				for(UnaryProduction unaryProduction : graph.getContextFreeGrammar().unaryProductionsByTarget.get(edge.label)){
					if(unaryProduction.isInputBackwards) {
						Edge input = (Edge)edge.source.incomingEdgesByLabel[unaryProduction.target].get(graph.new Edge(edge.sink, edge.source, unaryProduction.input));
						if(input != null) {
							this.addProduction(edge, input);
						}						
					} else {
						Edge input = (Edge)edge.source.outgoingEdgesByLabel[unaryProduction.target].get(graph.new Edge(edge.source, edge.sink, unaryProduction.input));
						if(input != null) {
							this.addProduction(edge, input);
						}
					}
				}
				for(BinaryProduction binaryProduction : graph.getContextFreeGrammar().binaryProductionsByTarget.get(edge.label)) {
					if(binaryProduction.isFirstInputBackwards && binaryProduction.isSecondInputBackwards) {
						for(Edge firstInput : ((Map<Edge,Edge>)edge.source.incomingEdgesByLabel[binaryProduction.firstInput]).keySet()) {
							Edge secondInput = (Edge)firstInput.source.incomingEdgesByLabel[binaryProduction.secondInput].get(graph.new Edge(edge.sink, firstInput.source, binaryProduction.secondInput));
							if(secondInput != null) {
								this.addProduction(edge, firstInput, secondInput);
							}
						}
					} else if(binaryProduction.isFirstInputBackwards) {
						for(Edge firstInput : ((Map<Edge,Edge>)edge.source.incomingEdgesByLabel[binaryProduction.firstInput]).keySet()) {
							Edge secondInput = (Edge)firstInput.source.outgoingEdgesByLabel[binaryProduction.secondInput].get(graph.new Edge(firstInput.source, edge.sink, binaryProduction.secondInput));
							if(secondInput != null) {
								this.addProduction(edge, firstInput, secondInput);
							}
						}
					} else if(binaryProduction.isSecondInputBackwards) {
						for(Edge firstInput : ((Map<Edge,Edge>)edge.source.outgoingEdgesByLabel[binaryProduction.firstInput]).keySet()) {
							Edge secondInput = (Edge)firstInput.sink.incomingEdgesByLabel[binaryProduction.secondInput].get(graph.new Edge(edge.sink, firstInput.sink, binaryProduction.secondInput));
							if(secondInput != null) {
								this.addProduction(edge, firstInput, secondInput);
							}
						}
					} else {
						for(Edge firstInput : ((Map<Edge,Edge>)edge.source.outgoingEdgesByLabel[binaryProduction.firstInput]).keySet()) {
							Edge secondInput = (Edge)firstInput.sink.outgoingEdgesByLabel[binaryProduction.secondInput].get(graph.new Edge(firstInput.sink, edge.sink, binaryProduction.secondInput));
							if(secondInput != null) {
								this.addProduction(edge, firstInput, secondInput);
							}
						}
					}
				}
			}
		}
		LinearProgramResult solution = this.lp.solve();
		Set<Edge> result = new HashSet<Edge>();
		for(Edge edge : (Set<Edge>)solution.variableValues.keySet()) {
			if((double)solution.variableValues.get(edge) < 0.5) {
				result.add(edge);
			}
		}
		return result;
	}
}
