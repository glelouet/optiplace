/**
 *
 */
package fr.emn.optiplace.server;

import fr.emn.optiplace.view.ViewConfiguration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public interface ViewConfigurationProvider {

	public ViewConfiguration getConfiguration(String confName);

}
