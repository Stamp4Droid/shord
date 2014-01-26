package stamp.missingmodels.util.cflsolver.main;

import stamp.missingmodels.util.cflsolver.cfg.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;

public class Main {
	public static void main(String[] args) {
		ContextFreeGrammar c = new ContextFreeGrammar(4);
		c.addUnaryProduction(2, 1);
		c.addBinaryProduction(3, 1, 2);
		System.out.println("CFG:");
		System.out.println(c.toString());
		Graph g = new Graph(4);
		g.addEdge("x", "y", 1, -1);
		g.addEdge("y", "z", 1, -1);
		System.out.println("Graph:");
		System.out.println(g.toString());
		new ReachabilitySolver().solve(c, g);
		System.out.println("Solution:");
		System.out.println(g.toString());
	}
}
