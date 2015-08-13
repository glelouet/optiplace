package fr.emn.optiplace.solver.choco;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.SolvingProcess;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.VM;

/**
 * test when a VM is migrating in the source configuration
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class PreMigrateTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PreMigrateTest.class);

	/**
	 * three nodes with resource capacity of 2 ; three VMs with resource use of 2,
	 * 1, 1 ; the first vm also is migrating to second node.<br />
	 * So the only solution is to move the second VM to the third node
	 */
	@Test
	public void testOneVMShadowing() {
		SolvingProcess sp = new SolvingProcess();
		sp.getStrat().setMoveMigratingVMs(true);
		SimpleConfiguration sc = new SimpleConfiguration("mem");
		sp.source(sc);
		Node[] nodes = { sc.addOnline("n0", 2), sc.addOnline("n1", 2), sc.addOnline("n2", 2) };
		VM[] vms = { sc.addVM("vm0", nodes[0], 2), sc.addVM("vm1", nodes[1], 1), sc.addVM("vm2", nodes[2], 1) };
		sc.setMigTarget(vms[0], nodes[1]);
		sp.solve();
		Configuration dest = sp.getTarget().getDestination();
		Assert.assertEquals(dest.getLocation(vms[0]), nodes[0]);
		Assert.assertEquals(dest.getLocation(vms[1]), nodes[1]);
		Assert.assertEquals(dest.getMigTarget(vms[1]), nodes[2]);
		Assert.assertEquals(dest.getLocation(vms[2]), nodes[2]);
	}
}
