/**
 *
 */
package fr.emn.optiplace.solver;

import java.util.Arrays;

import org.chocosolver.memory.IStateBool;
import org.chocosolver.solver.search.strategy.decision.DecisionMaker;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * an heuristics which makes decisions only if a certain criterion is met.
 * </p>
 * <p>
 * It observes a list of variables used in the criterion.<br />
 * Its {@link #isActivated()} must be called when exploring to check if this
 * heuristic can make a decision.
 * </p>
 * <p>
 * The observed variables are monitored by {@link #monitor}, a final variable
 * which simply calls {@link #dirty()} when a variable is modified. This reduces
 * the CPU cost of calculating the {@link #activated} value each time one
 * variable is modified.
 * </p>
 * <p>
 * Internally, the {@link #activated} flag is stored and returned by
 * {@link #isActivated()}, but when an observed variable is modified the
 * {@link #dirty} flag is set to require re-computation of this
 * {@link #activated} value. <br />
 * The {@link #dirty} and {@link #activated} flags are stored as
 * {@link IStateBool} to ensure backtracking will restore the correct values.
 * </p>
 *
 * @param <T>
 *          The type of variables to make decisions on (eg IntVar or SetVar)
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public abstract class ActivatedHeuristic<T extends Variable> extends AbstractStrategy<T> {

	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory.getLogger(ActivatedHeuristic.class);

	protected DecisionMaker decisions = new DecisionMaker();

	/**
	 * set {@link #checkActivated()} when {@link #isActivated()} is called and
	 * {@link #dirty} is true
	 */
	private final IStateBool activated;

	/**
	 * set to true when the observed variables are modified
	 */
	private final IStateBool dirty;

	/**
	 * an observed variable is modified
	 */
	protected final void dirty() {
		this.dirty.set(true);
	}

	/**
	 *
	 * @return can the heuristic make a decision over the deciion variables ?
	 */
	public final boolean isActivated() {
		if (dirty.get()) {
			activated.set(checkActivated());
			dirty.set(false);
		}
		return activated.get();
	}

	/**
	 * check whether the heuristic can make a decision
	 */
	abstract protected boolean checkActivated();

	/**
	 * the monitor on the observed variables.
	 */
	protected final IVariableMonitor<Variable> monitor = (v, e) -> dirty();

	protected Variable[] observed;

	public void addMonitors() {
		for (Variable v : observed) {
			v.addMonitor(monitor);
		}
	}

	public void remMonitors() {
		for (Variable v : observed) {
			v.removeMonitor(monitor);
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
		assert var != null : "no variable to get the solver from";
		activated = var.getModel().getEnvironment().makeBool(false);
		dirty = var.getModel().getEnvironment().makeBool(true);
	}

	/**
	 * redirects to {@link #ActivatedHeuristic(Variable[], Variable[])} on vars,
	 * vars
	 *
	 * @param vars
	 *          the variables to branch on and observe
	 */
	@SafeVarargs
	public ActivatedHeuristic(T... vars) {
		this(vars, vars);
	}

	protected String toString = null;

	protected String makeToString() {
		return getClass().getSimpleName() + Arrays.asList(getVariables());
	}

	@Override
	public String toString() {
		if (toString == null) {
			toString = makeToString();
		}
		return toString;
	}
}
