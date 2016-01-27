/**
 *
 */

package fr.emn.optiplace;

import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;


/**
 * The result of an optiplace resolution. Consists in the original source
 * configuration as well as the target configuration, solving data, a graph of
 * actions generated by the solver to transform the source configuration to the
 * dest configuration. Also keeps the ReconfigurationProblem in memory
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class DeducedTarget {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DeducedTarget.class);

	private IConfiguration destination;

	private final ActionGraph actions = new ActionGraph();

	private long buildTime = -1;

	private long configTime = -1;

	private long searchTime = -1;

	private IReconfigurationProblem problem;

	private int objective = -1;

	private long searchNodes = -1;

	private long searchBacktracks = -1;

	private long searchSolutions = -1;

	/**
	 * @return the destination
	 */
	public IConfiguration getDestination() {
		return destination;
	}

	/**
	 * @return the list of actions deduced.
	 */
	public ActionGraph getActions() {
		return actions;
	}

	/**
	 * @param destination
	 *          the destination to set
	 */
	public void setDestination(IConfiguration destination) {
		this.destination = destination;
	}

	/**
	 * @return the buildTime in nanosecond
	 */
	public long getBuildTime() {
		return buildTime;
	}

	/**
	 * @param buildTime
	 *          the buildTime to set
	 */
	public void setBuildTime(long buildTime) {
		this.buildTime = buildTime;
	}

	/**
	 * @return the configTime
	 */
	public long getConfigTime() {
		return configTime;
	}

	/**
	 * @param configTime
	 *          the configTime to set
	 */
	public void setConfigTime(long configTime) {
		this.configTime = configTime;
	}

	/**
	 * @return the time to search for solution, in nanosecond
	 */
	public long getSearchTime() {
		return searchTime;
	}

	/**
	 * @param searchTime
	 *          the searchTime to set
	 */
	public void setSearchTime(long searchTime) {
		this.searchTime = searchTime;
	}

	/**
	 *
	 * @return the total time to build the problem, configure the search and
	 *         launch the search.
	 */
	public long getTotalTime() {
		return searchTime + configTime + buildTime;
	}

	/**
	 * @return the problem
	 */
	public IReconfigurationProblem getProblem() {
		return problem;
	}

	/**
	 * @param problem
	 *          the problem to set
	 */
	public void setProblem(IReconfigurationProblem problem) {
		this.problem = problem;
	}

	/**
	 * @return the objective
	 */
	public int getObjective() {
		return objective;
	}

	/**
	 * @param objective
	 *          the objective to set
	 */
	public void setObjective(int objective) {
		this.objective = objective;
	}

	/**
	 * @return the searchNodes
	 */
	public long getSearchNodes() {
		return searchNodes;
	}

	/**
	 * @param l
	 *          the searchNodes to set
	 */
	public void setSearchNodes(long l) {
		searchNodes = l;
	}

	/**
	 * @param l
	 *          the searchBacktracks to set
	 */
	public void setSearchBacktracks(long l) {
		searchBacktracks = l;
	}

	/**
	 * @return the searchBacktracks
	 */
	public long getSearchBacktracks() {
		return searchBacktracks;
	}

	/**
	 * @return the searchSolutions
	 */
	public long getSearchSolutions() {
		return searchSolutions;
	}

	/**
	 * @param searchSolutions
	 *          the searchSolutions to set
	 */
	public void setSearchSolutions(long searchSolutions) {
		this.searchSolutions = searchSolutions;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("times : config=" + configTime + "ms build=" + buildTime + "ms search=" + searchTime + "ms\n");
		sb.append(
		    "stats : " + searchSolutions + " solutions, " + searchNodes + " nodes, " + searchBacktracks + " backtracks\n");
		sb.append("actions (").append(actions.nbActions()).append(") :\n").append(actions).append("\ndestination : \n")
		    .append(destination).append("\nobjective result = " + objective);
		return sb.toString();
	}
}
