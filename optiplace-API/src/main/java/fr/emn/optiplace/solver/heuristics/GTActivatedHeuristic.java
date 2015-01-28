package fr.emn.optiplace.solver.heuristics;

import java.util.Arrays;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.fast.FastDecision;
import org.chocosolver.solver.variables.IntVar;
import fr.emn.optiplace.solver.ActivatedHeuristic;

/**
 * an activated heuristic which ensures (like a constraint) that a&lt;b<br />
 * meaning a.min&lt;b.min and a.max&lt;b.max .
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class GTActivatedHeuristic extends ActivatedHeuristic<IntVar> {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GTActivatedHeuristic.class);

    /**
     * @param variables
     * @param observed
     */
    protected GTActivatedHeuristic(IntVar a, IntVar b) {
	super(new IntVar[] { a, b }, new IntVar[] { a, b });
    }

    @Override
    protected boolean checkActivated() {
	return vars[0].getLB() >= vars[1].getLB() || vars[0].getUB() >= vars[1].getUB();
    }

    @Override
    public void init() throws ContradictionException {
    }

    @Override
    public Decision<IntVar> getDecision() {
	if (!isActivated()) {
	    throw new UnsupportedOperationException();
	}
	FastDecision e = getFastDecision();
	if (vars[0].getLB() >= vars[1].getLB()) {// a.min >= b.min : set b.min >
	    // a.min+1
	    e.set(vars[1], vars[0].getLB() + 1, DecisionOperator.int_reverse_split);
	} else { // b.max<=a.max : set a.max < b.max -1
	    e.set(vars[0], vars[1].getUB() - 1, DecisionOperator.int_split);
	}
	return e;
    }

    @Override
    public String toString() {
	return "GTActivatedHeuristic" + Arrays.asList(vars);
    }
}
