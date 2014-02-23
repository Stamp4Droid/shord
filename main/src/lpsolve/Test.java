package lpsolve;

public class Test {
	public static void main(String[] args) throws LpSolveException {
		new Test().execute2();
	}

	public void execute1() throws LpSolveException {
		System.out.println("PATH : " + System.getProperty("java.library.path"));
		System.out.println("CLASSPATH : " + System.getProperty("java.class.path"));

		System.load("/home/obastani/Documents/projects/stamp/shord/lib/liblpsolve55.so");
		System.load("/home/obastani/Documents/projects/stamp/shord/lib/liblpsolve55j.so");

		LpSolve solver = LpSolve.makeLp(0,4);
		solver.strAddConstraint("3 2 2 1", LpSolve.LE, 4);
		solver.strAddConstraint("0 4 3 1", LpSolve.GE, 3);

		// set objective function                                                                                                                                                               
		solver.strSetObjFn("2 3 -2 3");

		// solve the problem                                                                                                                                                                    
		solver.solve();

		// print solution                                                                                                                                                                       
		System.out.println("Value of objective function: " + solver.getObjective());
		double[] var = solver.getPtrVariables();
		for (int i = 0; i < var.length; i++) {
			System.out.println("Value of var[" + i + "] = " + var[i]);
		}

		// delete the problem and free memory
		solver.deleteLp();
	}

	public void execute2() throws LpSolveException {
		System.load("/home/obastani/Documents/projects/stamp/shord/lib/liblpsolve55.so");
		System.load("/home/obastani/Documents/projects/stamp/shord/lib/liblpsolve55j.so");
		
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
	}
}
