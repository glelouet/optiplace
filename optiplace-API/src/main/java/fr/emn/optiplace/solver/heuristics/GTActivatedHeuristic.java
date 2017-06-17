package fr.emn.optiplace.solver.heuristics;

import java.util.Arrays;

import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.solver.ActivatedHeuristic;

/**
 * an activated heuristic considering two variables, that ensures (like a
 * constraint) that a&lt;b<br />
 * meaning a.min&lt;b.min and a.max&lt;b.max .<br />
 * Mainly used for tests
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class GTActivatedHeuristic extends ActivatedHeuristic<IntVar> {

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
	public Decision<IntVar> getDecision() {
		if (!isActivated()) {
			throw new UnsupportedOperationException();
		}
		IntDecision e;
		if (vars[0].getLB() >= vars[1].getLB()) {// a.min >= b.min : set b.min >
			// a.min+1
			e = decisions.makeIntDecision(vars[1], DecisionOperatorFactory.makeIntReverseSplit(), vars[0].getLB() + 1);
		} else { // b.max<=a.max : set a.max < b.max -1
			e = decisions.makeIntDecision(vars[0], DecisionOperatorFactory.makeIntSplit(), vars[1].getUB() - 1);
		}
		return e;
	}

	@Override
	public String toString() {
		return "GTActivatedHeuristic" + Arrays.asList(vars);
	}
}
