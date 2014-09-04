/**
 *
 */
package fr.emn.optiplace.server;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.server.viewConfigurationProviders.FileConfigurationProvider;
import fr.emn.optiplace.server.viewConfigurationProviders.MapConfigurationProvider;
import fr.emn.optiplace.server.viewConfigurationProviders.PlexerProvider;
import fr.emn.optiplace.view.ViewConfiguration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public class OptiplaceDefaultServer implements OptiplaceServer {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(OptiplaceDefaultServer.class);

	protected MapConfigurationProvider mapConfs = new MapConfigurationProvider();

	protected FileConfigurationProvider filesConfs = new FileConfigurationProvider();

	protected PlexerProvider confProvider = new PlexerProvider(mapConfs,
			filesConfs);

	public ViewConfigurationProvider getViewConfigurations() {
		return confProvider;
	}

	@Override
	public DeducedTarget solve(Configuration source,
			ViewConfiguration... configurations) {
		mapConfs.clear();
		if (configurations != null) {
			for (ViewConfiguration vc : configurations) {
				mapConfs.put(vc.name(), vc);
			}
		}
		// TODO
		throw new UnsupportedOperationException();
	}

}
