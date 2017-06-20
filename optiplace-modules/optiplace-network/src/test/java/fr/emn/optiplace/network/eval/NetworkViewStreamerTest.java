package fr.emn.optiplace.network.eval;

import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.network.NetworkView;

public class NetworkViewStreamerTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkViewStreamerTest.class);

	public static void main(String[] args) {
		Configuration c = new Configuration();
		c.addComputer("n0");
		c.addComputer("n1");
		c.addComputer("n2");
		c.addVM("vm0", null);
		c.addVM("vm1", null);

		NetworkViewStreamer sms = new NetworkViewStreamer(c, 0, 6, 2, 3, 3, -1);
		sms.stream().forEach(d -> System.err.println("\n" + d.getData()));
	}

	@Test
	public void testStreamNoNull() {
		Configuration c = new Configuration();
		c.addComputer("n0");
		c.addComputer("n1");
		c.addVM("v0", null);
		c.addVM("v1", null);
		c.addVM("v2", null);
		Stream<NetworkView> test = new NetworkViewStreamer(c, 2 * c.nbHosts(), 4 * c.nbHosts(), 2, 4, 4,
				10).stream();
		Assert.assertTrue(test.findAny().isPresent());
	}
}
