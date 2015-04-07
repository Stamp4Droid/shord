package stamp.missingmodels.util.cflsolver.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stamp.missingmodels.util.cflsolver.core.Util.Pair;

public class SCIPLinearProgram<T> extends LinearProgram<T> {
	@Override
	protected LinearProgramResult<T> solve(Objective<T> objective, List<Constraint<T>> constraints) {
		try {
			return solveHelper(objective, constraints);
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to solve LP!");
		}
	}

	private LinearProgramResult<T> solveHelper(Objective<T> objective, List<Constraint<T>> constraints) throws Exception {
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
		
		new File("ilp.mps").delete();
		PrintWriter pw = new PrintWriter("ilp.mps");
		pw.println("NAME");
		
		pw.println("ROWS");
		pw.println(" N  R0");
		Map<T,List<Pair<Integer,Double>>> variableConstraints = new HashMap<T,List<Pair<Integer,Double>>>();
		for(Coefficient<T> coefficient : objective.getCoefficients()) {
			List<Pair<Integer,Double>> variableConstraint = variableConstraints.get(coefficient.variable);
			if(variableConstraint == null) {
				variableConstraint = new ArrayList<Pair<Integer,Double>>();
				variableConstraints.put(coefficient.variable, variableConstraint);
			}
			variableConstraint.add(new Pair<Integer,Double>(0, coefficient.coefficient));
		}
		
		int counter = 1;
		for(Constraint<T> constraint : constraints) {
			for(Coefficient<T> coefficient : constraint.getCoefficients()) {
				List<Pair<Integer,Double>> variableConstraint = variableConstraints.get(coefficient.variable);
				if(variableConstraint == null) {
					variableConstraint = new ArrayList<Pair<Integer,Double>>();
					variableConstraints.put(coefficient.variable, variableConstraint);
				}
				variableConstraint.add(new Pair<Integer,Double>(counter, coefficient.coefficient));
			}
			char type;
			switch(constraint.constraintType) {
			case GEQ:
				type = 'G';
				break;
			case LEQ:
				type = 'L';
				break;
			case EQ:
				type = 'E';
				break;
			default:
				throw new RuntimeException("Unrecognized constraint type!");
			}
			pw.println(" " + type + "  R" + counter++);
		}
		
		pw.println("COLUMNS");
		pw.println("    MARK0000  'MARKER'                 'INTORG'");
		for(int i=0; i<numVariables; i++) {
			List<Pair<Integer,Double>> variableConstraint = variableConstraints.get(variableNames.get(i));
			for(int j=0; j<variableConstraint.size()/2; ++j) {
				Pair<Integer,Double> first = variableConstraint.get(2*j);
				Pair<Integer,Double> second = variableConstraint.get(2*j+1);
				pw.format("    C%-9dR%-9d%12.8f   R%-9d%12.8f\n", i+1, first.getX(), first.getY(), second.getX(), second.getY());
			}
			if(variableConstraint.size()%2 != 0) {
				Pair<Integer,Double> first = variableConstraint.get(variableConstraint.size()-1);
				pw.format("    C%-9dR%-9d%12.8f\n", i+1, first.getX(), first.getY());
			}
		}
		pw.println("    MARK0001  'MARKER'                 'INTEND'");
		
		pw.println("RHS");
		for(int i=0; i<constraints.size()/2; i++) {
			Constraint<T> first = constraints.get(2*i);
			Constraint<T> second = constraints.get(2*i+1);
			pw.format("    RHS       R%-9d%12.8f   R%-9d%12.8f\n", (2*i+1), first.constant, (2*i+2), second.constant);
		}
		if(constraints.size()%2 != 0) {
			Constraint<T> first = constraints.get(constraints.size()-1);
			pw.format("    RHS       R%-9d%12.8f\n", constraints.size(), first.constant);
		}

		pw.println("BOUNDS");
		for(int i=0; i<numVariables; i++) {
			pw.println(" BV BND       C" + (i+1));
		}
		
		pw.println("ENDATA");
		
		pw.close();
		
		System.out.println("Done in " + (System.currentTimeMillis() - time) + "ms");
		System.out.println("Solving LP");
		time = System.currentTimeMillis();
		double[] solution = new double[1+numConstraints+numVariables];
		
		new File("sol.txt").delete();
		Process p = Runtime.getRuntime().exec("../../lib/scip -l sol.txt -f ilp.mps -q");
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
			
		System.out.println("Solved LP");
		
		LinearProgramResult<T> result = new LinearProgramResult<T>(solution[0]);
		for(int j=solution.length-numVariables; j<solution.length; j++) {
			result.put(variableNames.get(j-1-numConstraints), solution[j]);
		}
		return result;
	}
}
