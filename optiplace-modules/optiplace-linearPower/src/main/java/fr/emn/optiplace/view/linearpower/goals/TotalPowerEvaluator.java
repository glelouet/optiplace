/**
 *
 */
package fr.emn.optiplace.view.linearpower.goals;

import org.chocosolver.solver.variables.IntVar;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.linearpower.LinearPowerView;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public class TotalPowerEvaluator implements SearchGoal {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(TotalPowerEvaluator.class);

	protected final LinearPowerView parent;

	/**
	 *
	 */
	public TotalPowerEvaluator(LinearPowerView parent) {
		this.parent = parent;
	}

	@Override
	public IntVar getObjective(IReconfigurationProblem rp) {
		return parent.getTotalPower();
	}
}
