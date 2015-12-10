package fr.emn.optiplace.network;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.network.data.Link;
import fr.emn.optiplace.network.data.Router;
import fr.emn.optiplace.network.data.VMGroup;


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

	@Test
	public void testSimpleFindPath() {
		NetworkData test = new NetworkData();
		Node n0 = new Node("n0");
		Extern e0 = new Extern("e0");
		Link l = test.addLink(n0, e0, 2);
		Assert.assertEquals(test.findPath(n0, e0), Arrays.asList(l));
		Assert.assertEquals(test.findPath(e0, n0), Arrays.asList(l));
		Assert.assertNull(test.findPath(e0, e0));
		Assert.assertNull(test.findPath(n0, n0));
		Assert.assertNull(test.findPath(e0, null));
		Assert.assertNull(test.findPath(n0, null));
		Assert.assertNull(test.findPath(null, e0));
		Assert.assertNull(test.findPath(null, n0));
	}

	@Test(dependsOnMethods = "testSimpleFindPath")
	public void testSmallFindPath() {
		NetworkData test = new NetworkData();
		Node n0 = new Node("n0");
		Extern e0 = new Extern("e0");
		Router r0 = new Router("r0");
		Router r1 = new Router("r1");
		Router r2 = new Router("r2");
		Link l0 = test.addLink(n0, r0, 3);
		Link l1 = test.addLink(r0, r1, 6);
		Link l2 = test.addLink(r1, e0, 12);
		Link l3 = test.addLink(r1, r2, 42);
		Assert.assertEquals(test.findPath(n0, e0), Arrays.asList(l0, l1, l2));
		Assert.assertEquals(test.findPath(e0, n0), Arrays.asList(l2, l1, l0));
		Assert.assertEquals(test.findPath(r0, r2), Arrays.asList(l1, l3), "list is : " + test.findPath(r0, r2));
	}

}
