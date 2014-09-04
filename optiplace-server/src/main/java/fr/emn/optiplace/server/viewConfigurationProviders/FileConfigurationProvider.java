/**
 *
 */
package fr.emn.optiplace.server.viewConfigurationProviders;

import java.io.File;
import java.util.HashMap;

import fr.emn.optiplace.server.ViewConfigurationProvider;
import fr.emn.optiplace.view.FileViewConfiguration;
import fr.emn.optiplace.view.ViewConfiguration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public class FileConfigurationProvider implements ViewConfigurationProvider {

	HashMap<String, FileViewConfiguration> loaded = new HashMap<String, FileViewConfiguration>();

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(FileConfigurationProvider.class);

	protected File[] directories = null;

	public void setPaths(File... directories) {
		this.directories = directories;
	}

	public void load() {
		loaded.clear();
		for (File d : directories) {
			if (d.exists() && d.isDirectory()) {
				for (File f : d.listFiles()) {
					if (f.isFile()) {
						String name = f.getName();
						int posDOT = name.lastIndexOf('.');
						if (posDOT != -1) {
							name = name.substring(0, posDOT);
						}
						loaded.put(name, new FileViewConfiguration(name, f));
					}
				}
			}
		}
	}

	@Override
	public ViewConfiguration getConfiguration(String confName) {
		return loaded.get(confName);
	}
}
