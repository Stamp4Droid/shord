package stamp.missingmodels.analysis;

import stamp.missingmodels.util.ConversionUtils;
import stamp.missingmodels.util.Relation;
import stamp.missingmodels.util.StubLookup;
import stamp.missingmodels.util.StubModelSet;
import stamp.missingmodels.util.StubLookup.StubLookupKey;
import stamp.missingmodels.util.StubModelSet.StubModel;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.jcflsolver.Edge;
import stamp.missingmodels.util.jcflsolver.EdgeData;
import stamp.missingmodels.util.jcflsolver.Graph;

public abstract class JCFLSolverRunner {
	// initialize and run
	public abstract void run(Class<? extends Graph> c, StubModelSet m);
	
	// basic getter methods
	public abstract Graph g();
	public abstract StubLookup s();
	public abstract StubModelSet m(); // input models, just for reference
	
	// methods for running experiments
	public abstract StubModelSet getProposedModels();
	
	/*
	 * Returns set of models such that if all are true, then the
	 * analysis is complete.
	 */
	public static class JCFLSolverSingle extends JCFLSolverRunner {
		private Graph g;
		private StubLookup s;
		private StubModelSet m;

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

		// proposals are true (1)
		public StubModelSet getProposedModels() {
			StubModelSet proposals = new StubModelSet();
			MultivalueMap<Edge,Pair<Edge,Boolean>> positiveWeightEdges = this.g.getPositiveWeightEdges("Src2Sink");
			for(Edge edge : positiveWeightEdges.keySet()) {
				for(Pair<Edge,Boolean> pair : positiveWeightEdges.get(edge)) {
					EdgeData data = pair.getX().getData(this.g);					
					proposals.put(new StubModel(this.s.get(new StubLookupKey(data.symbol, data.from, data.to))), 1);
				}
			}
			return proposals;
		}

		public void run(Class<? extends Graph> c, StubModelSet m) {
			// STEP 0: Set up the fields.
			try {
				this.g = c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Error creating graph: " + c.toString() + "!");
			}
			this.s = new StubLookup();
			this.m = m;

			// STEP 1: Fill the edges in the graph.
			this.fillTerminalEdges();

			// STEP 2: Run the algorithm.
			this.g.algo.process();
		}
	}

	/*
	 * Returns set of models such that if all are false, then
	 * analysis is complete. Does so by iteratively running
	 * the previous algorithm and rejecting proposed models.
	 */
	public static class JCFLSolverStubs extends JCFLSolverRunner {
		private JCFLSolverSingle j;
		private StubModelSet m;
		private Class<? extends Graph> c;
		private StubModelSet allProposed;

		/*
		 * Returns the last executed graph.
		 * @see stamp.missingmodels.analysis.JCFLSolverRunner#g()
		 */
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
		
		/*
		 * Proposals are false (2).
		 * @see stamp.missingmodels.analysis.JCFLSolverRunner#getProposedModels()
		 */
		public StubModelSet getProposedModels() {
			return this.allProposed;
		}

		private void runHelper() {
			this.allProposed = new StubModelSet(); // proposed models
			StubModelSet total = new StubModelSet(); // all models
			total.putAll(this.m); // initialize to the given known models
			
			// keep running and rejecting
			StubModelSet curProposed;
			do {
				// run the solver
				this.j = new JCFLSolverSingle();
				j.run(this.c, total);
				
				// add the current proposed models to all proposed and total
				curProposed = this.j.getProposedModels();
				total.putAllValue(curProposed, 2);
				this.allProposed.putAllValue(curProposed, 2);
			} while(!curProposed.isEmpty());
		}

		public void run(Class<? extends Graph> c, StubModelSet m) {
			this.c = c;
			this.m = m;
			runHelper();
		}
	}
}
