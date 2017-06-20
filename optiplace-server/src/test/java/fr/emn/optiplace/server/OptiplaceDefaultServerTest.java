/**
 *
 */
package fr.emn.optiplace.server;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class OptiplaceDefaultServerTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	.getLogger(OptiplaceDefaultServerTest.class);

	/**
	 * two resources : CPU and MEM <br />
	 * <p>
	 * One node with 1, 20 capacity<br />
	 * One Computer with 20, 1 capacity<br />
	 * </p>
	 * </p>
	 * One VM with 1,10 use<br />
	 * One VM with 10,1 use<br />
	 * The first VM fits on the first Computer, the second VM fits on the second
	 * Computer. <br />
	 * Both VM are placed on the first node : the second VM should be migrated to
	 * the second Computer.
	 * </p>
	 *
	 */
	@Test
	public void testSimpleSolve() {
		OptiplaceServer test = new OptiplaceServer();

		Configuration cfg = new Configuration("CPU", "MEM");
		Computer n1 = cfg.addComputer("n1", 1, 20);
		Computer n2 = cfg.addComputer("n2", 20, 1);
		VM vm1 = cfg.addVM("vm1", n1, 1, 10);
		VM vm2 = cfg.addVM("vm2", n1, 10, 1);

		DeducedTarget res = test.solve(cfg);
		IConfiguration dest = res.getDestination();

		Assert.assertEquals(res.getSearchSolutions(), 1);
		Assert.assertEquals(dest.nbHosted(n1), 1);
		Assert.assertEquals(dest.nbHosted(n2), 1, "dest is " + dest);
		Assert.assertEquals(dest.getLocation(vm1), n1);
		Assert.assertEquals(dest.getLocation(vm2), n2);
		Assert.assertEquals(dest.getMigTarget(vm1), null);
		Assert.assertEquals(dest.getMigTarget(vm2), null);
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
		int nbComputers = 10;
		int nbVmsPerComputer = 5;
		int nbWaitingVms = 10;
		OptiplaceServer test = new OptiplaceServer();
		Configuration cfg = new Configuration("CPU", "MEM", "DISK");
		Computer[] nodes = new Computer[nbComputers];
		VM[][] vms = new VM[nbComputers][nbVmsPerComputer];
		for (int i = 0; i < nbComputers; i++) {
			nodes[i] = cfg.addComputer("n" + i, 8 * 3000, 64 * 1024, 100 * 1000);
			vms[i] = new VM[nbVmsPerComputer];
			for (int j = 0; j < nbVmsPerComputer; j++) {
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
		IConfiguration dest = res.getDestination();
		for (Computer n : nodes) {
			Assert.assertEquals(dest.nbHosted(n), 6);
		}
	}
}
