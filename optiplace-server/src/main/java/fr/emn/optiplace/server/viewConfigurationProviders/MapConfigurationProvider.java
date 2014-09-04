/**
 *
 */
package fr.emn.optiplace.server.viewConfigurationProviders;

import java.util.HashMap;

import fr.emn.optiplace.server.ViewConfigurationProvider;
import fr.emn.optiplace.view.ViewConfiguration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public class MapConfigurationProvider
		extends
			HashMap<String, ViewConfiguration>
		implements
			ViewConfigurationProvider {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MapConfigurationProvider.class);

	@Override
	public ViewConfiguration getConfiguration(String confName) {
		return get(confName);
	}
}
