/**
 *
 */
package fr.emn.optiplace.goals;

import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class NBMigrationsCost implements SearchGoal {

	public static final NBMigrationsCost INSTANCE = new NBMigrationsCost();

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(NBMigrationsCost.class);

	@Override
	public IntDomainVar getObjective(ReconfigurationProblem rp) {
		return rp.nbMigrations();
	}
}
