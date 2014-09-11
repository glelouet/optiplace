/**
 *
 */
package fr.emn.optiplace.core.goals;

import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.core.heuristics.StickVMsHeuristic;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.SearchHeuristic;

/**
 * Heuristic of reducing the cost of migrations. The cost of a migration is
 * given by a resource specification, the migration of a VM costs as much as its
 * use of this resource.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class MigrationReducerGoal implements SearchGoal {

	private final ResourceSpecification resourceCost;

	/**
	 * @param resourceCost
	 * the resource wich makes each migration costly (eg the mem)
	 * @throws NullPointerException
	 * if the resource specified is null
	 */
	public MigrationReducerGoal(ResourceSpecification resourceCost) {
		this.resourceCost = resourceCost;
		if (resourceCost == null) {
			throw new NullPointerException();
		}
	}

	@Override
	public IntDomainVar getObjective(ReconfigurationProblem rp) {
		return rp.nbMigrations();
	}

	@Override
	public SearchHeuristic[] getHeuristics() {
		return new SearchHeuristic[] { new StickVMsHeuristic(
				resourceCost.makeVMComparator(false)) };
	}

}
