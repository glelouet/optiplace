package entropy.view.scheduling.objectives;

import java.util.ArrayList;
import java.util.List;

import choco.Choco;
import org.chocosolver.solver.constraints.SConstraint;
import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.actionModel.ActionModel;
import entropy.view.scheduling.actionModel.ActionModels;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.SearchHeuristic;

public class PackingCost implements SearchGoal {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PackingCost.class);

	private SchedulingView view = null;

	public PackingCost(SchedulingView view) {
		this.view = view;
	}

	@Override
	public IntVar getObjective(ReconfigurationProblem rp) {
		ReconfigurationProblem model = rp;
		IntVar globalCost = model.createBoundIntVar("globalCost", 0,
				Choco.MAX_UPPER_BOUND);
		List<ActionModel> allActions = new ArrayList<ActionModel>();
		allActions.addAll(view.getVirtualMachineActions());
		allActions.addAll(view.getNodeMachineActions());
		IntVar[] allCosts = ActionModels.extractCosts(allActions);
		List<IntVar> varCosts = new ArrayList<IntVar>();
		for (IntVar c : allCosts) {
			if (c.isInstantiated() && c.getVal() == 0) {
			} else {
				varCosts.add(c);
			}
		}
		IntVar[] costs = varCosts.toArray(new IntVar[varCosts
				.size()]);
		// model.post(model.eq(globalCost,
		// /*model.sum(costs)*/explodedSum(model,
		// costs, 200, true)));
		// BUG check if this is good. explodedsum was used before.
		SConstraint<?> cs = model.eq(globalCost, model.sum(costs));
		model.getCostConstraints().add(cs);
		// model.post(cs);
		return globalCost;
	}

	@Override
	public SearchHeuristic[] getHeuristics() {
		return null;
	}
}
