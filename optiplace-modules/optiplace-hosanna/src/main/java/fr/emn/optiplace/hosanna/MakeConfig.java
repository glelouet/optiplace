/**
 *
 */
package fr.emn.optiplace.hosanna;

import java.io.File;

import fr.emn.optiplace.configuration.Configuration;
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
	public static void main(String[] args) {
		Configuration cfg = new Configuration();
		cfg.addExtern("e1");
		cfg.addExtern("e2");
		ConfigurationFiler filer = new ConfigurationFiler(new File("infra.cfg")).withConfiguration(cfg);
		filer.write();
	}
}
