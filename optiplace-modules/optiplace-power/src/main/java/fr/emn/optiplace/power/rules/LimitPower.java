package fr.emn.optiplace.power.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.solver.ReconfigurationResult;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * constraint specifying that each node of a set should not consume more than a
 * given amount of Watt. <br />
 * need to be given a {@link #consumptions ConsumptionData}<br />
 *
 * @author guillaume
 */
public class LimitPower implements Rule {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LimitPower.class);

	protected int maxConsumption;

	protected PowerView consumptionView;

	protected Set<Node> nodes;

	public LimitPower() {
		this(null, 0);
	}

	public LimitPower(PowerView consumptionView, int maxConsumption, Node... nodes) {
		this(consumptionView, maxConsumption, new HashSet<>(Arrays.asList(nodes)));
	}

	public LimitPower(PowerView consumptionView, int maxConsumption, Set<Node> nodes) {
		this.maxConsumption = maxConsumption;
		this.consumptionView = consumptionView;
		this.nodes = nodes;
	}

	/** @return the maxConsumption */
	public int getMaxConsumption() {
		return maxConsumption;
	}

	/**
	 * @param maxConsumption
	 *          the maxConsumption to set
	 */
	public void setMaxConsumption(int maxConsumption) {
		this.maxConsumption = maxConsumption;
	}

	/** @return the consumption view */
	public PowerView getConsumptions() {
		return consumptionView;
	}

	/**
	 * @param consumptions
	 *          the consumptions to set
	 */
	public void setConsumptions(PowerView consumptions) {
		consumptionView = consumptions;
	}

	public Set<Node> getNodes() {
		return nodes;
	}

	public Stream<Node> getNodes(IConfiguration c) {
		if (nodes == null || nodes.isEmpty())
			return c.getNodes();
		else
			return nodes.stream().filter(c::hasNode);
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		getNodes(core.c()).forEach(n -> {
			try {
				consumptionView.getPower(n).updateUpperBound(getMaxConsumption(), Cause.Null);
			} catch (ContradictionException e) {
				throw new UnsupportedOperationException();
			}
		});
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		return !getNodes(cfg)
				.filter(
						n -> (consumptionView.getPowerData().getConsumption(cfg, consumptionView.getSpecs(), n) > maxConsumption))
				.findAny().isPresent();
	}

	@Override
	public boolean isSatisfied(ReconfigurationResult plan) {
		return isSatisfied(plan.getDestination());
	}
}
