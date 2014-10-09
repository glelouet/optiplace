package fr.emn.optiplace.solver;

import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.Configuration;

/**
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class ReconfigurationResult {

	protected Configuration destination, source;
	protected ActionGraph actions;

	/**
	 * 
	 * @return the end configuration
	 */
	public Configuration getDestination() {
		return destination;
	}

	/**
	 * 
	 * @return the source configuration
	 */
	public Configuration getSource() {
		return source;
	}

	/** @return the actions */
	public ActionGraph getActions() {
		return actions;
	}

	public void setData(Configuration src, Configuration dst, ActionGraph graph) {
		source = src;
		destination = dst;
		actions = graph;
	}
}
