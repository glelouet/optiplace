/**
 *
 */
package fr.emn.optiplace.server;

import java.io.File;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.center.configuration.Configuration;
import fr.emn.optiplace.configuration.parser.ConfigurationFiler;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class Main {

    @SuppressWarnings("unused")
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
	vm.loadAllViews();
	server.addViewLoader(vm);

	DeducedTarget t = server.solve(cfg);
	t.getDestination();
    }
}
