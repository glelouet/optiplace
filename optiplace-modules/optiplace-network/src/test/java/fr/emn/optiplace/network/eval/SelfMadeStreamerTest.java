package fr.emn.optiplace.network.eval;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.network.eval.SelfMadeStreamer;

public class SelfMadeStreamerTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SelfMadeStreamerTest.class);

	public static void main(String[] args) {
		Configuration c = new Configuration();
		c.addOnline("n0");
		c.addOnline("n1");
		c.addOnline("n2");
		c.addVM("vm0", null);
		c.addVM("vm1", null);

		SelfMadeStreamer sms = new SelfMadeStreamer(c, 0, 6, 2, 3, 3);
		sms.stream().forEach(d -> System.err.println("\n" + d.getData()));
	}
}
