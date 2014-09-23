/**
 *
 */
package fr.emn.optiplace.server.viewDataProviders;

import java.util.HashMap;

import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.ViewDataProvider;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class MapConfigurationProvider
		extends
			HashMap<String, ProvidedData>
		implements
			ViewDataProvider {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MapConfigurationProvider.class);

	@Override
	public ProvidedData getData(String confName) {
		return get(confName);
	}

	public void add(ProvidedData pd) {
		put(pd.name(), pd);
	}
}
