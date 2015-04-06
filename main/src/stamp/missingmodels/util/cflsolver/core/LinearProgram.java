package stamp.missingmodels.util.cflsolver.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lpsolve.LpSolve;

public abstract class LinearProgram<T> {
	public static class Coefficient<T> {
		public final T variable;
		public final double coefficient;

		public Coefficient(T variable, double coefficient) {
			this.variable = variable;
			this.coefficient = coefficient;
		}
		
		@Override
		public String toString() {
			return this.coefficient + "*(" + this.variable.toString() + ")";
		}
	}

	public static enum ObjectiveType {
		MAXIMIZE, MINIMIZE;
		
		@Override
		public String toString() {
			switch(this) {
			case MAXIMIZE:
				return "MAX";
			case MINIMIZE:
				return "MIN";
			default:
				throw new RuntimeException("Unrecognized objective type!");
			}
		}
	}

	public static enum ConstraintType {
		GEQ, LEQ, EQ;
		
		@Override
		public String toString() {
			switch(this) {
			case GEQ:
				return ">=";
			case LEQ:
				return "<=";
			case EQ:
				return "=";
			default:
				throw new RuntimeException("Invalid constraint type");
			}
		}
	}

	public static class Objective<T> {
		private final Map<T,Coefficient<T>> coefficients = new HashMap<T,Coefficient<T>>();
		public final ObjectiveType objectiveType;

		private Objective(ObjectiveType objectiveType) {
			this.objectiveType = objectiveType;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.objectiveType.toString()).append(" ");			
			for(Coefficient<T> t : this.coefficients.values()) {
				sb.append(t.toString()).append(" + ");
			}
			sb.delete(sb.length()-3, sb.length());
			return sb.toString();
		}
		
		public Collection<Coefficient<T>> getCoefficients() {
			return this.coefficients.values();
		}
		
		public Coefficient<T> getCoefficient(T t) {
			return this.coefficients.get(t);
		}

		public String toMappedString(Map<Integer,T> variableNames, int numVariables) {
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<numVariables; i++) {
				Coefficient<T> coefficient = this.coefficients.get(variableNames.get(i));
				if(coefficient == null) {
					sb.append("0.0 ");
				} else {
					sb.append(coefficient.coefficient + " ");
				}
			}
			return sb.toString().trim();
		}
	}

	public static class Constraint<T> {
		private final Map<T,Coefficient<T>> coefficients = new HashMap<T,Coefficient<T>>();
		public final ConstraintType constraintType;
		public final double constant;

		private Constraint(ConstraintType constraintType, double constant) {
			this.constraintType = constraintType;
			this.constant = constant;
		}
		
		public Collection<Coefficient<T>> getCoefficients() {
			return this.coefficients.values();
		}
		
		public Coefficient<T> getCoefficient(T t) {
			return this.coefficients.get(t);
		}

		public int convertConstraintType() {
			switch(this.constraintType) {
			case LEQ:
				return LpSolve.LE;
			case GEQ:
				return LpSolve.GE;
			case EQ:
				return LpSolve.EQ;
			}
			throw new RuntimeException("Constraint type not recognized!");
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(Coefficient<T> t : this.coefficients.values()) {
				sb.append(t.toString()).append(" + ");
			}
			sb.delete(sb.length()-2, sb.length());
			sb.append(this.constraintType.toString()).append(" ").append(constant);
			return sb.toString();
		}

		public String toMappedString(Map<Integer,T> variableNames, int numVariables) {
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<numVariables; i++) {
				Coefficient<T> coefficient = this.coefficients.get(variableNames.get(i));
				if(coefficient == null) {
					sb.append("0.0 ");
				} else {
					sb.append(coefficient.coefficient + " ");
				}
			}
			return sb.toString().trim();
		}
	}

	private Objective<T> objective = null;
	private List<Constraint<T>> constraints = new ArrayList<Constraint<T>>();
	
	protected abstract LinearProgramResult<T> solve(Objective<T> objective, List<Constraint<T>> constraints);

	public void setObjective(ObjectiveType objectiveType, Collection<Coefficient<T>> coefficients) {
		this.objective = new Objective<T>(objectiveType);
		for(Coefficient<T> coefficient : coefficients) {
			this.objective.coefficients.put(coefficient.variable, coefficient);
		}
	}

	@SuppressWarnings("unchecked")
	public void setObjective(ObjectiveType objectiveType, Coefficient<T> ... coefficients) {
		this.objective = new Objective<T>(objectiveType);
		for(Coefficient<T> coefficient : coefficients) {
			this.objective.coefficients.put(coefficient.variable, coefficient);
		}
	}

	public void addConstraint(ConstraintType constraintType, double constant, Collection<Coefficient<T>> coefficients) {
		Constraint<T> constraint = new Constraint<T>(constraintType, constant);
		for(Coefficient<T> coefficient : coefficients) {
			constraint.coefficients.put(coefficient.variable, coefficient);
		}
		this.constraints.add(constraint);
	}

	public void addConstraint(ConstraintType constraintType, double constant, Coefficient<T> ... coefficients) {
		Constraint<T> constraint = new Constraint<T>(constraintType, constant);
		for(Coefficient<T> coefficient : coefficients) {
			constraint.coefficients.put(coefficient.variable, coefficient);
		}
		this.constraints.add(constraint);
		if(this.constraints.size()%1000 == 0) {
			//System.out.println(this.constraints.size());
		}
	}

	public static class LinearProgramResult<T> {
		public final double objective;
		private final Map<T,Double> variableValues = new HashMap<T,Double>();

		public LinearProgramResult(double objective) {
			this.objective = objective;
		}
		
		public void put(T t, double value) {
			this.variableValues.put(t, value);
		}
		
		public double get(T t) {
			return this.variableValues.get(t);
		}
		
		public Set<Map.Entry<T,Double>> entrySet() {
			return this.variableValues.entrySet();
		}
		
		public Set<T> keySet() {
			return this.variableValues.keySet();
		}
	}
	
	public LinearProgramResult<T> solve() {
		return solve(objective, constraints);
	}
}
