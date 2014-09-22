/**
 *
 */
package fr.emn.optiplace.server;

import java.util.ArrayList;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.OptiplaceServer;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.server.viewDataProviders.FileDataProvider;
import fr.emn.optiplace.server.viewDataProviders.MapConfigurationProvider;
import fr.emn.optiplace.server.viewDataProviders.PlexerProvider;
import fr.emn.optiplace.view.ProvidedData;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class OptiplaceDefaultServer implements OptiplaceServer {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(OptiplaceDefaultServer.class);

	protected MapConfigurationProvider mapConfs = new MapConfigurationProvider();

	protected FileDataProvider filesConfs = new FileDataProvider();

	protected PlexerProvider confProvider = new PlexerProvider(mapConfs,
			filesConfs);

	public ViewDataProvider getViewConfigurations() {
		return confProvider;
	}

	protected ArrayList<AvailableView> views = new ArrayList<>();

	@Override
	public DeducedTarget solve(Configuration source,
			ProvidedData... configurations) {
		mapConfs.clear();
		if (configurations != null) {
			for (ProvidedData vc : configurations) {
				mapConfs.put(vc.name(), vc);
			}
		}

		// TODO
		throw new UnsupportedOperationException();
	}

}
