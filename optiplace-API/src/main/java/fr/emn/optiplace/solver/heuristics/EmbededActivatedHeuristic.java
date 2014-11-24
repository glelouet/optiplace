package fr.emn.optiplace.solver.heuristics;

import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.Variable;
import fr.emn.optiplace.solver.ActivatedHeuristic;

/**
 * embed an AbstractStrategy in an activatedHeuristic. The activatedHeuristic is
 * activated as long as the embedded strategy did not return null to
 * getDecision();
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class EmbededActivatedHeuristic<T extends Variable> extends ActivatedHeuristic<T> {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EmbededActivatedHeuristic.class);

    protected final AbstractStrategy<T> strat;

    /**
     * @param variables
     * @param observed
     */
    public EmbededActivatedHeuristic(AbstractStrategy<T> strat) {
	super(strat.vars, new Variable[0]);
	activated = true;
	this.strat = strat;
    }

    @Override
    protected void checkActivated() {
	// do nothing, always activated if activated before.
    }

    @Override
    public void init() throws ContradictionException {
    }

    @Override
    public Decision<T> getDecision() {
	Decision<T> e = strat.getDecision();
	if (e == null) {
	    activated = false;
	}
	return e;
    }
}
