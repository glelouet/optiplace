
package fr.emn.optiplace.power.rules;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;


/**
 * constraint specifying that a group of node should not consume more than a
 * given amount of Watt. <br />
 * need to be given a {@link #consumptions ConsumptionData}<br />
 *
 * @author guillaume
 */
public class LimitSumPower extends LimitPower {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LimitSumPower.class);


	public LimitSumPower(PowerView consumptionView, int maxConsumption, Node... nodes) {
		super(consumptionView, maxConsumption, nodes);
	}

	public LimitSumPower(PowerView consumptionView, int maxConsumption, Set<Node> nodes) {
		super(consumptionView, maxConsumption, nodes);
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		List<IntVar> l = getNodes(core.c()).map(parent::getPower).collect(Collectors.toList());
		IntVar totalPower = parent.v.sum("sumpowers", l.toArray(new IntVar[] {}));
		try {
			totalPower.updateUpperBound(maxConsumption, Cause.Null);
		}
		catch (ContradictionException e) {
			// this should never happen
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		return getNodes(cfg).mapToDouble(n -> parent.getPowerData().getConsumption(cfg, n)).sum() < maxConsumption;
	}
}
