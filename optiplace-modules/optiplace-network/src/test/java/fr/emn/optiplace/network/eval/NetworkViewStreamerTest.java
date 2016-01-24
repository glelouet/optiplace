package fr.emn.optiplace.network.eval;

import fr.emn.optiplace.configuration.Configuration;

public class NetworkViewStreamerTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkViewStreamerTest.class);

	public static void main(String[] args) {
		Configuration c = new Configuration();
		c.addOnline("n0");
		c.addOnline("n1");
		c.addOnline("n2");
		c.addVM("vm0", null);
		c.addVM("vm1", null);

		NetworkViewStreamer sms = new NetworkViewStreamer(c, 0, 6, 2, 3, 3, -1);
		sms.stream().forEach(d -> System.err.println("\n" + d.getData()));
	}
}
