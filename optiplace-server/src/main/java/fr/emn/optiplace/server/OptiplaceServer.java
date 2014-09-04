/**
 *
 */
package fr.emn.optiplace.server;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.view.ViewConfiguration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public interface OptiplaceServer {

	public DeducedTarget solve(Configuration source,
			ViewConfiguration... configurations);

}
