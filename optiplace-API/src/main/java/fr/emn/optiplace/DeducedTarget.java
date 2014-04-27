/**
 *
 */
package fr.emn.optiplace;

import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/** @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013 */
public class DeducedTarget {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DeducedTarget.class);

	private Configuration destination;

	private final ActionGraph actions = new ActionGraph();

	private long buildTime;

	private long configTime;

	private long searchTime;

	private ReconfigurationProblem problem;

	private IntDomainVar objective;

	private int searchNodes;

	private int searchBacktracks;

	private int searchSolutions;

	/** @return the destination */
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
	 *            the destination to set
	 */
	public void setDestination(Configuration destination) {
		this.destination = destination;
	}

	/** @return the buildTime */
	public long getBuildTime() {
		return buildTime;
	}

	/**
	 * @param buildTime
	 *            the buildTime to set
	 */
	public void setBuildTime(long buildTime) {
		this.buildTime = buildTime;
	}

	/** @return the configTime */
	public long getConfigTime() {
		return configTime;
	}

	/**
	 * @param configTime
	 *            the configTime to set
	 */
	public void setConfigTime(long configTime) {
		this.configTime = configTime;
	}

	/** @return the searchTime */
	public long getSearchTime() {
		return searchTime;
	}

	/**
	 * @param searchTime
	 *            the searchTime to set
	 */
	public void setSearchTime(long searcTime) {
		searchTime = searcTime;
	}

	/** @return the problem */
	public ReconfigurationProblem getProblem() {
		return problem;
	}

	/**
	 * @param problem
	 *            the problem to set
	 */
	public void setProblem(ReconfigurationProblem problem) {
		this.problem = problem;
	}

	/** @return the objective */
	public IntDomainVar getObjective() {
		return objective;
	}

	/**
	 * @param objective
	 *            the objective to set
	 */
	public void setObjective(IntDomainVar objective) {
		this.objective = objective;
	}

	/** @return the searchNodes */
	public int getSearchNodes() {
		return searchNodes;
	}

	/**
	 * @param searchNodes
	 *            the searchNodes to set
	 */
	public void setSearchNodes(int searchNodes) {
		this.searchNodes = searchNodes;
	}

	/** @return the searchBacktracks */
	public int getSearchBacktracks() {
		return searchBacktracks;
	}

	/**
	 * @param searchBacktracks
	 *            the searchBacktracks to set
	 */
	public void setSearchBacktracks(int searchBacktracks) {
		this.searchBacktracks = searchBacktracks;
	}

	/** @return the searchSolutions */
	public int getSearchSolutions() {
		return searchSolutions;
	}

	/**
	 * @param searchSolutions
	 *            the searchSolutions to set
	 */
	public void setSearchSolutions(int searchSolutions) {
		this.searchSolutions = searchSolutions;
	}
}
