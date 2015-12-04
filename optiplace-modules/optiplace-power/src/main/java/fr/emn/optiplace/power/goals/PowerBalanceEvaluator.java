package fr.emn.optiplace.power.goals;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;

/**
 * evaluates the formal consumption unbalanced value of a
 * {@link IReconfigurationProblem}
 *
 * @author guillaume
 * @see SearchGoal
 */
public class PowerBalanceEvaluator implements SearchGoal {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PowerBalanceEvaluator.class);

	PowerView consumptionView;

	/** @return the consumptionView */
	public PowerView getConsumptionView() {
		return consumptionView;
	}

	/**
	 * @param consumptionView
	 *            the consumptionView to set
	 */
	public void setConsumptionView(PowerView consumptionView) {
		this.consumptionView = consumptionView;
	}

	public PowerBalanceEvaluator(PowerView consumptionView) {
		super();
		this.consumptionView = consumptionView;
	}

	@Override
	public IntVar getObjective(IReconfigurationProblem rp) {
		return consumptionView.getMaxPowerDiff();
	}
}
