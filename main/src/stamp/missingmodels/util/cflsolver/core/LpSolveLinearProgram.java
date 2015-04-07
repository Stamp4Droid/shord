package stamp.missingmodels.util.cflsolver.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class LpSolveLinearProgram<T> extends LinearProgram<T> {
	@Override
	protected LinearProgramResult<T> solve(Objective<T> objective, List<Constraint<T>> constraints) {
		try {
			return this.solveHelper(objective, constraints);
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to solve LP");
		}
	}

	private static <T> int convertConstraintType(Constraint<T> constraint) {
		switch(constraint.constraintType) {
		case LEQ:
			return LpSolve.LE;
		case GEQ:
			return LpSolve.GE;
		case EQ:
			return LpSolve.EQ;
		}
		throw new RuntimeException("Constraint type not recognized!");
	}

	private static <T> String toMappedString(Constraint<T> constraint, Map<Integer,T> variableNames, int numVariables) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<numVariables; i++) {
			Coefficient<T> coefficient = constraint.getCoefficient(variableNames.get(i));
			if(coefficient == null) {
				sb.append("0.0 ");
			} else {
				sb.append(coefficient.coefficient + " ");
			}
		}
		return sb.toString().trim();
	}

	private static <T> String toMappedString(Objective<T> objective, Map<Integer,T> variableNames, int numVariables) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<numVariables; i++) {
			Coefficient<T> coefficient = objective.getCoefficient(variableNames.get(i));
			if(coefficient == null) {
				sb.append("0.0 ");
			} else {
				sb.append(coefficient.coefficient + " ");
			}
		}
		return sb.toString().trim();
	}
	
	protected LinearProgramResult<T> solveHelper(Objective<T> objective, List<Constraint<T>> constraints) throws LpSolveException {
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
		int numConstraints = constraints.size();
		if(objective == null) {
			throw new RuntimeException("No objective set!");
		}
		for(Coefficient<T> coefficient : objective.getCoefficients()) {
			Integer variable = variables.get(coefficient.variable);
			if(variable == null) {
				variable = numVariables++;
				variables.put(coefficient.variable, variable);
				variableNames.put(variable, coefficient.variable);
			}
		}
		for(Constraint<T> constraint : constraints) {
			for(Coefficient<T> coefficient : constraint.getCoefficients()) {
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
		
		/*
		if(numConstraints > 65000) {
			throw new Error("Too many constraints!");
		}
		*/
		
		System.out.println("Setting up LP");
		long time = System.currentTimeMillis();
		
		LpSolve problem = LpSolve.makeLp(0, numVariables);
		//problem.setAddRowmode(true); // THIS CAUSES BUGS
		
		problem.strSetObjFn(toMappedString(objective, variableNames, numVariables));
		if(objective.objectiveType == ObjectiveType.MAXIMIZE) {
			problem.setMaxim();
		} else if(objective.objectiveType == ObjectiveType.MINIMIZE) {
			problem.setMinim();
		} else {
			throw new RuntimeException("Objective type not recognized!");
		}
		//System.out.println(this.objective.toString());
		for(Constraint<T> constraint : constraints) {
			//System.out.println(constraint.toString());
			problem.strAddConstraint(toMappedString(constraint, variableNames, numVariables), convertConstraintType(constraint), constraint.constant);
		}
		
		// Set variables to be integer valued
		for(int i=1; i<=problem.getNcolumns(); i++) {
			problem.setInt(i, true);
			problem.setUpbo(i, 1.0);
		}
		//problem.writeMps("ilp.mps");
		
		System.out.println("Done in " + (System.currentTimeMillis() - time) + "ms");
		
		//problem.printLp();
	
		System.out.println("Solving LP");
		
		problem.setDebug(true); // set branch & bound debugging to true
		problem.setVerbose(0);
		problem.setScaling(LpSolve.SCALE_MEAN); // use automatic scaling (if int type variable then only rows scaled)
		
		time = System.currentTimeMillis();
		
		problem.solve();
		
		double[] solution = new double[1+numConstraints+numVariables];
		problem.getPrimalSolution(solution);
		problem.deleteLp();

		System.out.println("Solved LP");
		
		LinearProgramResult<T> result = new LinearProgramResult<T>(solution[0]);
		for(int j=solution.length-numVariables; j<solution.length; j++) {
			result.put(variableNames.get(j-1-numConstraints), solution[j]);
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
		LpSolveLinearProgram<String> problem = new LpSolveLinearProgram<String>();
		problem.setObjective(ObjectiveType.MINIMIZE, new Coefficient<String>("x", 2.0), new Coefficient<String>("y", 3.0), new Coefficient<String>("z", -2.0), new Coefficient<String>("w", 3.0));
		problem.addConstraint(ConstraintType.LEQ, 4.0, new Coefficient<String>("x", 3.0), new Coefficient<String>("y", 2.0), new Coefficient<String>("z", 2.0), new Coefficient<String>("w", 1.0));
		problem.addConstraint(ConstraintType.GEQ, 3.0, new Coefficient<String>("x", 0.0), new Coefficient<String>("y", 4.0), new Coefficient<String>("z", 3.0), new Coefficient<String>("w", 1.0));
		
		LinearProgramResult<String> solution = problem.solve();
		System.out.println("Objective: " + solution.objective);
		for(Map.Entry<String,Double> entry : solution.entrySet()) {
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
