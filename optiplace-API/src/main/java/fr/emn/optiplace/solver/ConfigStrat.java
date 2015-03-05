/**
 *
 */

package fr.emn.optiplace.solver;

import java.util.ArrayList;

import org.chocosolver.solver.search.loop.monitors.ISearchMonitor;

import fr.emn.optiplace.solver.choco.ChocoResourcePacker;


/**
 * How to create and explore a problem.<br />
 * <p>
 * Contains parameters, such as limiting the exploration fo the solution tree,
 * using a specific packer for the resources, reducing the objective value,
 * logging the solutions and choices, or showing some events
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class ConfigStrat {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigStrat.class);

	private long maxSearchTime;

	private ObjectiveReducer reducer = null;

	private ChocoResourcePacker packer = null;

	private ArrayList<ISearchMonitor> displayers = new ArrayList<ISearchMonitor>();

	private boolean logBasics = false;

	private boolean logSolutions = false;

	private boolean logChoices = false;

	/**
	 * @return the maxSearchTime
	 */
	public long getMaxSearchTime() {
		return maxSearchTime;
	}

	/**
	 * @param maxSearchTime
	 *          the maxSearchTime to set, in ms
	 */
	public void setMaxSearchTime(long timeMS) {
		this.maxSearchTime = timeMS;
	}

	/**
	 * @return the reducer
	 */
	public ObjectiveReducer getReducer() {
		return reducer;
	}

	/**
	 * @param reducer
	 *          the reducer to set
	 */
	public void setReducer(ObjectiveReducer reducer) {
		this.reducer = reducer;
	}

	/**
	 * @return the packer
	 */
	public ChocoResourcePacker getPacker() {
		return packer;
	}

	/**
	 * @param packer
	 *          the packer to set
	 */
	public void setPacker(ChocoResourcePacker packer) {
		this.packer = packer;
	}

	/**
	 * @return the displayers
	 */
	public ArrayList<ISearchMonitor> getDisplayers() {
		return displayers;
	}

	/**
	 * @param displayers
	 *          the displayers to set
	 */
	public void setDisplayers(ArrayList<ISearchMonitor> displayers) {
		this.displayers = displayers;
	}

	/**
	 * @return the logChoices
	 */
	public boolean isLogChoices() {
		return logChoices;
	}

	/**
	 * @param logChoices
	 *          should we log the choices ? if true implies setLogBasics(true) the
	 *          logChoices to set.
	 */
	public void setLogChoices(boolean logChoices) {
		this.logChoices = logChoices;
		if (logChoices) {
			setLogBasics(true);
		}
	}

	/**
	 * @return the logBasics
	 */
	public boolean isLogBasics() {
		return logBasics;
	}

	/**
	 * @param logBasics
	 *          should we log the initialization and end data of the solver ? If
	 *          false imples setLogSolutions(false) and setLogChoices(false) the
	 *          logBasics to set
	 */
	public void setLogBasics(boolean logBasics) {
		this.logBasics = logBasics;
		if (!logBasics) {
			setLogChoices(false);
			setLogSolutions(false);
		}
	}

	/**
	 * @return the logSolutions
	 */
	public boolean isLogSolutions() {
		return logSolutions;
	}

	/**
	 * @param logSolutions
	 *          should we log the solutions found ? if true implies
	 *          setLogBasics(true) the logSolutions to set
	 */
	public void setLogSolutions(boolean logSolutions) {
		this.logSolutions = logSolutions;
		if (logSolutions) {
			setLogBasics(true);
		}
	}

	protected boolean disableCheckSource = false;

	/**
	 * Set this to true to prevent the solver from checking the correctness of the
	 * source configuration. Useful mostly in test when we know we don't want to
	 * test the source configuration (because we already know it is correct or
	 * not)<br />
	 * This source configuration is used in a first heuristic to find an "easy"
	 * solution, reducing the time to search of an initial solution, while also
	 * giving a limit to the objective value. This "source" solution is checked
	 * using an heuristic placing the VMs on their source Nodes, and then asking
	 * all the views their own FIND heuristic.
	 *
	 * @param dcs
	 *          the boolean value of whether or not we should check the
	 *          correctness of the source configuration.
	 */
	public void setDisableCheckSource(boolean dcs) {
		disableCheckSource = dcs;
	}

	/**
	 * @return return whether or not we should check the correctness of the source
	 *         configuration.
	 */
	public boolean isDisableCheckSource() {
		return disableCheckSource;
	}

	protected boolean disableOptimize = false;

	/**
	 * Set whether or no we should stop at first solution. If set to false, the
	 * solver will keep searching for the best solution, if to false it will stop
	 * after the first solution found.
	 * <p>
	 * This is different from setting the Objective parameter of the views to
	 * false, as its heuristics will be used if it is not set to null. If the
	 * objective is set to null, its heuristics are not used.
	 * </p>
	 *
	 * @param disable
	 */
	public void setDisableOptimize(boolean disable) {
		disableOptimize = disable;
	}

	public boolean isDisableOptimize() {
		return disableOptimize;
	}

	private boolean disableViewRace = false;

	/**
	 * @return the disableMutlipleGoals
	 */
	public boolean isDisableViewRace() {
		return disableViewRace;
	}

	/**
	 * set to true to prevent the solver to execute IF several views are
	 * specifying heuristics or goals, eg when the views are dynamically loaded
	 * and thus the order of loading and selection of the
	 *
	 * @param disableMutlipleGoals
	 *          the disableMutlipleGoals to set
	 */
	public void setDisableViewRace(boolean disableMutlipleGoals) {
		disableViewRace = disableMutlipleGoals;
	}

	private boolean logHeuristicsSelection = false;

	/**
	 * @return
	 */
	public boolean isLogHeuristicsSelection() {
		return logHeuristicsSelection;
	}

	/**
	 * set whether we should log (debug) the ActivatedHeuristics which makes a
	 * decision
	 *
	 * @param log
	 */
	public void setLogHeuristicsSelection(boolean log) {
		logHeuristicsSelection = log;
	}

}
