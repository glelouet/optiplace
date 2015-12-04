package fr.emn.optiplace.hostcost.goals;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.hostcost.HostCostView;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;

public class TotalHostCostEvaluator implements SearchGoal {

	HostCostView parent;

	public TotalHostCostEvaluator(HostCostView v) {
		this.parent = v;
	}

	@Override
	public IntVar getObjective(IReconfigurationProblem rp) {
		return parent.getTotalCost();
	}

}
