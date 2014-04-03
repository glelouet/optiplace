/**
 *
 */
package entropy;

import java.util.ArrayList;

import choco.kernel.common.logging.Verbosity;
import choco.kernel.solver.search.ISolutionDisplay;
import entropy.solver.ObjectiveReducer;
import entropy.solver.choco.ChocoResourcePacker;
import entropy.view.SearchGoal;
import entropy.view.SearchHeuristic;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class ConfigStrat {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ConfigStrat.class);

	private SearchGoal goal;

	private long maxSearchTime;

	private ArrayList<SearchHeuristic> heuristics = new ArrayList<SearchHeuristic>();

	private ObjectiveReducer reducer = null;

	private ChocoResourcePacker packer = null;

	private Verbosity chocoVerbosity = null;

	private int chocoLoggingDepth = -1;

	private ArrayList<ISolutionDisplay> displayers = new ArrayList<ISolutionDisplay>();

	/**
	 * @return the goal
	 */
	public SearchGoal getGoal() {
		return goal;
	}

	/**
	 * @param goal
	 *            the goal to set
	 */
	public void setGoal(SearchGoal goal) {
		this.goal = goal;
	}

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
	 * @return the heuristics
	 */
	public ArrayList<SearchHeuristic> getHeuristics() {
		return heuristics;
	}

	/**
	 * @param heuristics
	 *            the heuristics to set
	 */
	public void setHeuristics(ArrayList<SearchHeuristic> heuristics) {
		this.heuristics = heuristics;
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
	 * @return the chocoVerbosity
	 */
	public Verbosity getChocoVerbosity() {
		return chocoVerbosity;
	}

	/**
	 * @param chocoVerbosity
	 *            the chocoVerbosity to set
	 */
	public void setChocoVerbosity(Verbosity chocoVerbosity) {
		this.chocoVerbosity = chocoVerbosity;
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
	public ArrayList<ISolutionDisplay> getDisplayers() {
		return displayers;
	}

	/**
	 * @param displayers
	 *            the displayers to set
	 */
	public void setDisplayers(ArrayList<ISolutionDisplay> displayers) {
		this.displayers = displayers;
	}

}
