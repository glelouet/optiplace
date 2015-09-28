package fr.emn.optiplace.solver.heuristics;

import org.chocosolver.memory.IStateBool;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.solver.ActivatedHeuristic;

/**
 * Bring an AbstractStrategy in an activatedHeuristic. The activatedHeuristic is
 * activated as long as the internal strategy did not return null to
 * getDecision();
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class Static2Activated<T extends Variable> extends ActivatedHeuristic<T> {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Static2Activated.class);

	protected final AbstractStrategy<T> strat;

	/**
	 * @param variables
	 * @param observed
	 */
	public Static2Activated(AbstractStrategy<T> strat) {
		super(strat.getVariables(), new Variable[0]);
		this.strat = strat;
		nullRet = strat.getVariables()[0].getSolver().getEnvironment().makeBool(false);
	}

	// set to true when the embedded heuristic returns null : we can't call it
	// again
	IStateBool nullRet;

	@Override
	protected boolean checkActivated() {
		return !nullRet.get();
	}

	@Override
	public void init() throws ContradictionException {
		strat.init();
	}

	@Override
	public Decision<T> getDecision() {
		Decision<T> e = strat.getDecision();
		if (e == null) {
			nullRet.set(true);
		}
		return e;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + strat + ")";
	}
}
