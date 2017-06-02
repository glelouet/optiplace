package fr.emn.optiplace.power.goals;

import java.util.List;

import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.power.heuristics.EfficiencyMaxFirstNodes;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;

/**
 * generate the total consumption of a datacenter in a
 * {@link IReconfigurationProblem} <br />
 * set its {@link #unusedOff} value to true to force unused servers' consumption
 * to 0.
 *
 * @author guillaume
 */
public class TotalPowerEvaluator implements SearchGoal {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TotalPowerEvaluator.class);

	/** mother view */
	PowerView consumptionView;

	/**
	 * @param consumptionView
	 *          the view that describes the consumptions of servers and the
	 *          ReconfigurationPRoblem.
	 * @param unusedOff
	 *          to specify that unused Servers have a consumption set to 0.
	 */
	public TotalPowerEvaluator(PowerView consumptionView) {
		super();
		this.consumptionView = consumptionView;
	}

	/**
	 * construct a new evaluator with no data. set them later.
	 */
	public TotalPowerEvaluator() {
		this(null);
	}

	@Override
	public IntVar getObjective(IReconfigurationProblem rp) {
		return consumptionView.getTotalPower();
	}

	@Override
	public List<AbstractStrategy<? extends Variable>> getHeuristics(IReconfigurationProblem rp) {
		return new EfficiencyMaxFirstNodes(consumptionView.getPowerData(), null).getHeuristics(rp);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
