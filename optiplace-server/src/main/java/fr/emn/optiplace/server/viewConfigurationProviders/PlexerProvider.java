/**
 *
 */
package fr.emn.optiplace.server.viewConfigurationProviders;

import fr.emn.optiplace.server.ViewDataProvider;
import fr.emn.optiplace.view.ProvidedData;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public class PlexerProvider implements ViewDataProvider {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PlexerProvider.class);

	ViewDataProvider[] delegates = null;

	public void setDelegates(ViewDataProvider... delegates) {
		if (delegates == null) {
			delegates = new ViewDataProvider[]{};
		}
		this.delegates = delegates;
	}

	public PlexerProvider() {
	}

	public PlexerProvider(ViewDataProvider... delegates) {
		this();
		setDelegates(delegates);
	}

	@Override
	public ProvidedData getConfiguration(String confName) {
		for (ViewDataProvider p : delegates) {
			ProvidedData ret = p.getConfiguration(confName);
			if (ret != null) {
				return ret;
			}
		}
		return null;
	}

}
