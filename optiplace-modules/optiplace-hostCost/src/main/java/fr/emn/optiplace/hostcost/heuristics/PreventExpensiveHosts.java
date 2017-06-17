package fr.emn.optiplace.hostcost.heuristics;

import java.util.Comparator;
import java.util.stream.Stream;

import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.hostcost.CostData;
import fr.emn.optiplace.hostcost.HostCostView;

/**
 * a heuristic that set the number of vm on expensive locations to the lowest
 * possible values.<br />
 *
 * this is a bad heuristic, only good to show how to write heuristics.
 *
 * @author Guillaume Le Louët
 *
 */
public class PreventExpensiveHosts {

	public static Stream<VMLocation> orderLocationsByCost(HostCostView view) {
		// sort all hoster with a non-null cost
		CostData data = view.getCostData();
		IConfiguration cfg = view.getProblem().getSourceConfiguration();
		Comparator<VMLocation> c = (l1, l2) -> {
			int ret = data.getCost(l2, cfg.getSite(l2)) - data.getCost(l1, cfg.getSite(l1));
			if (ret == 0) {
				ret = (int) (cfg.nbHosted(l1) - cfg.nbHosted(l2));
			}
			return ret;
		};
		return cfg.getLocations().filter(l -> data.getCost(l, cfg.getSite(l)) > 0).sorted(c);
	}

	public static IntStrategy makeHeuristic(HostCostView view) {
		IntVar[] nbhosteds = orderLocationsByCost(view).map(view.pb::nbVMsOn).toArray(IntVar[]::new);
		return Search.inputOrderLBSearch(nbhosteds);
	}
}
