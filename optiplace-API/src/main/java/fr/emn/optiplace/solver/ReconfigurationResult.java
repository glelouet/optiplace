package fr.emn.optiplace.solver;

import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.IConfiguration;

/**
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class ReconfigurationResult {

	protected IConfiguration destination, source;
	protected ActionGraph actions;

	/**
	 * 
	 * @return the end configuration
	 */
	public IConfiguration getDestination() {
		return destination;
	}

	/**
	 * 
	 * @return the source configuration
	 */
	public IConfiguration getSource() {
		return source;
	}

	/** @return the actions */
	public ActionGraph getActions() {
		return actions;
	}

	public void setData(IConfiguration src, IConfiguration dst, ActionGraph graph) {
		source = src;
		destination = dst;
		actions = graph;
	}
}
