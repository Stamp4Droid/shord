package shord.missingmodels.analysis;

import stamp.missingmodels.util.ConversionUtils;
import stamp.missingmodels.util.Relation;
import stamp.missingmodels.util.StubLookup;
import stamp.missingmodels.util.StubModelSet;
import stamp.missingmodels.util.jcflsolver.Graph;

public abstract class JCFLSolverRunner {
	public abstract Graph g();
	public abstract StubLookup s();
	public abstract StubModelSet m();

	public static class JCFLSolverStubs extends JCFLSolverRunner {
		private JCFLSolverSingle j;
		private final StubModelSet m;
		private final Class<? extends Graph> c;

		public Graph g() {
			if(j == null) {
				return null;
			}
			return this.j.g;
		}

		public StubLookup s() {
			if(j == null) {
				return null;
			}
			return this.j.s;
		}

		public StubModelSet m() {
			if(j == null) {
				return null;
			}
			return this.j.m;
		}

		public void run() {
			try {
				j = new JCFLSolverSingle(this.c.newInstance(), this.m);
				StubModelSet newM;
				while(!(newM = new StubModelSet(j.g(), j.s())).isEmpty()){
					this.m.putAllValue(newM, 2);
					j = new JCFLSolverSingle(this.c.newInstance(), this.m);
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
				throw new RuntimeException("Error creating graph: " + c.toString() + "!");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Error creating graph: " + c.toString() + "!");
			}
		}

		public JCFLSolverStubs(Class<? extends Graph> c, StubModelSet m) {
			this.c = c;
			this.m = m;
			run();
		}
	}

	public static class JCFLSolverSingle extends JCFLSolverRunner {
		private final Graph g;
		private final StubLookup s;
		private final StubModelSet m;

		public Graph g() {
			return this.g;
		}

		public StubLookup s() {
			return this.s;
		}

		public StubModelSet m() {
			return this.m;
		}

		/*
		 * The following code is for running the JCFLSolver analysis.
		 */
		private void fillTerminalEdges() {
			for(int k=0; k<this.g.numKinds(); k++) {
				if(this.g.isTerminal(k)) {
					if(ConversionUtils.getChordRelationsFor(this.g.kindToSymbol(k)).isEmpty()) {
						System.out.println("No edges found for relation " + this.g.kindToSymbol(k) + "...");
					}
					for(Relation rel : ConversionUtils.getChordRelationsFor(this.g.kindToSymbol(k))) {
						rel.addEdges(this.g.kindToSymbol(k), this.g, this.s, this.m);
					}
				}
			}
		}

		public JCFLSolverSingle(Graph g) {
			this(g, new StubModelSet());
		}

		public JCFLSolverSingle(Graph g, StubModelSet m) {
			// STEP 0: Set up the fields.
			this.g = g;
			this.s = new StubLookup();
			this.m = m;

			// STEP 1: Fill the edges in the graph.
			this.fillTerminalEdges();

			// STEP 2: Run the algorithm.
			this.g.algo.process();
		}
	}
}
