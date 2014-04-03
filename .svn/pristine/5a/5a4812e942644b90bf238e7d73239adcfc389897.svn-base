/**
 *
 */
package fr.emn.optiplace.core.goals;

import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.core.heuristics.StickVMsHeuristic;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.SearchHeuristic;

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
