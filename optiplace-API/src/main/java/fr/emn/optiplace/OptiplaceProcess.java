/**
 *
 */

package fr.emn.optiplace;

import java.util.ArrayList;
import java.util.List;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.solver.ConfigStrat;
import fr.emn.optiplace.view.View;


/**
 * <p>
 * Main process to solve a problem. This class should be created by the
 * optiplaceServer each time it has a problem to solve. to use it you need to
 * <el>
 * <li>specify the {@link #center} to work on, that is, the source configuration
 * and the views to integrate.</li>
 * <li>specify the {@link #strat} to have searching parameters, passed to the
 * choco solver.</li>
 * <li>call {@link #solve()}</li> </el>
 * </p>
 * <p>
 * The internal methods are public available to let developers test or debug
 * easily.
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public abstract class OptiplaceProcess {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OptiplaceProcess.class);

	public void solve() {
		try {
			makeProblem();
			configLogging();
			configSearch();
			makeSearch();
			extractData();
		}
		catch (Exception e) {
			logger.warn("", e);
		}
	}

	/** generate the elements of the problem in the solver */
	protected abstract void makeProblem();

	/** set the logging according to the problem solver used */
	protected abstract void configLogging();

	/** prepare the heuristics and the objectives */
	protected abstract void configSearch();

	/** actually make the solver go for the solution(s) */
	protected abstract void makeSearch();

	/** extract the interesting data from the solver result */
	protected abstract void extractData();

	protected Configuration sourceConfig = null;

	protected List<View> views = new ArrayList<>();

	protected ConfigStrat strat = new ConfigStrat();

	protected DeducedTarget target = new DeducedTarget();

	public Configuration source() {
		return sourceConfig;
	}

	public void source(Configuration source) {
		sourceConfig = source;
	}

	public List<View> views() {
		return views;
	}

	public void views(List<View> views) {
		this.views = views;
	}

	/**
	 * @return the strat
	 */
	public ConfigStrat getStrat() {
		return strat;
	}

	public void strat(ConfigStrat strat) {
		this.strat = strat;
	}

	/**
	 * @return the target
	 */
	public DeducedTarget getTarget() {
		return target;
	}

	public void target(DeducedTarget target) {
		this.target = target;
	}
}
