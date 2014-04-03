package entropy.core.goals;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Node;
import entropy.solver.choco.ReconfigurationProblem;
import entropy.view.SearchGoal;
import entropy.view.SearchHeuristic;

public class LoadBalancingMinMaxCost implements SearchGoal {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(LoadBalancingMinMaxCost.class);

	@Override
	public IntDomainVar getObjective(ReconfigurationProblem rp) {
		ReconfigurationProblem model = rp;
		int max = 0;
		for (Node o : model.nodes()) {
			if (o.getCoreCapacity() > max) {
				max = o.getCoreCapacity();
			}
		}
		IntDomainVar ret = model.createBoundIntVar("loadBalancingCost", 0, max);
		IntDomainVar maxLoad = model.max(model.getUsedCPUs());
		IntDomainVar minLoad = model.min(model.getUsedCPUs());
		// System.err.println("max load : " + maxLoad);
		// System.err.println("min load : " + minLoad);
		// ret = maxload-minload. Use plus untill
		// IntDomainVar ReconfigurationProblem.minus(IntDomainVar, IntDomainVar)
		// is implemented
		model.post(model.eq(model.plus(ret, minLoad), maxLoad));
		return ret;
	}

	public static final LoadBalancingMinMaxCost INSTANCE = new LoadBalancingMinMaxCost();

	@Override
	public SearchHeuristic[] getHeuristics() {
		return null;
	}
}
