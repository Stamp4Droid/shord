package main;

import graph.Graph;
import solver.ReachabilitySolver;
import cfg.ContextFreeGrammar;

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
