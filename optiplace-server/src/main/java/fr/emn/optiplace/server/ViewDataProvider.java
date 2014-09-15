/**
 *
 */
package fr.emn.optiplace.server;

import fr.emn.optiplace.view.ProvidedData;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public interface ViewDataProvider {

	public ProvidedData getConfiguration(String confName);

}
