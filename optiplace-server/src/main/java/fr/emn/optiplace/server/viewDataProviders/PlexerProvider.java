/**
 *
 */
package fr.emn.optiplace.server.viewDataProviders;

import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.ViewDataProvider;

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
	public ProvidedData getData(String confName) {
		for (ViewDataProvider p : delegates) {
			ProvidedData ret = p.getData(confName);
			if (ret != null) {
				return ret;
			}
		}
		return null;
	}

}
