/**
 *
 */
package fr.emn.optiplace.server;

import fr.emn.optiplace.view.ProvidedData;

/**
 * a class implementing this interface can provide a data associated to a name.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public interface ViewDataProvider {

	public ProvidedData getData(String confName);

}
