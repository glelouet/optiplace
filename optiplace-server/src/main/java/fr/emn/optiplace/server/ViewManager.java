package fr.emn.optiplace.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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

    public void start() {
	loadAllViews();
    }

    public void setJarDir(File... dirs) {
	this.jarDirs = dirs;
    }

    protected static FileFilter JARFILTER = new FileFilter() {

	@Override
	public boolean accept(File pathname) {
	    return pathname.isFile() && pathname.getName().endsWith(".jar");
	}
    };

    public List<View> loadAllViews() {
	ArrayList<View> ret = new ArrayList<>();
	for (File f : jarDirs) {
	    if (!f.exists()) {
		System.err.println("no directory " + f.getAbsolutePath() + " exists");
	}
	    for (File c : f.listFiles(JARFILTER)) {
		View v = extractViewFromJar(c);
	    if (v != null) {
		ret.add(v);
	    }
	}
	}
	return ret;
    }

    /** @param jarF */
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
	    Class<? extends View> c = (Class<? extends View>) cl.loadClass(desc.clazz);
	    cl.close();
	    return c.newInstance();
	} catch (Exception e) {
	    logger.warn("", e);
	    return null;
	}
    }

    public static List<View> keepCorrectviews(List<View> allViews, ViewDataProvider vdp) {
	HashSet<String> activated = new HashSet<>(), moved = new HashSet<>();
	HashMap<String, Set<String>> unactivated = new HashMap<>();
	HashMap<String, View> viewsByName = new HashMap<>();
	// first we keep only the views which can be configured . If a view has
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
	for(String n : activated) {
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
     * extract all the views from the jar files in jardDirs which <br />
     * <el> <li>have their configuration present in the provider</li> <li>have
     * their dependencies provided by views of this same group</li> </el> The
     * returned views are configured and their dependencies are satisfied (the
     * views they depend on are injected and present in the returned list)
     *
     * @param provider
     *            A provider to give the views the resources they require
     * @return a new modifiable list of all the views present in the dirs of
     *         this, as jars, and configured with both their configuration
     *         parameters and their view dependencies
     */
    public List<View> loadGoodViews(ViewDataProvider provider) {
	return keepCorrectviews(loadAllViews(), provider);
    }

}
