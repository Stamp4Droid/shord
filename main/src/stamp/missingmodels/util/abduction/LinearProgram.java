package stamp.missingmodels.util.abduction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;
import stamp.missingmodels.util.cflsolver.util.ConversionUtils;

public class LinearProgram<T> {
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

	private static class Objective<T> {
		private final Map<T,Coefficient<T>> coefficients = new HashMap<T,Coefficient<T>>();
		private final ObjectiveType objectiveType;

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

		private String toMappedString(Map<Integer,T> variableNames, int numVariables) {
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

	private static class Constraint<T> {
		private final Map<T,Coefficient<T>> coefficients = new HashMap<T,Coefficient<T>>();
		private final ConstraintType constraintType;
		private final double constant;

		private Constraint(ConstraintType constraintType, double constant) {
			this.constraintType = constraintType;
			this.constant = constant;
		}

		private int convertConstraintType() {
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

		private String toMappedString(Map<Integer,T> variableNames, int numVariables) {
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
		public final Map<T,Double> variableValues = new HashMap<T,Double>();

		public LinearProgramResult(double objective) {
			this.objective = objective;
		}
	}
	
	public LinearProgramResult<T> solve() throws LpSolveException {
		try {
			System.load("/home/obastani/Documents/projects/research/stamp/shord/lib/liblpsolve55.so");
			System.load("/home/obastani/Documents/projects/research/stamp/shord/lib/liblpsolve55j.so");
		} catch (Error e) {
			e.printStackTrace();
		}

		System.out.println("Setting up constraints");
		
		Map<T,Integer> variables = new HashMap<T,Integer>();
		Map<Integer,T> variableNames = new HashMap<Integer,T>();
		int numVariables = 0;
		int numConstraints = this.constraints.size();
		if(this.objective == null) {
			throw new RuntimeException("No objective set!");
		}
		for(Coefficient<T> coefficient : this.objective.coefficients.values()) {
			Integer variable = variables.get(coefficient.variable);
			if(variable == null) {
				variable = numVariables++;
				variables.put(coefficient.variable, variable);
				variableNames.put(variable, coefficient.variable);
			}
		}
		for(Constraint<T> constraint : this.constraints) {
			for(Coefficient<T> coefficient : constraint.coefficients.values()) {
				Integer variable = variables.get(coefficient.variable);
				if(variable == null) {
					variable = numVariables++;
					variables.put(coefficient.variable, variable);
					variableNames.put(variable, coefficient.variable);
				}
			}
		}
		
		System.out.println("Number of variables: " + numVariables);
		System.out.println("Number of constraints: " + numConstraints);
		
		if(numConstraints > 65000) {
			throw new Error("Too many constraints!");
		}
		
		System.out.println("Setting up LP");
		long time = System.currentTimeMillis();
		
		LpSolve problem = LpSolve.makeLp(0, numVariables);
		//problem.setAddRowmode(true); // THIS CAUSES BUGS
		
		problem.strSetObjFn(this.objective.toMappedString(variableNames, numVariables));
		if(this.objective.objectiveType == ObjectiveType.MAXIMIZE) {
			problem.setMaxim();
		} else if(this.objective.objectiveType == ObjectiveType.MINIMIZE) {
			problem.setMinim();
		} else {
			throw new RuntimeException("Objective type not recognized!");
		}
		//System.out.println(this.objective.toString());
		for(Constraint<T> constraint : this.constraints) {
			//System.out.println(constraint.toString());
			problem.strAddConstraint(constraint.toMappedString(variableNames, numVariables), constraint.convertConstraintType(), constraint.constant);
		}
		
		// Set variables to be integer valued
		for(int i=1; i<=problem.getNcolumns(); i++) {
			problem.setInt(i, true);
			problem.setUpbo(i, 1.0);
		}
		problem.writeMps("ilp.mps");
		
		System.out.println("Done in " + (System.currentTimeMillis() - time) + "ms");
		
		//problem.printLp();
	
		System.out.println("Solving LP");
		
		problem.setDebug(true); // set branch & bound debugging to true
		problem.setVerbose(0);
		//problem.setScaling(LpSolve.SCALE_MEAN); // use automatic scaling (if int type variable then only rows scaled)
		
		time = System.currentTimeMillis();
		
		double[] solution = new double[1+numConstraints+numVariables];
		
		try {
			Process p = Runtime.getRuntime().exec("./scip-3.1.0.linux.x86_64.gnu.opt.spx -l sol.txt -f ilp.mps -q");
			try {
				p.waitFor();
			} catch(Exception e) {
				e.printStackTrace();
			}
			System.out.println("Done in " + (System.currentTimeMillis() - time) + "ms");
			BufferedReader solutionReader = new BufferedReader(new FileReader("sol.txt"));
			boolean read = false;
			for(String line = solutionReader.readLine(); line != null; line = solutionReader.readLine()) {
				if(line.equals("primal solution:")) {
					read = true;
					continue;
				}
				if(line.equals("Statistics")) {
					read = false;
					continue;
				}
				if(read && line.startsWith("C")) {
					String[] tokens = line.split(" ");
					int i = Integer.parseInt(tokens[0].substring(1));
					solution[numConstraints+i] = 1;
				}
			}
			solutionReader.close();
			new File("sol.txt").delete();
			new File("ilp.mps").delete();
		} catch(Exception e) {
			e.printStackTrace();
		}

		/*
		problem.solve();
		
		double[] solution = new double[1+numConstraints+numVariables];
		problem.getPrimalSolution(solution);
		*/
		problem.deleteLp();

		System.out.println("Solved LP");
		
		LinearProgramResult<T> result = new LinearProgramResult<T>(solution[0]);
		for(int j=solution.length-numVariables; j<solution.length; j++) {
			result.variableValues.put(variableNames.get(j-1-numConstraints), solution[j]);
		}
		return result;
	}

	/*
	public static void main(String[] args) throws LpSolveException {
		test1();
		//test2();
	}
	*/

	public static void test1() throws LpSolveException {
		LinearProgram<String> problem = new LinearProgram<String>();
		problem.setObjective(ObjectiveType.MINIMIZE, new Coefficient<String>("x", 2.0), new Coefficient<String>("y", 3.0), new Coefficient<String>("z", -2.0), new Coefficient<String>("w", 3.0));
		problem.addConstraint(ConstraintType.LEQ, 4.0, new Coefficient<String>("x", 3.0), new Coefficient<String>("y", 2.0), new Coefficient<String>("z", 2.0), new Coefficient<String>("w", 1.0));
		problem.addConstraint(ConstraintType.GEQ, 3.0, new Coefficient<String>("x", 0.0), new Coefficient<String>("y", 4.0), new Coefficient<String>("z", 3.0), new Coefficient<String>("w", 1.0));
		
		LinearProgramResult<String> solution = problem.solve();
		System.out.println("Objective: " + solution.objective);
		for(Map.Entry<String,Double> entry : solution.variableValues.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}

	public static void test2() throws LpSolveException {
		System.load("/home/obastani/Documents/projects/research/stamp/shord/lib/liblpsolve55.so");
		System.load("/home/obastani/Documents/projects/research/stamp/shord/lib/liblpsolve55j.so");
		
		LpSolve problem = LpSolve.makeLp(0, 4); // make a new problem with 4 variables and 0 constraints
		problem.strAddConstraint("3 2 2 1", LpSolve.LE, 4); // add constraint 3*c1 + 2*c2 + 2*c3 + c4 <= 4
		problem.strAddConstraint("0 4 3 1", LpSolve.GE, 3); // add constraint 4*c2 + 3*c3 + c4 >= 3
		problem.strSetObjFn("2 3 -2 3"); // set objective to 2*c1 + 3*c2 - 2*c3 + 3*c4

		problem.printLp(); // print the problem
		System.out.println(problem.solve()); // solve the problem
		
		// We can display the solution with problem.printObjective(), problem.printSolution(1), and problem.printConstraints(1)
		problem.solve();
		problem.printObjective();
		problem.printSolution(1);
		problem.printConstraints(1);
		problem.printDuals();
		
		/*
		problem.setMaxim(); // maximize objective
		problem.setMat(2, 1, 0.5); // set matrix element (2,1) to 0.5
		problem.setRh(1, 7.45); // change rhs element of line 1 to 7.45
		problem.setInt(4, true); // set c4 to integer type
		problem.setDebug(true); // set branch & bound debugging to true
		problem.setLowbo(2, 2); // set lower bound of c2 to 2
		problem.setUpbo(4, 5.3); // set upper bound of c4 to 5.3
		problem.delConstraint(1); // delete first constraint

		problem.strAddConstraint("1 2 1 4", LpSolve.EQ, 8); // add constraint c1 + 2*c2 + c3 * 4*c4 = 8
		problem.strAddColumn("3 2 2"); // add column
		problem.delColumn(3); // delete column 3
		problem.setScaling(LpSolve.SCALE_MEAN); // use automatic scaling (if int type variable then only rows scaled)
		problem.getMat(2, 3); // returns a single matrix element (unscaled problem)
		problem.setInt(3, false); // undo int type
		problem.unscale(); // turn off scaling

		problem.setDebug(false); // turn off debugging
		problem.setTrace(true); // turn on problem trace
		problem.resetBasis(); // problem will try to solve at the last found basis unless it is reset
		problem.setRowName(1, "speed"); // give variables and constraints names
		problem.setColName(2, "money");
		problem.printLp();
		problem.deleteLp();
		*/
	}
}
