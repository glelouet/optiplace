/**
 *
 */
package entropy;

import entropy.solver.PlanException;

/**
 * <p>
 * Main process to solve a problem. <el>
 * <li>specify the {@link #center} to work on</li>
 * <li>specify the {@link #strat} to have searching strategy</li>
 * <li>call {@link #solve()}</li> </el>
 * </p>
 * <p>
 * The internal methods are public available to let developers test or debug
 * easily.
 * </p>
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public abstract class EntropyProcess {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(EntropyProcess.class);

	public void solve() {
		try {
			makeProblem();
			configLogging();
			configSearch();
			makeSearch();
			extractData();
		} catch (Exception e) {
			logger.warn("", e);
		}
	}

	public abstract void configLogging();

	public abstract void makeProblem() throws PlanException;

	public abstract void configSearch();

	public abstract void makeSearch();

	public abstract void extractData();

	protected BaseCenter center = new BaseCenter();

	protected ConfigStrat strat = new ConfigStrat();

	protected DeducedTarget target = new DeducedTarget();

	/**
	 * @return the center
	 */
	public BaseCenter getCenter() {
		return center;
	}

	public void center(BaseCenter center) {
		this.center = center;
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
