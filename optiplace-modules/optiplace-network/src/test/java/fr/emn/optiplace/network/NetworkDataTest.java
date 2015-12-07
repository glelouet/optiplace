package fr.emn.optiplace.network;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.network.NetworkData.VMGroup;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class NetworkDataTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkDataTest.class);

	@Test
	public void testAddVM() {
		NetworkData test = new NetworkData();
		VM v0 = new VM("v0");
		VM v1 = new VM("v1");
		VM v2 = new VM("v2");
		VM v3 = new VM("v3");
		VMGroup g0 = test.addGroup("g0", 5);
		VMGroup g1 = test.addGroup("g1", 10);
		test.addVM(g0, v0, v1);
		test.addVM(g1, v2, v3);

		Assert.assertEquals(test.use(v0, v1), 5);
		Assert.assertEquals(test.use(v0, v0), 0);
		Assert.assertEquals(test.use(v1, v0), 5);

		Assert.assertEquals(test.use(v0, v2), 0);
		Assert.assertEquals(test.use(v0, v3), 0);
		Assert.assertEquals(test.use(v1, v2), 0);
		Assert.assertEquals(test.use(v1, v3), 0);
		Assert.assertEquals(test.use(v2, v3), 10);
		Assert.assertEquals(test.use(v3, v0), 0);

	}

}
