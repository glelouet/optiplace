/**
 *
 */

package fr.emn.optiplace.homogeneous;

import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.IConfiguration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class HomogeneousViewTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HomogeneousViewTest.class);

	Optiplace p;
	IConfiguration cfg;

	@BeforeMethod
	public void makeServer() {
		cfg = new Configuration("MEM", "CPU");
		HomogeneousView v = new HomogeneousView();
		p = new Optiplace(cfg);
		p.addView(v);

		p.getStrat().setMaxSearchTime(3000);
		p.getStrat().setGoalId("packinggoal");
	}

	@Test
	public void testTinyCenter() {
		Computer n0 = cfg.addComputer("n0", 10000, 5000);
		Computer n1 = cfg.addComputer("n1", 10000, 5000);
		cfg.addVM("vm0_0", n0, 2000, 100);
		cfg.addVM("vm1_0", n1, 2000, 100);
		p.solve();
		DeducedTarget t = p.getTarget();
		Assert.assertNotNull(t);
		Assert.assertTrue(t.getDestination().nbHosted(n0) == 0 || t.getDestination().nbHosted(n1) == 0);
	}

	@Test(dependsOnMethods = "testTinyCenter")
	public void testSmallCenter() {
		Computer n0 = cfg.addComputer("n0", 10000, 5000);
		Computer n1 = cfg.addComputer("n1", 10000, 5000);
		cfg.addVM("vm0_0", n0, 2000, 100);
		cfg.addVM("vm0_1", n0, 2000, 100);
		cfg.addVM("vm0_2", n0, 2000, 100);
		cfg.addVM("vm1_0", n1, 2000, 100);
		cfg.addVM("vm1_1", n1, 2000, 100);
		p.solve();
		DeducedTarget t = p.getTarget();
		Assert.assertTrue(t.getDestination().nbHosted(n1) == 0);
	}

	@Test(dependsOnMethods = "testSmallCenter")
	public void testMediumCenter() {
		// in this test, we made 5 servers
		// the first two have more VMs than the others
		// so they should contain all the VMs.
		Computer n4 = cfg.addComputer("n4", 10000, 10000);
		Computer n3 = cfg.addComputer("n3", 10000, 10000);
		Computer n2 = cfg.addComputer("n2", 10000, 10000);
		Computer n1 = cfg.addComputer("n1", 10000, 10000);
		Computer n0 = cfg.addComputer("n0", 10000, 10000);

		// we don't care about the CPU
		// as long as max(VM.CPU/VM.MEM)<min(node.CPU/node.MEM)
		// for any vm and node
		cfg.addVM("vm0_0", n0, 2000, 10);
		cfg.addVM("vm0_1", n0, 2000, 50);
		cfg.addVM("vm0_2", n0, 2000, 20);
		cfg.addVM("vm1_0", n1, 2000, 150);
		cfg.addVM("vm1_1", n1, 2000, 75);
		cfg.addVM("vm2_0", n2, 2000, 30);
		cfg.addVM("vm3_0", n3, 1500, 100);

		p.solve();
		DeducedTarget t = p.getTarget();
		Assert.assertTrue(t.getDestination().getHosted(n0).findAny().isPresent(), "" + t);
		Assert.assertTrue(t.getDestination().getHosted(n1).findAny().isPresent(), "" + t);
		Assert.assertEquals(t.getDestination().nbHosted(n2), 0, "" + t);
		Assert.assertEquals(t.getDestination().nbHosted(n3), 0, "" + t);
		Assert.assertEquals(t.getDestination().nbHosted(n4), 0, "" + t);
	}

	/**
	 * <p>
	 * test with decreasing load on 8 homogeneous servers. each node can host up
	 * to 12 Vms . The VMs are all homogeneous. The limiting resource is the
	 * memory
	 * </p>
	 * <p>
	 * The Computers are separated in 4 groups of 2 nodes each. The first groups
	 * have less VMs, the last are almost full. nbVM(group i) =
	 * maxVMPerComputer*i/nbComputerGroups.<br />
	 * With 12 VMs max, this means 0 VMs on first groups, 3 on second, 6 on third,
	 * 9 on fourth
	 * </p>
	 */
	@Test(dependsOnMethods = "testMediumCenter")
	public void testLargeCenter() {
		int maxVMsPerComputer = 12;
		int nodesPerGroup = 2;
		int nbComputerGroups = 4;
		int nbComputers = nodesPerGroup * nbComputerGroups;
		int vmMem = 2000, vmCpu = 100;
		int nodeMem = vmMem * maxVMsPerComputer, nodeCpu = vmCpu * maxVMsPerComputer * 3;
		Computer[] nodes = new Computer[nbComputers];
		for (int gi = 0; gi < nbComputerGroups; gi++) {
			int nbVMs = gi * maxVMsPerComputer / nbComputerGroups;
			for (int ni = 0; ni < nodesPerGroup; ni++) {
				int idx = gi * nodesPerGroup + ni;
				nodes[idx] = cfg.addComputer("n" + idx, nodeMem, nodeCpu);
				for (int vmi = 0; vmi < nbVMs; vmi++) {
					cfg.addVM("vm" + idx + "_" + vmi, nodes[idx], vmMem, vmCpu);
				}
			}
		}
		p.solve();
		Assert.assertNotNull(p.getTarget());
		IConfiguration d = p.getTarget().getDestination();
		for (int i = 0; i < nodes.length / 2 - (1 + nodesPerGroup / 2); i++) {
			Assert.assertEquals(d.nbHosted(nodes[i]), 0,
					"node " + nodes[i] + " has vms : " + d.getHosted(nodes[i]).collect(Collectors.toList()));
		}
	}
}
