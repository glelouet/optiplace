/**
 *
 */
package entropy.core.goals;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.core.heuristics.StickVMsHeuristic;
import entropy.solver.choco.ReconfigurationProblem;
import entropy.view.SearchGoal;
import entropy.view.SearchHeuristic;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class MigrationReducerGoal implements SearchGoal {

	@Override
	public IntDomainVar getObjective(ReconfigurationProblem rp) {
		return rp.nbMigrations();
	}

	@Override
	public SearchHeuristic[] getHeuristics() {
		return new SearchHeuristic[]{new StickVMsHeuristic()};
	}

}
