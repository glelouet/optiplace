/**
 *
 */
package fr.emn.optiplace.server;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.OptiplaceSolver;
import fr.emn.optiplace.SolvingProcess;
import fr.emn.optiplace.center.BaseCenter;
import fr.emn.optiplace.center.configuration.Configuration;
import fr.emn.optiplace.server.viewDataProviders.FileDataProvider;
import fr.emn.optiplace.server.viewDataProviders.MapConfigurationProvider;
import fr.emn.optiplace.server.viewDataProviders.PlexerProvider;
import fr.emn.optiplace.solver.ConfigStrat;
import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.ViewDataProvider;

/** @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014 */
public class OptiplaceServer implements OptiplaceSolver {

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
      .getLogger(OptiplaceServer.class);

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

  protected PlexerProvider confProvider = new PlexerProvider(mapConfs,
      filesConfs);

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
  public DeducedTarget solve(Configuration source,
      ProvidedData... configurations) {
    filesConfs.load();
    mapConfs.clear();
    if (configurations != null) {
      for (ProvidedData vc : configurations) {
        mapConfs.add(vc);
      }
    }
    BaseCenter center = new BaseCenter();
    center.setSource(source);
	center.getViews().addAll(vm.getViews(getViewDataProvider()));
    SolvingProcess sp = new SolvingProcess();
    sp.center(center);
    if (strat != null) {
      sp.strat(strat);
    }
    sp.solve();
    return sp.getTarget();
  }

}
