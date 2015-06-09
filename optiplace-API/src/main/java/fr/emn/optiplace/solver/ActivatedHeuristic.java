/**
 *
 */
package fr.emn.optiplace.solver;

import org.chocosolver.memory.IStateBool;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.decision.fast.FastDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.PoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory.getLogger(ActivatedHeuristic.class);

	private static final long serialVersionUID = 1L;

	protected static PoolManager<FastDecision> manager = new PoolManager<>();

	protected FastDecision getFastDecision() {
		FastDecision e = manager.getE();
		if (e == null) {
			e = new FastDecision(manager);
		}
		return e;
	}

	// /////////////////////////////////////////////////////////////////

	private final IStateBool activated;

	/** set to true when the observed variables are modified */
	private final IStateBool dirty;

	protected void dirty() {
		this.dirty.set(true);;
	}

	public boolean isActivated() {
		if (dirty.get()) {
			activated.set(checkActivated());
			dirty.set(false);;
		}
		return activated.get();
	}

	/** check whether the variables of the heuristic can be branched on */
	abstract protected boolean checkActivated();

	protected IVariableMonitor<Variable> monitor = new IVariableMonitor<Variable>()
	{

		private static final long serialVersionUID = 1L;

		@Override
		public void onUpdate(Variable var, IEventType evt) throws ContradictionException {
			dirty();
		}
	};

	protected Variable[] observed;

	public void addMonitors() {
		for (Variable v : observed) {
			v.addMonitor(monitor);
		}
	}

	public void remMonitors() {
		try {
			for (Variable v : observed) {
				v.removeMonitor(monitor);
			}
		} catch (UnsupportedOperationException e) {
			// do nothing.
		}
	}

	/**
	 *
	 * @param decisionVars
	 *          The variables of the problem on which to make decisions
	 * @param observedVars
	 *          the Variables of the problem which help decide when to branch.
	 */
	public ActivatedHeuristic(T[] decisionVars, Variable[] observedVars) {
		super(decisionVars);
		this.observed = observedVars;
		Variable var = null;
		if (decisionVars == null || decisionVars.length == 0) {
			var = observedVars[0];
		} else {
			var = decisionVars[0];
		}
		activated = var.getSolver().getEnvironment().makeBool(false);
		dirty = var.getSolver().getEnvironment().makeBool(true);
	}

	public ActivatedHeuristic(T[] vars) {
		this(vars, vars);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
