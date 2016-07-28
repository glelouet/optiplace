/**
 *
 */
package fr.emn.optiplace.hosanna;

import java.io.File;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.parser.ConfigurationFiler;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class MakeConfig {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MakeConfig.class);

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Configuration cfg = new Configuration("disk_size", "num_cpus", "mem_size");
		Extern e1 = cfg.addExtern("e1", 5, 8, 4096);
		Extern e2 = cfg.addExtern("e2", 10, 4, 4096);


		ConfigurationFiler filer = new ConfigurationFiler(new File("infra.cfg")).withConfiguration(cfg);
		filer.write();
	}
}
