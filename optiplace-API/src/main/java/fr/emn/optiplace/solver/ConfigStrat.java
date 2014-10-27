/**
 *
 */
package fr.emn.optiplace.solver;

import java.util.ArrayList;

import solver.search.loop.monitors.ISearchMonitor;
import fr.emn.optiplace.solver.choco.ChocoResourcePacker;

/**
 * How to create and explore a problems.<br />
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
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ConfigStrat.class);

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
	 *            the maxSearchTime to set
	 */
	public void setMaxSearchTime(long maxSearchTime) {
		this.maxSearchTime = maxSearchTime;
	}

	/**
	 * @return the reducer
	 */
	public ObjectiveReducer getReducer() {
		return reducer;
	}

	/**
	 * @param reducer
	 *            the reducer to set
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
	 *            the packer to set
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
	 *            the displayers to set
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
	 * should we log the choices ? if true implies setLogBasics(true) the
	 * logChoices to set.
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
	 * should we log the initialization and end data of the solver ? If false
	 * imples setLogSolutions(false) and setLogChoices(false) the logBasics to set
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
	 * should we log the solutions found ? if true implies setLogBasics(true) the
	 * logSolutions to set
	 */
	public void setLogSolutions(boolean logSolutions) {
		this.logSolutions = logSolutions;
		if (logSolutions) {
			setLogBasics(true);
		}
	}

}
