/**
 *
 */
package fr.emn.optiplace.solver;

import java.util.Arrays;

import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.Variable;

/**
 * An {@link AbstractStrategy} which contains a list of
 * {@link ActivatedHeuristic} . It computes a decision by returning the first
 * decision not null returned by its activated heuristics.
 * <p>
 * on first call to {@link #getDecision()}, all activatedHeuristics' propagators
 * are added to the problem
 * </p>
 * <p>
 * When all the activatedheuristics returned null( or were not activated), this
 * heuristic is deactivated by the solver ; so all the propagators previously
 * added are now removed from the solver
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class HeuristicsList extends AbstractStrategy<Variable> {

    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HeuristicsList.class);

    @SafeVarargs
    protected static Variable[] concatVars(ActivatedHeuristic<? extends Variable>... list) {
	if (list == null) {
	    return null;
	}
	int length = Arrays.asList(list).stream().mapToInt(ah -> ah.vars.length).sum();
	if (length == 0) {
	    return null;
	}
	Variable[] ret = new Variable[length];

	int copied = 0;
	for (ActivatedHeuristic<?> ah : list) {
	    Variable[] arr = ah.vars;
	    System.arraycopy(arr, 0, ret, copied, arr.length);
	    copied += arr.length;
	}
	return ret;
    }

    private final ActivatedHeuristic<?>[] list;
    boolean inserted = false;

    protected boolean logActivated = false;

    public void setLogActivated(boolean log) {
	logActivated = log;
    }

    @SafeVarargs
    public HeuristicsList(Solver s, ActivatedHeuristic<?>... list) {
	super(concatVars(list));
	this.solver = s;
	this.list = list;
    }

    @Override
    public void init() throws ContradictionException {
    }

    Solver solver;

    @SuppressWarnings("unchecked")
    @Override
    public Decision<Variable> getDecision() {
	if (!inserted) {
	    for (ActivatedHeuristic<? extends Variable> element : list) {
		element.addMonitors();
	    }
	    inserted = true;
	}
	for (ActivatedHeuristic<? extends Variable> ah : list) {
	    if (ah.isActivated()) {
		Decision<Variable> d = (Decision<Variable>) ah.getDecision();
		if (d != null) {
		    if (logActivated) {
			logger.debug("activated heuristic " + ah + " returned decision " + d);
		    }
		    return d;
		}
	    }
	}
	// no good decision : we won't be called again by the solver, so we
	// remove the propagators
	for (ActivatedHeuristic<? extends Variable> element : list) {
	    element.remMonitors();
	}
	inserted = false;
	return null;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + Arrays.asList(list);
    }
}
