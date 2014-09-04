/**
 *
 */
package fr.emn.optiplace.server.viewConfigurationProviders;

import fr.emn.optiplace.server.ViewConfigurationProvider;
import fr.emn.optiplace.view.ViewConfiguration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public class PlexerProvider implements ViewConfigurationProvider {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PlexerProvider.class);

	ViewConfigurationProvider[] delegates = null;

	public void setDelegates(ViewConfigurationProvider... delegates) {
		if (delegates == null) {
			delegates = new ViewConfigurationProvider[]{};
		}
		this.delegates = delegates;
	}

	public PlexerProvider() {
	}

	public PlexerProvider(ViewConfigurationProvider... delegates) {
		this();
		setDelegates(delegates);
	}

	@Override
	public ViewConfiguration getConfiguration(String confName) {
		for (ViewConfigurationProvider p : delegates) {
			ViewConfiguration ret = p.getConfiguration(confName);
			if (ret != null) {
				return ret;
			}
		}
		return null;
	}

}
