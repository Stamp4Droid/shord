package stamp.missingmodels.util.cflsolver.main;

import stamp.missingmodels.util.cflsolver.cfg.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;

public class Main {
	public static void main(String[] args) {
		ContextFreeGrammar c = new ContextFreeGrammar();
		c.addBinaryProduction("S", "(", ")");
		c.addBinaryProduction("S0", "(", "S");
		c.addBinaryProduction("S", "S0", ")");
		c.addBinaryProduction("S", "S", "S");
		System.out.println("CFG:");
		System.out.println(c.toString());
		
		Graph g = new Graph(c);
		g.addEdge("w", "x", "(");
		g.addEdge("x", "y", "(");
		g.addEdge("y", "z", ")");
		g.addEdge("y", "y", ")");
		System.out.println("Graph:");
		System.out.println(g.toString());
		
		new ReachabilitySolver().solve(c, g);
		
		System.out.println("Solution:");
		System.out.println(g.toString());
	}
}
