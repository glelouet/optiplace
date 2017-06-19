package fr.emn.optiplace.network;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.IOptiplace;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.network.data.Router;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class NetworkViewTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkViewTest.class);

	@Test
	public void testSimpleProblem() {
		// two nodes, each has a VM, a link between the two nodes
		Configuration c = new Configuration();
		Node n0 = c.addNode("n0");
		Node n1 = c.addNode("n1");
		VM v0 = c.addVM("v0", n0);
		VM v1 = c.addVM("v1", n1);

		NetworkView nv = new NetworkView();
		nv.getData().setUse(v0, v1, 10);

		// first case the couple vms use less than capacity : no modification
		nv.getData().setLink(n1, n0, 10);
		IConfiguration dest = new Optiplace(c).with(nv).solve().getDestination();
		Assert.assertEquals(dest.getLocation(v0), n0);
		Assert.assertEquals(dest.getLocation(v1), n1);

		// second case the VM use more than capacity : they must be grouped together
		nv.getData().setLink(n1, n0, 5);
		dest = new Optiplace(c).with(nv).solve().getDestination();
		Assert.assertEquals(dest.getLocation(v0), dest.getLocation(v1));
	}

	@Test(dependsOnMethods = "testSimpleProblem")
	public void testSimpleProblemWithRouter() {
		// now we test with a router between the two nodes
		Configuration c = new Configuration();
		Node n0 = c.addNode("n0");
		Node n1 = c.addNode("n1");
		VM v0 = c.addVM("v0", n0);
		VM v1 = c.addVM("v1", n1);

		NetworkView nv = new NetworkView();
		nv.getData().setUse(v0, v1, 10);
		Router r0 = nv.getData().addRouter("r0");

		nv.getData().setLink(n0, r0, 10);
		nv.getData().setLink(n1, r0, 10);
		IConfiguration dest = new Optiplace(c).with(nv).solve().getDestination();
		Assert.assertEquals(dest.getLocation(v0), n0);
		Assert.assertEquals(dest.getLocation(v1), n1);

		nv.getData().setLink(n0, r0, 5);
		dest = new Optiplace(c).with(nv).solve().getDestination();
		Assert.assertEquals(dest.getLocation(v0), dest.getLocation(v1));
	}

	@Test(dependsOnMethods = "testSimpleProblemWithRouter")
	public void test3VM3NodeOneRouter() {
		// little more complex problem, with VM use adds up
		// the configuration is initially bad, as each node can only host ONE VM
		Configuration c = new Configuration("mem");
		Node n0 = c.addNode("n0", 1);
		Node n1 = c.addNode("n1", 1);
		Node n2 = c.addNode("n2", 1);
		VM v0 = c.addVM("v0", n2, 1);
		VM v1 = c.addVM("v1", n2, 1);
		VM v2 = c.addVM("v2", n2, 1);

		// the V0 has a link of use 10 to v1 and a link of use 5 to v2
		// the corresponding nodes have the same corresponding capacities (though
		// through the router)
		NetworkView nv = new NetworkView();
		nv.getData().setUse(v0, v1, 10);
		nv.getData().setUse(v0, v2, 5);
		Router r0 = nv.getData().addRouter("r0");
		nv.getData().setLink(n0, r0, 15);
		nv.getData().setLink(n1, r0, 10);
		nv.getData().setLink(n2, r0, 5);
		IConfiguration dest = new Optiplace(c).with(nv).solve().getDestination();
		Assert.assertEquals(dest.getLocation(v0), n0);
		Assert.assertEquals(dest.getLocation(v1), n1);
		Assert.assertEquals(dest.getLocation(v2), n2);
	}

	@Test(dependsOnMethods = "testSimpleProblem")
	public void testWaiting() {
		// we test a bad configuration, each node can only host one VM but each VM
		// request too much network : they should stay waiting.
		Configuration c = new Configuration("mem");
		Node n0 = c.addNode("n0", 1);
		Node n1 = c.addNode("n1", 1);
		VM v0 = c.addVM("v0", null, 1);
		VM v1 = c.addVM("v1", null, 1);
		NetworkView nv = new NetworkView();
		nv.getData().setUse(v0, v1, 10);

		nv.getData().setLink(n0, n1, 5);
		IOptiplace o = new Optiplace(c).with(nv);
		IConfiguration dest = o.solve().getDestination();
		Assert.assertEquals(dest.getLocation(v0), null);
		Assert.assertEquals(dest.getLocation(v1), null);

		// now the link between the two nodes is correct : the vm should be running
		nv.getData().setLink(n0, n1, 10);
		dest = new Optiplace(c).with(nv).solve().getDestination();
		Assert.assertNotNull(dest.getLocation(v0));
		Assert.assertNotNull(dest.getLocation(v1));
	}
}
