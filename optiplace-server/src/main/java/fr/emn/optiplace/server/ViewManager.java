package fr.emn.optiplace.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.emn.optiplace.view.View;
import fr.emn.optiplace.view.ViewDataProvider;
import fr.emn.optiplace.view.ViewDescription;

/**
 * generates a list of view and their description from the jar files in a
 * folder.
 * <p>
 * first use is to load several jar files and extract the views inside.
 * </p>
 * <p>
 * Then it is used to only keeps the views which can be configured thanks to a
 * {@link ViewDataProvider} and their view dependencies being all met
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 */
public class ViewManager {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ViewManager.class);

	protected File[] jarDirs = new File[] { new File(".") };

	public void setJarDir(File... dirs) {
		jarDirs = dirs;
	}

	protected static final FileFilter JARFILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.isFile() && pathname.getName().endsWith(".jar");
		}
	};

	/**
	 * loads all the views possible from the jars in its dirs and the preloaded
	 * views. The views are NOT configured, use
	 * {@link #getViews(ViewDataProvider)} to make them configured
	 *
	 * @return a sorted list of the views.
	 */
	public List<View> loadViews() {
		ArrayList<View> ret = new ArrayList<>();
		for (Class<? extends View> c : internalViews) {
			try {
				if (!bannedViews.contains(c.getName())) {
					ret.add(c.newInstance());
				}
			} catch (Exception e) {
				logger.warn("error when creating an instance of " + c, e);
			}
		}
		if (!disableLoading) {
			for (File f : jarDirs) {
				if (!f.exists() || !f.isDirectory()) {
					logger.debug("no jar directory " + f.getAbsolutePath() + " exists");
				} else {
					for (File c : f.listFiles(JARFILTER)) {
						View v = extractViewFromJar(c);
						if (v != null && !bannedViews.contains(v.getName())) {
							ret.add(v);
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * extract a view stored in a jar, built as a module for optiplace.
	 *
	 * @param jarF
	 *          the file representing a jar.
	 */
	@SuppressWarnings("resource")
	protected View extractViewFromJar(File jarF) {
		try {
			URLClassLoader cl = new URLClassLoader(new URL[] { jarF.toURI().toURL() });
			ViewDescription desc = new ViewDescription();
			InputStream is = cl.getResourceAsStream(fr.emn.optiplace.view.PluginParser.DESCRIPTORFILENAME);
			if (is == null) {
				cl.close();
				return null;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			desc.read(reader);
			@SuppressWarnings("unchecked")
			Class<? extends View> c = (Class<? extends View>) cl.loadClass(desc.clazz);
			is.close();
			return c.newInstance();
		} catch (Exception e) {
			logger.warn("", e);
			return null;
		}
	}

	/**
	 * Try to configure the resources of the views and inject their dependencies.
	 *
	 * @param allViews
	 *          the list of all views that can be used
	 * @param vdp
	 *          the provider of configurations
	 * @return the list of views which are configured and have their dependencies
	 */
	public static List<View> keepCorrectviews(List<View> allViews, ViewDataProvider vdp) {
		HashSet<String> activated = new HashSet<>(), moved = new HashSet<>();
		HashMap<String, Set<String>> unactivated = new HashMap<>();
		HashMap<String, View> viewsByName = new HashMap<>();
		// first we keep only the views which can be configured. If a view has
		// no dependency we add it to activated, else we add it to unactivated
		// with its dependencies
		for (View v : allViews) {
			if (v.setConfs(vdp)) {
				String name = v.getClass().getName();
				if (!viewsByName.containsKey(name)) {
					viewsByName.put(name, v);
					Set<String> deps = v.extractDependencies();
					if (deps.size() == 0) {
						activated.add(name);
					} else {
						unactivated.put(name, deps);
					}
				}
			}
		}
		// then we select, in several passes, the views not already activated
		// and check if their dependencies are all in the activated set, in that
		// case we put them in moved ; afterward we add all names from moved to
		// activated
		do {
			moved.clear();
			for (Entry<String, Set<String>> e : unactivated.entrySet()) {
				if (activated.containsAll(e.getValue())) {
					moved.add(e.getKey());
				}
			}
			if (!moved.isEmpty()) {
				unactivated.keySet().removeAll(moved);
				activated.addAll(moved);
			}
		} while (!moved.isEmpty());
		// then we add all the views in a dictionary, which we provide to all
		// the views to configure their dependencies
		HashMap<String, View> availableViews = new HashMap<>();
		for (String n : activated) {
			availableViews.put(n, viewsByName.get(n));
		}
		for (View v : availableViews.values()) {
			if (!v.setDependencies(availableViews)) {
				logger.warn("error : can not configure view " + v + ", available views are : " + availableViews);
			}
		}
		return new ArrayList<>(availableViews.values());
	}

	/**
	 * <p>
	 * extract all the views from the jar files in jardDirs which <br />
	 * <el>
	 * <li>have their configuration present in the provider</li>
	 * <li>have their dependencies provided by views of this same group</li>
	 * </el>
	 * </p>
	 * <p>
	 * The returned views are configured and their dependencies are satisfied (the
	 * views they depend on are injected and present in the returned list)
	 * </p>
	 * <p>
	 * Also, the returned list is sorted
	 * <ol>
	 * <li>first come the views which are loaded from jar, then the views which
	 * come from the specific demand</li>
	 * <li>then the views are sorted by their name ascending</li>
	 * </ol>
	 * </p>
	 *
	 * @param provider
	 *          A provider to give the views the resources they require
	 * @return a new modifiable list of all the views present in the dirs of this,
	 *         as jars, and configured with both their configuration parameters
	 *         and their view dependencies.
	 */
	public List<View> getViews(ViewDataProvider provider) {
		List<View> accepted = keepCorrectviews(loadViews(), provider);
		sortViewsByNameExcluding(accepted, internalViews.stream().map(c -> c.getSimpleName()).collect(Collectors.toList()));
		return accepted;
	}

	/**
	 * Sort a list of view by their name, keeping the views with given names at
	 * the last position of the list
	 *
	 * @param l
	 *          the list of known views to sort.
	 * @param last
	 *          an ordered collection of views names. This should be a
	 *          {@link LinkedHashSet}, {@link LinkedHashMap}, any list, or
	 *          basically any collection that retains insertion order
	 */
	public static void sortViewsByNameExcluding(List<View> l, Collection<String> last) {
		// first sort them by presence in the set
		Collections.sort(l, (v1, v2) -> (last.contains(v1.getName()) ? 1 : 0) - (last.contains(v2.getName()) ? 1 : 0));
		// find the first index of presence in last and make sublist
		int indexfirst = 0;
		for (; indexfirst < l.size() && !last.contains(l.get(indexfirst).getName()); indexfirst++) {
		}
		if (indexfirst >= 2) {
			Collections.sort(l.subList(0, indexfirst), (v1, v2) -> v1.getName().compareTo(v2.getName()));
		}
		if (indexfirst < l.size()) {
			List<View> sub = l.subList(indexfirst, l.size());
			List<String> indexed = new ArrayList<>(last);
			Collections.sort(sub, (v1, v2) -> indexed.indexOf(v2.getName()) - indexed.indexOf(v1.getName()));
		}
	}

	/**
	 * the internal ordered collection of views to add to {@link #loadViews()}.
	 */
	protected LinkedHashSet<Class<? extends View>> internalViews = new LinkedHashSet<>();

	/**
	 * get the internal ordered collection of views to add to the loaded views.
	 * <p>
	 * Those views are instantiated and to be returned by {@link #loadViews()}
	 * </p>
	 * <p>
	 * However, they CAN be returned by {@link #getViews(ViewDataProvider)
	 * getViews}, but only if their dependencies are met (Views and resources) on
	 * configuration.
	 * </p>
	 *
	 * @return the internal set of views which are created using
	 *         {@link Class#newInstance() } and added to the list of views in
	 *         {@link #loadviews() loadviews).
	 */
	public LinkedHashSet<Class<? extends View>> getPreLoadedViewClasses() {
		return internalViews;
	}

	boolean disableLoading = false;

	/**
	 * set weither we forbid or not the loading of views from jars. default is
	 * false. If set to true, then only views from getPreloadedViews will be
	 * returned .
	 *
	 * @param b
	 *          the boolean to disable the loading of views from jars
	 */
	public void setDisableLoading(boolean b) {
		disableLoading = b;
	}

	protected HashSet<String> bannedViews = new HashSet<>();

	/**
	 * set the views that must not be used.
	 *
	 * @param strings
	 */
	public void setBannedViews(String... strings) {
		bannedViews.clear();
		if (strings != null) {
			bannedViews.addAll(Arrays.asList(strings));
		}
	}

	public Stream<String> getBannedViews() {
		return bannedViews.stream();
	}

}
