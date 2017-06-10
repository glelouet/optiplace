package fr.emn.optiplace.hostcost.goals;

import java.util.Arrays;
import java.util.List;

import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.hostcost.HostCostView;
import fr.emn.optiplace.hostcost.heuristics.CloseWiderVMCostFirst;
import fr.emn.optiplace.hostcost.heuristics.PreventExpensiveHosts;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;

public class TotalHostCostEvaluator implements SearchGoal {

	HostCostView parent;

	public TotalHostCostEvaluator(HostCostView v) {
		parent = v;
	}

	@Override
	public IntVar getObjective(IReconfigurationProblem rp) {
		return parent.getTotalCost();
	}

	@Override
	public List<AbstractStrategy<? extends Variable>> getHeuristics(IReconfigurationProblem rp) {
		return parent.hostcostHeuristicPrevent ? Arrays.asList(PreventExpensiveHosts.makeHeuristic(parent))
				: Arrays.asList(CloseWiderVMCostFirst.makeHeuristic(parent));
	}

}
