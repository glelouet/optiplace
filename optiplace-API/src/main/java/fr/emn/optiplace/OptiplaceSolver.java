/**
 *
 */
package fr.emn.optiplace;

import fr.emn.optiplace.center.configuration.Configuration;
import fr.emn.optiplace.view.ProvidedData;

/**
 * Main entry point to create a stand-alone optiplace server.
 * <p>
 * A ConfiguredServer solves optimization problem on a center. For this, it is
 * reponsible to configure and integrate the views to add in a
 * reconfigurationProblem.
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public interface OptiplaceSolver {

	/**
	 * @param source
	 * the present state of the center, ie the hosting of the VMs on the Nodes
	 * @param datas
	 * information to provide to the views. If informations with the same name are
	 * already present in the solver, the new data replace the old one.
	 * @return a resolution of the reconfiguration problem.
	 */
	public DeducedTarget solve(Configuration source, ProvidedData... datas);

}
