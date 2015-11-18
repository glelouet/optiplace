/**
 *
 */

package fr.emn.optiplace.core.goals;

import java.util.List;

import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.core.heuristics.StickVMsHeuristic;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;


/**
 * Heuristic of reducing the number of migrations.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class MigrationReducerGoal implements SearchGoal {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MigrationReducerGoal.class);

	private final String resourceName;

	/**
	 * @param resourceCost
	 *          the resource wich makes each migration costly (eg the mem)
	 * @throws NullPointerException
	 *           if the resource specified is null
	 */
	public MigrationReducerGoal(String name) {
		resourceName = name;
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
		return rp.scalar(rp.isMigrateds(), rh.getVmsLoads());
	}

	@Override
	public List<AbstractStrategy<? extends Variable>> getHeuristics(IReconfigurationProblem rp) {
		return new StickVMsHeuristic(rp.getResourcesHandlers().get(resourceName).getSpecs().makeVMComparator(false))
		    .getHeuristics(rp);
	}

}
