/**
 *
 */
package fr.emn.optiplace.core.goals;

import solver.variables.IntVar;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.core.heuristics.StickVMsHeuristic;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.SearchHeuristic;

/**
 * Heuristic of reducing the number of migrations.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class MigrationReducerGoal implements SearchGoal {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MigrationReducerGoal.class);

	private final String resourceName;

	/**
	 * @param resourceCost
	 * the resource wich makes each migration costly (eg the mem)
	 * @throws NullPointerException
	 * if the resource specified is null
	 */
	public MigrationReducerGoal(String name) {
		this.resourceName = name;
		if (name == null) {
			throw new NullPointerException();
		}
	}

	@Override
	public IntVar getObjective(IReconfigurationProblem rp) {
		ResourceHandler rh = rp.getResourcesHandlers().get(resourceName);
		if (rh == null) {
			logger.warn("can not get the resource specification for " + resourceName);
			return null;
		}
		return rp.scalar(rp.isMigrateds(), rh.getVmsUses());
	}

	@Override
	public SearchHeuristic[] getHeuristics(IReconfigurationProblem rp) {
		return new SearchHeuristic[] { new StickVMsHeuristic(rp
				.getResourcesHandlers().get(resourceName).getSpecs()
				.makeVMComparator(false)) };
	}

}
