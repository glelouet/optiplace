package fr.emn.optiplace.eval;

import fr.emn.optiplace.configuration.Configuration;

public class ConfigurationStreamerMain {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigurationStreamerMain.class);

	public static void main(String[] args) {

		Configuration c = new Configuration();
		c.addOnline("n0");
		c.addOnline("n1");
		c.addOnline("n2");
		c.addVM("v0", null);
		c.addVM("v1", null);
		c.addVM("v2", null);
		c.addVM("v3", null);
		c.addVM("v4", null);
		c.addVM("v5", null);
		System.err.println(ConfigurationStreamer.streamResource(c, "mem", 15, 50, 3).count());
	}
}
