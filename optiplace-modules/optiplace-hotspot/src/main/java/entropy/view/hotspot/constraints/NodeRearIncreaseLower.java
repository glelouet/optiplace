package entropy.view.hotspot.constraints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import entropy.view.hotspot.HotSpotView;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/** @author guillaume */
public class NodeRearIncreaseLower implements Rule {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NodeRearIncreaseLower.class);

	protected int maxIncrease;

	protected HotSpotView hotspotView;

	protected Set<Node> nodes;

	public NodeRearIncreaseLower(HotSpotView view, int maxIncrease, Set<Node> nodes) {
		hotspotView = view;
		this.maxIncrease = maxIncrease;
		this.nodes = nodes;
	}

	public NodeRearIncreaseLower(HotSpotView view, int maxIncrease, Node... nodes) {
		this(view, maxIncrease, new HashSet<>(Arrays.asList(nodes)));
	}

	/** @return the maxConsumption */
	public int getMaxIncrease() {
		return maxIncrease;
	}

	/**
	 * @param maxConsumption
	 *          the maxConsumption to set
	 */
	public void setMaxIncrease(int maxIncrease) {
		this.maxIncrease = maxIncrease;
	}

	/** @return the consumptions */
	public HotSpotView getView() {
		return hotspotView;
	}

	/**
	 * @param consumptions
	 *          the consumptions to set
	 */
	public void setView(HotSpotView hotspots) {
		hotspotView = hotspots;
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		for (Node n : nodes) {
			hotspotView.post(core.getModel().arithm(hotspotView.getRearTemp(n), "<=", maxIncrease));
		}
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		PowerView consv = hotspotView.getConsumption();
		HashMap<Node, Double> cons = consv.getPowerData().getConsumptions(cfg, true);
		HashMap<String, Double> rears = hotspotView.getImpacts().impact(cons);
		for (Node n : nodes) {
			Double rear = rears.get(n.getName());
			if (rear != null && rear > maxIncrease) {
				return false;
			}
		}
		return true;
	}
}
