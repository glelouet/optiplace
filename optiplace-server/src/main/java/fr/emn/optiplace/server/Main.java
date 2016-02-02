/**
 *
 */

package fr.emn.optiplace.server;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.parser.ConfigurationFiler;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class Main {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Main.class);

	/**
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("b", "banviews", true, "the list of views names to disable");
		options.addOption("c", "confpath", true, "the path to look after conf.conf file. Default is '.'.");
		options.addOption("d", "viewdata", true,
				"the paths to look after for views data. Paths are separated by ':'. Default path is '.'.");
		options.addOption("h", "help", false, "print this message and exit");
		options.addOption("n", "nodyn", false, "disable the loading of views from jar(only static views can be used)");
		options.addOption("r", "required", true, "list of the views required to solve a problem.");
		options.addOption("s", "separator", true, "the separator of the options in the args ; default is ':'");
		options.addOption("v", "viewspath", true,
				"the paths in which to look after views. In case of several paths, they must be separated by ':'. default is 'views'.");

		CommandLine line = new org.apache.commons.cli.BasicParser().parse(options, args);

		if (line.hasOption('h')) {
			new HelpFormatter().printHelp(
					"optiplace. Loads a configuration of VM on Nodes, views of this configuration, configure those views with their data, then print an enhanced cofiguration.",
					options);
			return;
		}

		String confPath = line.hasOption('c') ? line.getOptionValue('c') : null;
		File f = new File(confPath, "conf.conf");
		if (!f.exists()) {
			logger.error("error : no configuration found : " + f.getAbsolutePath());
			return;
		}
		ConfigurationFiler cf = new ConfigurationFiler(f);
		cf.read();
		IConfiguration cfg = cf.getCfg();
		OptiplaceServer server = new OptiplaceServer();
		if (line.hasOption('s')) {
			server.parse_FS(line.getOptionValue('s'));
		}
		if (line.hasOption('d')) {
			server.parse_datapath(line.getOptionValue('d', "."));
		}
		if (line.hasOption('v')) {
			server.parse_viewspath(line.getOptionValue('v', "views"));
		}
		if (line.hasOption("n")) {
			server.getViewManager().setDisableLoading(true);
		}
		if (line.hasOption('b')) {
			server.parse_banViews(line.getOptionValue('b'));
		}

		System.out.println("solving");
		DeducedTarget t = server.solve(cfg);
		System.out.println(t.getDestination());
	}
}
