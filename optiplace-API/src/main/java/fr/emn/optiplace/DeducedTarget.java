/**
 *
 */

package fr.emn.optiplace;

import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.Configuration;
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

	private Configuration destination;

	private final ActionGraph actions = new ActionGraph();

	private long buildTime;

	private long configTime;

	private long searchTime;

	private long firstSolTime;

	private IReconfigurationProblem problem;

	private int objective;

	private long searchNodes;

	private long firstSolNodes;

	private long searchBacktracks;

	private long firstSolBacktracks;

	private long searchSolutions;

	/**
	 * @return the destination
	 */
	public Configuration getDestination() {
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
	public void setDestination(Configuration destination) {
		this.destination = destination;
	}

	/**
	 * @return the buildTime
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
	 * @return the searchTime
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
	 * @return the searchTime
	 */
	public long getFirstSolTime() {
		return firstSolTime;
	}

	/**
	 * @param searchTime
	 *          the searchTime to set
	 */
	public void setFirstSolTime(long firstSolTime) {
		this.firstSolTime = firstSolTime;
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
	 * @return the searchNodes
	 */
	public long getFirstSolNodes() {
		return firstSolNodes;
	}

	/**
	 * @param l
	 *          the searchNodes to set
	 */
	public void setFirstSolNodes(long l) {
		firstSolNodes = l;
	}

	/**
	 * @return the searchBacktracks
	 */
	public long getSearchBacktracks() {
		return searchBacktracks;
	}

	/**
	 * @param l
	 *          the searchBacktracks to set
	 */
	public void setFirstSolBacktracks(long l) {
		firstSolBacktracks = l;
	}

	/**
	 * @return the searchBacktracks
	 */
	public long getFirstSolBacktracks() {
		return firstSolBacktracks;
	}

	/**
	 * @param l
	 *          the searchBacktracks to set
	 */
	public void setSearchBacktracks(long l) {
		searchBacktracks = l;
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
		sb.append(
		    "first sol : " + firstSolTime + "ms, " + firstSolNodes + " nodes, " + firstSolBacktracks + " backtracks\n");
		sb.append("actions (").append(actions.nbActions()).append(") :\n").append(actions).append("\ndestination : \n")
		    .append(destination).append("\nobjective result = " + objective);
		return sb.toString();
	}
}
