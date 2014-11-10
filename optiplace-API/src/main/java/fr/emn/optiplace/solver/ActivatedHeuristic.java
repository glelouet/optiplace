/**
 *
 */
package fr.emn.optiplace.solver;

import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.Variable;
import util.ESat;

/**
 * <p>
 * an heuristics which can make decisions after it has ended up giving decisions
 * on a previous pass.
 * </p>
 * <p>
 * it contains a propagator which must be added in the solver ; then call the
 * {@link #isActivated()} to know if this heuristic can make a decision.
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public abstract class ActivatedHeuristic<T extends Variable> extends AbstractStrategy<T> {

    private static final long serialVersionUID = 1L;

    protected boolean activated = false;

    public boolean isActivated() {
	return activated;
    }

    /** check whether the variables of the heuristic can be branched on */
    abstract protected boolean checkActivated();

    Propagator<? extends Variable> propagator = new Propagator<Variable>(this.vars) {

	private static final long serialVersionUID = 1L;

	@Override
	public void propagate(int evtmask) throws ContradictionException {
	    activated = checkActivated();
	}

	@Override
	public ESat isEntailed() {
	    return ESat.FALSE;
	}
    };

    public Propagator<? extends Variable> getPropagator() {
	return propagator;
    }

    protected Variable[] observed;

    /**
     *
     * @param variables
     *            The variables of the problem on which to make decisions
     * @param observed
     *            the Variables of the problem which help decide when to branch.
     */
    protected ActivatedHeuristic(T[] variables, Variable[] observed) {
	super(variables);
	this.observed = observed;
    }

}
