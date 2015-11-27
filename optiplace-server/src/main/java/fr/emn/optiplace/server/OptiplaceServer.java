/**
 *
 */
package fr.emn.optiplace.server;

import java.io.File;
import java.util.List;
import java.util.Properties;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.OptiplaceSolver;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.server.viewDataProviders.FileDataProvider;
import fr.emn.optiplace.server.viewDataProviders.MapConfigurationProvider;
import fr.emn.optiplace.server.viewDataProviders.PlexerProvider;
import fr.emn.optiplace.solver.ConfigStrat;
import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.View;
import fr.emn.optiplace.view.ViewDataProvider;

/**
 * An Optiplace server aggregates several functionalities :
 * <ul>
 * <li>it uses a {@link ViewManager} to load, configure, link the views</li>
 * <li>it solves a Optiplace Problem designed as a source Configuration and
 * specific data to use in its views.</li>
 * </ul>
 * <p>
 * The behavior can be changed by
 * <ul>
 * <li>directly interaction with the components, eg modification of
 * {@link #getFileDataPRovider()} or {@link #getViewManager()}</li>
 * <li>call the parse_X methods to parse String</li>
 * <li>using a {@link Properties} which associates vales to the keys of
 * {@link PROPS} enums, and calling the {@link #configure(Properties)}</li>
 * </ul>
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class OptiplaceServer implements OptiplaceSolver {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OptiplaceServer.class);

	protected ConfigStrat strat = null;

	public ConfigStrat getStrat() {
		if (strat == null) {
			strat = new ConfigStrat();
		}
		return strat;
	}

	public void setStrat(ConfigStrat strat) {
		this.strat = strat;
	}

	protected MapConfigurationProvider mapConfs = new MapConfigurationProvider();

	protected FileDataProvider filesConfs = new FileDataProvider();

	protected PlexerProvider confProvider = new PlexerProvider(mapConfs, filesConfs);

	public ViewDataProvider getViewDataProvider() {
		return confProvider;
	}

	ViewManager vm = new ViewManager();

	public ViewManager getViewManager() {
		return vm;
	}

	/** @return the internal file data loader */
	public FileDataProvider getFileDataPRovider() {
		return filesConfs;
	}

	@Override
	public DeducedTarget solve(Configuration source, ProvidedData... configurations) {
		filesConfs.load();
		mapConfs.clear();
		if (configurations != null) {
			for (ProvidedData vc : configurations) {
				mapConfs.add(vc);
			}
		}
		List<View> views = vm.getViews(getViewDataProvider());
		Optiplace sp = new Optiplace();
		sp.source(source);
		sp.views(views);
		if (strat != null) {
			sp.strat(strat);
		}
		sp.solve();
		return sp.getTarget();
	}

	protected String field_sep = ":";

	public boolean parse_FS(String data) {
		if (data != null) {
			field_sep = data;
			return true;
		}
		return false;
	}

	public boolean parse_viewspath(String data) {
		try {
			String[] dirs = data.split(field_sep);
			File[] files = new File[dirs.length];
			for (int i = 0; i < dirs.length; i++) {
				files[i] = new File(dirs[i]);
			}
			vm.setJarDir(files);
			return true;
		} catch (Exception e) {
			logger.warn("error while parsing view path : " + data, e);
			return false;
		}
	}

	public boolean parse_datapath(String data) {
		try {
			String[] dirs = data.split(field_sep);
			File[] files = new File[dirs.length];
			for (int i = 0; i < dirs.length; i++) {
				files[i] = new File(dirs[i]);
			}
			getFileDataPRovider().setPaths(files);
			return true;
		} catch (Exception e) {
			logger.warn("error while parsing data path : " + data, e);
			return false;
		}
	}

	public boolean parse_banViews(String value) {
		if (value == null || value.isEmpty()) {
			getViewManager().setBannedViews((String[]) null);
		} else {
			getViewManager().setBannedViews(value.split(field_sep));
		}
		return true;
	}

	// TODO in case we want to requires specific views
	// public boolean parse_required(String value) {
	// if (value == null || value.isEmpty()) {
	// setRequiredViews((String[]) null);
	// } else {
	// setRequiredViews(value.split(field_sep));
	// }
	// return true;
	// }
	//
	// HashSet<String> requiredViews = new HashSet<>();
	//
	// protected void setRequiredViews(String... strings) {
	// requiredViews.clear();
	// if (strings != null && strings.length != 0) {
	// requiredViews.addAll(Arrays.asList(strings));
	// }
	// }

	public void configure(Properties props) {
		for (PROPS c : PROPS.values()) {
			c.apply(props, this);
		}
	}

}
