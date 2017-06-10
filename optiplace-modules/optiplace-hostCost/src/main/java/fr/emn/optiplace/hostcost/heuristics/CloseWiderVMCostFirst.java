package fr.emn.optiplace.hostcost.heuristics;

import java.util.stream.IntStream;

import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.hostcost.HostCostView;

/**
 * a heuristic which
 * <ol>
 * <li>select the vm with higher difference in location cost max-min</li>
 * <li>affect the cost of this vm to the lowest value.</li>
 * </ol>
 *
 * @author Guillaume Le Louët
 *
 */
public class CloseWiderVMCostFirst {
	public static IntStrategy makeHeuristic(HostCostView view) {
		// the variable we work on are the cost variables of the vms (retrieved by
		// view::getCost)
		IntVar[] scope = IntStream.range(0, view.b.vms().length).mapToObj(view::getCost).toArray(IntVar[]::new);
		// we select the variable with max domain size
		VariableSelector<IntVar> varSelector = ISF.maxDomainSize_var_selector();
		// we attribute the lowest value to this variable.
		IntValueSelector valSelector = ISF.min_value_selector();
		return new IntStrategy(scope, varSelector, valSelector);
	}
}
