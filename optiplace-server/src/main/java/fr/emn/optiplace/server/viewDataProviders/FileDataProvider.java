/**
 *
 */
package fr.emn.optiplace.server.viewDataProviders;

import java.io.File;
import java.util.HashMap;

import fr.emn.optiplace.view.FileViewData;
import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.ViewDataProvider;

/** Load ViewData from specified directories
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014 */
public class FileDataProvider implements ViewDataProvider {

  HashMap<String, FileViewData> loaded = new HashMap<String, FileViewData>();

  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(FileDataProvider.class);

  protected File[] directories = null;

  public void setPaths(File... directories) {
    this.directories = directories;
  }

  /** clear any loaded config and re-load the specified directories */
  public void load() {
    loaded.clear();
    if (directories != null) {
      for (File d : directories) {
        if (d.exists() && d.isDirectory()) {
          for (File f : d.listFiles()) {
            if (f.isFile()) {
              String name = f.getName();
              int posDOT = name.lastIndexOf('.');
              if (posDOT != -1) {
                name = name.substring(0, posDOT);
              }
              loaded.put(name, new FileViewData(name, f));
            }
          }
        }
      }
    }
  }

  @Override
  public ProvidedData getData(String confName) {
    return loaded.get(confName);
  }
}
