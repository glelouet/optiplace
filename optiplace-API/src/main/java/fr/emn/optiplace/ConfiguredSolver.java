/**
 *
 */
package fr.emn.optiplace;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.view.ProvidedData;

/**
 * Main entry point tto create a stand-alone optiplace server.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public interface ConfiguredSolver {

	public DeducedTarget solveProblem(Configuration source, ProvidedData... datas);

}
