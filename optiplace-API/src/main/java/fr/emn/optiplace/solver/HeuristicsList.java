/**
 *
 */

package fr.emn.optiplace.solver;

import java.util.Arrays;
import java.util.List;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;


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
		if (list == null) { return null; }
		int length = Arrays.asList(list).stream().mapToInt(ah -> ah.getVariables().length).sum();
		if (length == 0) { return null; }
		Variable[] ret = new Variable[length];

		int copied = 0;
		for (ActivatedHeuristic<?> ah : list) {
			Variable[] arr = ah.getVariables();
			System.arraycopy(arr, 0, ret, copied, arr.length);
			copied += arr.length;
		}
		return ret;
	}

	private final ActivatedHeuristic<?>[] leaders;
	boolean inserted = false;

	protected boolean logActivated = false;

	public void setLogActivated(boolean log) {
		logActivated = log;
	}

	@SafeVarargs
	public HeuristicsList(Solver s, ActivatedHeuristic<?>... leaders) {
		super(concatVars(leaders));
		solver = s;
		this.leaders = leaders;
	}

	@Override
	public boolean init() {
		for (ActivatedHeuristic<?> a : leaders)
			if (!a.init())
				return false;
		return true;
	}

	Solver solver;

	@SuppressWarnings("unchecked")
	@Override
	public Decision<Variable> getDecision() {
		if (!inserted) {
			for (ActivatedHeuristic<? extends Variable> element : leaders) {
				element.addMonitors();
			}
			inserted = true;
		}
		for (ActivatedHeuristic<? extends Variable> ah : leaders) {
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
		// the heuristics didn't find a decision : we won't be called again by the
		// solver, so we remove the monitors
		for (ActivatedHeuristic<? extends Variable> element : leaders) {
			element.remMonitors();
		}
		inserted = false;
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + Arrays.asList(leaders);
	}

	public List<ActivatedHeuristic<? extends Variable>> getLeaders() {
		return Arrays.asList(leaders);
	}
}
