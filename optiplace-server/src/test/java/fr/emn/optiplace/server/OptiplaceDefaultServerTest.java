/**
 *
 */
package fr.emn.optiplace.server;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.VM;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class OptiplaceDefaultServerTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(OptiplaceDefaultServerTest.class);

	@Test
	public void testSimpleSolve() {
		OptiplaceServer test = new OptiplaceServer();
		// test.getStrat().setLogChoices(true);
		SimpleConfiguration cfg = new SimpleConfiguration("CPU", "MEM");
		Node n1 = cfg.addOnline("n1", 1, 20);
		Node n2 = cfg.addOnline("n2", 10, 5);
		VM vm1 = cfg.addVM("vm1", n1, 5, 5);
		VM vm2 = cfg.addVM("vm2", n1, 1, 10);
		DeducedTarget res = test.solve(cfg);
		Assert.assertEquals(res.getSearchSolutions(), 1);
		Assert.assertEquals(res.getDestination().nbHosted(n2), 1);
		Assert.assertEquals(res.getDestination().nbHosted(n1), 1);
		Assert.assertEquals(res.getDestination().getLocation(vm1), n2);
		Assert.assertEquals(res.getDestination().getLocation(vm2), n1);
	}

	/**
	 * test with 10 servers and 5 VMs per server, plus 10 waiting VMs
	 * <p>
	 * The 5 servers are homogeneous, with 64 GB RAM, 8*3GHz CPU, and 100 TB HD
	 * </p>
	 * <p>
	 * the 50 VMs use 10 GB Mem, and around 1TB per VM and low CPU use, so the
	 * bottleneck is always the MEM
	 * </p>
	 */
	@Test
	public void testBasicCenter() {
		int nbNodes = 10;
		int nbVmsPerNode = 5;
		int nbWaitingVms = 10;
		OptiplaceServer test = new OptiplaceServer();
		// test.getStrat().setLogChoices(true);
		SimpleConfiguration cfg = new SimpleConfiguration("CPU", "MEM", "DISK");
		Node[] nodes = new Node[nbNodes];
		VM[][] vms = new VM[nbNodes][nbVmsPerNode];
		for (int i = 0; i < nbNodes; i++) {
			nodes[i] = cfg.addOnline("n" + i, 8 * 3000, 64 * 1024, 100 * 1000);
			vms[i] = new VM[nbVmsPerNode];
			for (int j = 0; j < nbVmsPerNode; j++) {
				VM vm = cfg.addVM("vm" + i + "_" + j, nodes[i], 500, 10 * 1024,
						1 * 1000);
				vms[i][j] = vm;
			}
		}
		VM[] waitings = new VM[nbWaitingVms];
		for (int i = 0; i < nbWaitingVms; i++) {
			waitings[i] = cfg.addVM("vm_" + i, null, 500, 10 * 1024, 1 * 100);
		}
		DeducedTarget res = test.solve(cfg);
		Configuration dest = res.getDestination();
		for (Node n : nodes) {
			Assert.assertEquals(dest.nbHosted(n), 6);
		}
	}
}
