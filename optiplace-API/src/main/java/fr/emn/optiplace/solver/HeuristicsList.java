/**
 *
 */
package fr.emn.optiplace.solver;

import java.lang.reflect.Array;
import java.util.Arrays;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.Variable;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class HeuristicsList<T extends Variable> extends AbstractStrategy<T> {

    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HeuristicsList.class);

    private final ActivatedHeuristic<? extends T>[] list;
    boolean propagated = false;

    @SafeVarargs
    @SuppressWarnings("unchecked")
    protected static <V extends Variable> V[] concatVars(ActivatedHeuristic<V>... list) {
	if (list == null) {
	    return null;
	}
	int length = Arrays.asList(list).stream().mapToInt(ah -> ah.vars.length).sum();
	if (length == 0) {
	    return null;
	}
	V[] ret = (V[]) Array.newInstance(list[0].vars.getClass().getComponentType(), length);

	int copied = 0;
	for (ActivatedHeuristic<V> ah : list) {
	    V[] arr = ah.vars;
	    System.arraycopy(arr, 0, ret, copied, arr.length);
	    copied += arr.length;
	}
	return ret;
    }

    @SafeVarargs
    public HeuristicsList(Solver s, ActivatedHeuristic<T>... list) {
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
    public Decision<T> getDecision() {
	System.err.println("computing decision for " + this);
	if (!propagated) {
	    for (ActivatedHeuristic<? extends T> element : list) {
		Propagator<? extends Variable> p = element.getPropagator();
		solver.getEngine().dynamicAddition(new Constraint("" + p, p), true);
		try {
		    p.propagate(0);
		} catch (ContradictionException e) {
		    logger.warn("", e);
		}
	    }
	    propagated = true;
	    System.err.println("propagation done");
	}
	for (ActivatedHeuristic<? extends T> ah : list) {
	    if (ah.isActivated()) {
		Decision<T> d = (Decision<T>) ah.getDecision();
		if (d != null) {
		    System.err.println("heuristic " + ah + " activated and chose " + d);
		    return d;
		} else {
		    System.err.println("heuristic " + ah + " activated and returned null");
		}
	    } else {
		System.err.println(" " + ah + " not activated");
	    }
	}
	System.err.println("no heuristic available, returning null");
	for (ActivatedHeuristic<? extends T> element : list) {
	    solver.getEngine().desactivatePropagator(element.getPropagator());
	}
	System.err.println("unregister done");
	return null;
    }
}
