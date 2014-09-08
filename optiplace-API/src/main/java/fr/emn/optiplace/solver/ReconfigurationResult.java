package fr.emn.optiplace.solver;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class ReconfigurationResult {

	protected Configuration destination, source;

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

	public void setData(Configuration src, Configuration dst) {
		this.source = src;
		this.destination = dst;
	}

	/**
	 * 
	 * @return the number of migrations requested to go from source to dest.
	 */
	public int getNbMigrations() {
		int migs = 0;
		for (Node n : source.getAllNodes()) {
			for (VirtualMachine vm : source.getRunnings(n)) {
				if (!destination.getRunnings(n).contains(vm)) {
					migs++;
				}
			}
		}
		return migs;
	}
}
