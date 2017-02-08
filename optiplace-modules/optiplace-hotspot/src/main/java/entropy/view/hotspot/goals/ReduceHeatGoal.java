package entropy.view.hotspot.goals;

import org.chocosolver.solver.variables.IntVar;

import entropy.view.hotspot.HotSpotView;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;

/**
 *
 * @author Guillaume Le Louët 2016 [ guillaume.lelouet@gmail.com ]
 *
 */
public class ReduceHeatGoal implements SearchGoal{

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReduceHeatGoal.class);

	private HotSpotView parent;

	public ReduceHeatGoal(HotSpotView parent) {
		this.parent = parent;
	}

	@Override
	public IntVar getObjective(IReconfigurationProblem rp) {
		return parent.maxRearTemperature();
	}

}
