package fr.emn.optiplace.view;

import java.util.Collections;
import java.util.List;

import solver.search.strategy.ISF;
import solver.search.strategy.selectors.IntValueSelector;
import solver.search.strategy.selectors.SetValueSelector;
import solver.search.strategy.selectors.VariableSelector;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.IntStrategy;
import solver.search.strategy.strategy.SetStrategy;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import fr.emn.optiplace.solver.ActivatedHeuristic;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * Describes an objective variable to reduce. Creates a variable in a model and
 * inject constraints linking this variable to its formal representation.<br />
 * can provide heuristics that MAY be used IFF the search request does not
 * provide its own heuristics.
 *
 * @author Guillaume Le Louët
 */
public interface SearchGoal {

    /**
     * Produces a formal expression of the objective variable in a model
     *
     * @param rp
     *            the solver to add constraints into and extract global cost
     *            from
     * @return the expression of the added formal cost of the model
     */
    public IntVar getObjective(IReconfigurationProblem rp);

    /**
     * @return the heuristics associated with the objective to reduce. Can be
     *         null, or an empty array, if no heuristic is interesting. Default
     *         implementation is null
     */
    default List<AbstractStrategy<? extends Variable>> getHeuristics(IReconfigurationProblem rp) {
	return Collections.emptyList();
    }

    /**
     * @return the heuristics associated with the objective to reduce. Can be
     *         null, or an empty array, if no heuristic is interesting. Default
     *         implementation is null
     */
    default List<ActivatedHeuristic<? extends Variable>> getActivatedHeuristics(IReconfigurationProblem rp) {
	return Collections.emptyList();
    }

    @SuppressWarnings("serial")
    public static AbstractStrategy<? extends Variable> makeAssignHeuristic(String name,
	    VariableSelector<IntVar> varSelector,
	    IntValueSelector valSelector, IntVar... vars) {
	return new IntStrategy(vars, varSelector, valSelector, ISF.assign()) {
	    @Override
	    public String toString() {
		return name;
	    };
	};
    }

    @SuppressWarnings("serial")
    public static SetStrategy makeAssignHeuristic(String name, VariableSelector<SetVar> varS, SetValueSelector valS,
	    boolean enforceFirst, SetVar... sets) {
	return new SetStrategy(sets, varS, valS, enforceFirst) {
	    @Override
	    public String toString() {
		return name;
	    };
	};
    }
}
