package stamp.missingmodels.util.temp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lpsolve.LpSolveException;
import stamp.missingmodels.util.abduction.LinearProgram;
import stamp.missingmodels.util.abduction.LinearProgram.Coefficient;
import stamp.missingmodels.util.abduction.LinearProgram.ConstraintType;
import stamp.missingmodels.util.abduction.LinearProgram.LinearProgramResult;
import stamp.missingmodels.util.abduction.LinearProgram.ObjectiveType;
import stamp.missingmodels.util.temp.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.temp.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.temp.Graph.Edge;

public class AbductiveInference {
	private LinearProgram<Edge> lp = new LinearProgram<Edge>();
	private Set<Edge> constrainedEdges = new HashSet<Edge>();
	
	private void addConstrainedEdge(Edge edge) {
		this.constrainedEdges.add(edge);
	}
	
	private void addConstrainedEdgeConstraints() {
		for(Edge edge : this.constrainedEdges) {
			this.lp.addConstraint(ConstraintType.LEQ, 1.0, new Coefficient<Edge>(edge, 1.0));
		}
	}
	
	private void addProduction(Edge target, Edge input) {
		this.addConstrainedEdge(target);
		this.addConstrainedEdge(input);
		this.lp.addConstraint(ConstraintType.LEQ, 0.0, new Coefficient<Edge>(input, 1.0), new Coefficient<Edge>(target, -1.0));
	}
	
	private void addProduction(Edge target, Edge firstInput, Edge secondInput) {
		this.addConstrainedEdge(target);
		this.addConstrainedEdge(firstInput);
		this.addConstrainedEdge(secondInput);
		this.lp.addConstraint(ConstraintType.LEQ, 1.0, new Coefficient<Edge>(firstInput, 1.0), new Coefficient<Edge>(secondInput, 1.0), new Coefficient<Edge>(target, -1.0));
	}
	
	private void setRemovedEdges(Collection<Edge> removedEdges) {
		for(Edge edge : removedEdges) {
			this.addConstrainedEdge(edge);
			this.lp.addConstraint(ConstraintType.LEQ, 0.0, new Coefficient<Edge>(edge, 1.0));
		}
	}
	
	private void setObjective(Collection<Edge> cutEdges) {
		Set<Coefficient<Edge>> coefficients = new HashSet<Coefficient<Edge>>();
		for(Edge edge : cutEdges) {
			if(edge.weight != 0) {
				this.addConstrainedEdge(edge);
				coefficients.add(new Coefficient<Edge>(edge, edge.weight));
			}
		}
		this.lp.setObjective(ObjectiveType.MAXIMIZE, coefficients);
	}
	
	public void removeEdge(Graph graph, Edge edge) {
	}
	
	public void performInferenceNew(Graph graph, Collection<Edge> cutEdges, Collection<Edge> removedEdges) throws LpSolveException {
		for(Edge edge : removedEdges) {
			this.removeEdge(graph, edge);
		}
	}
	
	public Set<Edge> performInference(Graph graph, Collection<Edge> cutEdges, Collection<Edge> removedEdges) throws LpSolveException {
		this.setObjective(cutEdges);
		this.setRemovedEdges(removedEdges);
		for(Edge edge : graph.getEdges()) {
			if(edge.weight > 0) {
				for(UnaryProduction unaryProduction : graph.getContextFreeGrammar().unaryProductionsByTarget.get(edge.label)) {
					if(unaryProduction.isInputBackwards) {
						for(Edge input : ((Map<Edge,Edge>)edge.source.incomingEdgesByLabel[unaryProduction.input]).keySet()) {
							if(input.source.equals(edge.sink) && ReachabilitySolver.getField(input.field, unaryProduction.ignoreFields) == edge.field) {
								this.addProduction(edge, input);
							}
						}
					} else {
						for(Edge input : ((Map<Edge,Edge>)edge.source.outgoingEdgesByLabel[unaryProduction.input]).keySet()) {
							if(input.sink.equals(edge.sink) && ReachabilitySolver.getField(input.field, unaryProduction.ignoreFields) == edge.field) {
								this.addProduction(edge, input);
							}
						}
					}
				}
				for(BinaryProduction binaryProduction : graph.getContextFreeGrammar().binaryProductionsByTarget.get(edge.label)) {
					if(binaryProduction.isFirstInputBackwards && binaryProduction.isSecondInputBackwards) {
						for(Edge firstInput : ((Map<Edge,Edge>)edge.source.incomingEdgesByLabel[binaryProduction.firstInput]).keySet()) {
							for(Edge secondInput : ((Map<Edge,Edge>)firstInput.source.incomingEdgesByLabel[binaryProduction.secondInput]).keySet()) {
								if(secondInput.source.equals(edge.sink) && ReachabilitySolver.getField(firstInput.field, secondInput.field, binaryProduction.ignoreFields) == edge.field) {
									this.addProduction(edge, firstInput, secondInput);
								}
							}
						}
					} else if(binaryProduction.isFirstInputBackwards) {
						for(Edge firstInput : ((Map<Edge,Edge>)edge.source.incomingEdgesByLabel[binaryProduction.firstInput]).keySet()) {
							for(Edge secondInput : ((Map<Edge,Edge>)firstInput.source.outgoingEdgesByLabel[binaryProduction.secondInput]).keySet()) {
								if(secondInput.sink.equals(edge.sink) && ReachabilitySolver.getField(firstInput.field, secondInput.field, binaryProduction.ignoreFields) == edge.field) {
									this.addProduction(edge, firstInput, secondInput);
								}
							}
						}
					} else if(binaryProduction.isSecondInputBackwards) {
						for(Edge firstInput : ((Map<Edge,Edge>)edge.source.outgoingEdgesByLabel[binaryProduction.firstInput]).keySet()) {
							for(Edge secondInput : ((Map<Edge,Edge>)firstInput.sink.incomingEdgesByLabel[binaryProduction.secondInput]).keySet()) {
								if(secondInput.source.equals(edge.sink) && ReachabilitySolver.getField(firstInput.field, secondInput.field, binaryProduction.ignoreFields) == edge.field) {
									this.addProduction(edge, firstInput, secondInput);
								}
							}
						}
					} else {
						for(Edge firstInput : ((Map<Edge,Edge>)edge.source.outgoingEdgesByLabel[binaryProduction.firstInput]).keySet()) {
							for(Edge secondInput : ((Map<Edge,Edge>)firstInput.sink.outgoingEdgesByLabel[binaryProduction.secondInput]).keySet()) {
								if(secondInput.sink.equals(edge.sink) && ReachabilitySolver.getField(firstInput.field, secondInput.field, binaryProduction.ignoreFields) == edge.field) {
									this.addProduction(edge, firstInput, secondInput);
								}
							}
						}
					}
				}
			}
		}
		this.addConstrainedEdgeConstraints();
		LinearProgramResult solution = this.lp.solve();
		Set<Edge> result = new HashSet<Edge>();
		for(Edge edge : (Set<Edge>)solution.variableValues.keySet()) {
			//System.out.println("val: " + (double)solution.variableValues.get(edge));
			if((double)solution.variableValues.get(edge) < 0.5) {
				result.add(edge);
			}
		}
		return result;
	}
}
