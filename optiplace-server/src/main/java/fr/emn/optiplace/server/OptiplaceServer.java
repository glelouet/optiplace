/**
 *
 */
package fr.emn.optiplace.server;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.OptiplaceSolver;
import fr.emn.optiplace.SolvingProcess;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.server.viewDataProviders.FileDataProvider;
import fr.emn.optiplace.server.viewDataProviders.MapConfigurationProvider;
import fr.emn.optiplace.server.viewDataProviders.PlexerProvider;
import fr.emn.optiplace.solver.BaseCenter;
import fr.emn.optiplace.solver.ConfigStrat;
import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.View;
import fr.emn.optiplace.view.ViewDataProvider;

/** @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014 */
public class OptiplaceServer implements OptiplaceSolver {

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

  /** @return the internal file data loader */
  public FileDataProvider getFileDataPRovider() {
    return filesConfs;
  }

  protected LinkedHashMap<String, View> views = new LinkedHashMap<>();

  /** add a view to use while solving a problem
   * @param v the view to add
   * @return the view previously associated to this view's name */
  public View addView(View v) {
    return views.put(v.getClass().getName(), v);
  }

  public void setViews(View... views) {
    this.views.clear();
    if (views != null && views.length != 0) {
      for (View v : views) {
        addView(v);
      }
    }
  }

  public Stream<View> getViews() {
    return views.values().stream();
  }

  public View getView(String name) {
    return views.get(name);
  }

  public int nbViews() {
    return views.size();
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
    LinkedHashSet<View> addedViews = new LinkedHashSet<>();
    LinkedHashSet<View> rejected = new LinkedHashSet<>();
    for (View v : views.values()) {
      boolean configured = v.setConfs(confProvider) && v.setDependencies(views);
      if (configured) {
        addedViews.add(v);
      } else {
        rejected.add(v);
      }
    }
    if (addedViews.size() != views.size()) {
      logger.warn("cannot solve, need to configure the views : " + rejected);
    }
    BaseCenter center = new BaseCenter();
    center.setSource(source);
    center.getViews().addAll(addedViews);
    SolvingProcess sp = new SolvingProcess();
    sp.center(center);
    if (strat != null) {
      sp.strat(strat);
    }
    sp.solve();
    return sp.getTarget();
  }

}
