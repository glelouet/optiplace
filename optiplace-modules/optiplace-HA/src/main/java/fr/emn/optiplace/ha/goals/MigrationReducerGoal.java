/**
 *
 */

package fr.emn.optiplace.ha.goals;

import java.util.List;

import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.core.heuristics.StickVMsHeuristic;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;


/**
 * goal of reducing the number of migrations.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class MigrationReducerGoal implements SearchGoal {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MigrationReducerGoal.class);

	private final String resName;

	/**
	 * @param resourceCost
	 *          the resource wich makes each migration costly (eg the mem)
	 * @throws NullPointerException
	 *           if the resource specified is null
	 */
	public MigrationReducerGoal(String resName) {
		this.resName = resName;
		if (resName == null) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public IntVar getObjective(IReconfigurationProblem rp) {
		return rp.v().scalar(rp.isMigrateds(), rp.getUse(resName).getVMsLoads());
	}

	@Override
	public List<AbstractStrategy<? extends Variable>> getHeuristics(IReconfigurationProblem rp) {
		return new StickVMsHeuristic(rp.specs(resName).makeVMComparator(false))
		    .getHeuristics(rp);
	}

}
