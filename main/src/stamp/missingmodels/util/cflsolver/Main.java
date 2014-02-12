package stamp.missingmodels.util.cflsolver;


public class Main {
	public static void main(String[] args) {
		ContextFreeGrammar flowGrammar = new ContextFreeGrammar();
		flowGrammar.addUnaryProduction("flow", "new");
		flowGrammar.addBinaryProduction("flow", "flow", "assign");
		flowGrammar.addProduction("flowField", new String[]{"flow", "put", "flow"}, new boolean[]{false, false, true});
		flowGrammar.addProduction("flow", new String[]{"flowField", "flow", "get"});
		
		System.out.println("CFG:");
		System.out.println(flowGrammar.toString());
		
		Graph g = new Graph(flowGrammar);
		g.addEdge("o1", "x", "new");
		g.addEdge("o2", "z", "new");
		g.addEdge("x", "y", "assign");
		g.addEdge("z", "y", "put", "f");
		g.addEdge("x", "w", "assign");
		g.addEdge("w", "v", "get", "f");
		System.out.println("Graph:");
		System.out.println(g.toString());
		
		new ReachabilitySolver().solve(flowGrammar, g);
		
		System.out.println("Solution:");
		System.out.println(g.toString());
	}
}
