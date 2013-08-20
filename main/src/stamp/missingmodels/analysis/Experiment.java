package stamp.missingmodels.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stamp.missingmodels.util.StubModelSet;
import stamp.missingmodels.util.StubModelSet.StubModel;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.jcflsolver.Graph;

public class Experiment {
	private JCFLSolverRunner j;
	private Class<? extends Graph> c;
	
	private List<StubModelSet> proposed = new ArrayList<StubModelSet>();
	
	/*
	 * Returns the runner.
	 */
	public JCFLSolverRunner j() {
		return this.j;
	}
	
	/*
	 * Returns a list of all proposed models.
	 */
	public StubModelSet getAllProposedModels() {
		StubModelSet allProposed = new StubModelSet();
		for(StubModelSet m : this.proposed) {
			allProposed.putAll(m);
		}
		return allProposed;
	}
	
	/*
	 * Statistic 1: The number of proposed models.
	 */
	public int getNumProposed() {
		return this.getAllProposedModels().size();
	}
	
	/*
	 * Statistic 2: The number of rounds.
	 */
	public int getNumRounds() {
		return this.proposed.size();
	}
	
	/*
	 * Statistic 3: The accuracy.
	 */
	// TODO: here

	/*
	 * @param groundTruthModels The ground truth stub models.
	 * @param initialModels The set of models to use initially.
	 * @param defaultModelValue If a model is not present, how
	 * to treat it. Default is 0.
	 */ 
	public void run(StubModelSet groundTruthModels, StubModelSet initialModels, int defaultModelValue) {
		// STEP 0: Initilize the total set of models
		StubModelSet total = new StubModelSet();
		total.putAll(initialModels);
		
		// STEP 1: Iteratively run the solver and add models.
		StubModelSet curProposed;
		do {
			// STEP 1a: Run the analysis.
			this.j.run(this.c, total);
			
			// STEP 1b: Get the proposed models
			curProposed = this.j.getProposedModels();
			
			System.out.println(curProposed.size());
			
			// STEP 1c: Add the proposed models to the list.
			this.proposed.add(curProposed);
			
			// STEP 1d: Add the proposed models to the total set of models.
			for(StubModel model : curProposed.keySet()) {
				// For now, not containing a model is equivalent to its having value 0.
				// TODO: Is there a situation where we would want to keep this separate?
				if(groundTruthModels.get(model) != 0) {
					total.put(model, groundTruthModels.get(model));
				} else {
					total.put(model, defaultModelValue);
				}
			}
		} while(!curProposed.isEmpty());
	}
	
	public void run(StubModelSet groundTruthModels, StubModelSet initialModels) {
		run(groundTruthModels, initialModels, 0); 
	}
	
	/*
	 * Initializes the experiment
	 * @param cj The class of the runner to use to propose models.
	 * @param cg The class of graph to use.
	 */
	public Experiment(Class<? extends JCFLSolverRunner> cj, Class<? extends Graph> c) {
		try {
			this.j = cj.newInstance();
			this.c = c;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Error initializing jcfl runner: " + c.toString());
		}
	}
	
	/*
	 * A stub model class augmented with some data.
	 */
	public abstract static class StubModelSetWithData<T> extends StubModelSet {
		private static final long serialVersionUID = 906602907093081931L;
		
		private Map<StubModel,T> data = new HashMap<StubModel,T>();
		
		public T getData(StubModel model) {
			T datum = this.data.get(model);
			if(datum == null) {
				datum = this.defaultValue();
				this.data.put(model, datum);
			}
			return datum;
		}
		
		@Override
		public Integer put(StubModel key, Integer value) {
			return this.put(key, value, this.defaultValue());
		}
		
		public Integer put(StubModel key, Integer value, T data) {
			this.data.put(key, data);
			return super.put(key, value);
		}
		
		public abstract T defaultValue();
		
		public abstract String toString(T data);
		public abstract T parseData(String representation);
	}
	
	/*
	 * A stub model class where the data is (proposed,round).
	 * Default proposed value is 0, default round value is -1.
	 */
	public static class ProposedStubModelSet extends StubModelSetWithData<Pair<Integer,Integer>> {
		public static final int DEFAULT_ROUND = -1;
		private static final long serialVersionUID = 4715908928086091234L;
		
		public Integer put(StubModel key, Integer value, int proposed, int round) {
			return super.put(key, value, new Pair<Integer,Integer>(proposed, round));
		}

		@Override
		public Pair<Integer,Integer> defaultValue() {
			return new Pair<Integer,Integer>(0,-1);
		}

		@Override
		public String toString(Pair<Integer,Integer> data) {
			return data.toString();
		}

		@Override
		public Pair<Integer,Integer> parseData(String representation) {
			String[] tokens = representation.substring(1, representation.length()-1).split(",");
			return new Pair<Integer,Integer>(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
		}
	}
}
