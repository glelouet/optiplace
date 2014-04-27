package fr.emn.optiplace.server;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import fr.emn.optiplace.view.PluginDescriptor;

/** @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014 */
public class ViewManager {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ViewManager.class);

	protected File jarDir = new File(".");

	public void start() {
		load();
	}

	public void setJarDir(File jarDir) {
		this.jarDir = jarDir;
	}

	protected static FileFilter JARFILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.isFile() && pathname.getName().endsWith(".jar");
		}
	};

	protected void load() {
		System.err.println("working in " + jarDir.getAbsolutePath());
		if (!jarDir.exists()) {
			System.err.println("no directory " + jarDir.getAbsolutePath()
					+ " exists");
		}
		for (File f : jarDir.listFiles(JARFILTER)) {
			System.err.println(f.getAbsolutePath());
			try {
				JarFile jar = new JarFile(f.getAbsolutePath());
				JarEntry entry = jar
						.getJarEntry(fr.emn.optiplace.view.PluginDescriptor.DESCRIPTORFILENAME);
				jar.close();
				if (entry != null) {
					addManaged(f);
				}
			} catch (IOException e) {
				System.err.println("aborting load of " + f.getAbsolutePath()
						+ " because of " + e);
			}
		}
	}

	/** @param jar */
	protected void addManaged(File jarFile) {
		System.err.println("managing file " + jarFile);
		try {
			URLClassLoader cl = new URLClassLoader(new URL[]{jarFile.toURI()
					.toURL()});
			PluginDescriptor desc = new PluginDescriptor();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							cl.getResourceAsStream(fr.emn.optiplace.view.PluginDescriptor.DESCRIPTORFILENAME)));
			desc.read(reader);
			System.err.println("got desc : " + desc);
		} catch (Exception e) {
			logger.warn("", e);
		}
	}
}
