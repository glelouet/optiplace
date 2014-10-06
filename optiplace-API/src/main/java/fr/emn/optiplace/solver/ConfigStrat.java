/**
 *
 */
package fr.emn.optiplace.solver;

import java.util.ArrayList;

import solver.search.loop.monitors.ISearchMonitor;
import fr.emn.optiplace.solver.choco.ChocoResourcePacker;

/**
 * How to create and explore a problems.
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

	private int chocoLoggingDepth = -1;

	private ArrayList<ISearchMonitor> displayers = new ArrayList<ISearchMonitor>();

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
	 * @return the chocoLoggingDepth
	 */
	public int getChocoLoggingDepth() {
		return chocoLoggingDepth;
	}

	/**
	 * @param chocoLoggingDepth
	 *            the chocoLoggingDepth to set
	 */
	public void setChocoLoggingDepth(int chocoLoggingDepth) {
		this.chocoLoggingDepth = chocoLoggingDepth;
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

}
