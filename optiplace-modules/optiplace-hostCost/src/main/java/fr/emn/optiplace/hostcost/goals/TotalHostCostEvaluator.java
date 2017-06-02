package fr.emn.optiplace.hostcost.goals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.hostcost.CostData;
import fr.emn.optiplace.hostcost.HostCostView;
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
		IntVar[] nbhosteds = orderLocationsByCost().map(rp::nbVMsOn).toArray(IntVar[]::new);
		IntStrategy strat = ISF.lexico_LB(nbhosteds);
		return Arrays.asList(strat);
	}

	public Stream<VMLocation> orderLocationsByCost() {
		// sort all hoster with a non-null cost
		CostData data = parent.getCostData();
		IConfiguration cfg = parent.getProblem().getSourceConfiguration();
		return cfg.getLocations().filter(l -> data.getCost(l, cfg.getSite(l)) > 0)
				.sorted((l1, l2) -> data.getCost(l2, cfg.getSite(l2)) - data.getCost(l1, cfg.getSite(l1)));
	}

}
