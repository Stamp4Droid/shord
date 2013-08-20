package stamp.missingmodels.analysis;

import java.util.ArrayList;
import java.util.List;

import stamp.missingmodels.util.StubModelSet;
import stamp.missingmodels.util.StubModelSet.StubModel;
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
}
