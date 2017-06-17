package fr.emn.optiplace.core.heuristics;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;

public class NoWaitingHeuristic {

	private static final Logger logger = LoggerFactory.getLogger(NoWaitingHeuristic.class);

	public static List<AbstractStrategy<? extends Variable>> getHeuristics(IReconfigurationProblem rp) {
		IntVar[] vars = rp.c().getWaitings().map(rp::isWaiting).toArray(IntVar[]::new);
		List<AbstractStrategy<? extends Variable>> ret = new ArrayList<>();
		if (vars != null && vars.length > 0) {
			ret.add(SearchGoal.makeAssignHeuristic(NoWaitingHeuristic.class.getSimpleName() + ".IntVar",
					new InputOrder<>(rp.getModel()),
					new IntDomainMin(), vars));
		} else {
			logger.debug("no VM waiting, can't make heuristic ");
		}
		return ret;
	}

}
