/**
 *
 */
package fr.emn.optiplace.server;

import java.io.File;
import java.util.List;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.center.configuration.Configuration;
import fr.emn.optiplace.configuration.parser.ConfigurationFiler;
import fr.emn.optiplace.view.View;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class Main {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Main.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
	String confPath = null;
	File f = new File(confPath, "conf.conf");
	if (!f.exists()) {
	    System.err.println("error : no configuration found : " + f.getAbsolutePath());
	}
	ConfigurationFiler cf = new ConfigurationFiler(f);
	cf.read();
	Configuration cfg = cf.getCfg();
	OptiplaceServer server = new OptiplaceServer();
	server.getFileDataPRovider().setPaths(new File("."));

	ViewManager vm = new ViewManager();
	vm.setJarDir(new File("views"));
	List<View> views = vm.loadGoodViews(server.getViewDataProvider());
	logger.debug("loaded views : " + views);

	if (views != null && !views.isEmpty()) {
	    server.setViews(views.toArray(new View[0]));
	}
	System.out.println("solving");
	DeducedTarget t = server.solve(cfg);
	System.out.println(t.getDestination());
    }
}
