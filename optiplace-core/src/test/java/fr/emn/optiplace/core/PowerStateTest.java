/**
 *
 */
package fr.emn.optiplace.core;

import org.testng.Assert;

import fr.emn.optiplace.SolvingProcess;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleCenterStates;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.SimpleNode;
import fr.emn.optiplace.configuration.SimpleVirtualMachine;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * test the power management of nodes
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class PowerStateTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PowerStateTest.class);

	// states of servers and vms have no more sense.
	// @Test
	public void smallcentertest() {

		SimpleConfiguration cfg = new SimpleConfiguration();
		Node n1 = new SimpleNode("n1", 1, 5, 10);
		n1.setPowerSwitchable(true);
		cfg.addOnline(n1);
		Node n2 = new SimpleNode("n2", 1, 10, 10);
		n2.setPowerSwitchable(true);
		cfg.addOffline(n2);
		Node n3 = new SimpleNode("n3", 1, 5, 10);
		n3.setPowerSwitchable(false);
		cfg.addOnline(n3);
		Node n4 = new SimpleNode("n4", 1, 10, 10);
		n4.setPowerSwitchable(false);
		cfg.addOffline(n4);
		VirtualMachine vm = new SimpleVirtualMachine("vm", 1, 6, 1);
		cfg.addWaiting(vm);

		SimpleCenterStates sd = new SimpleCenterStates();
		sd.getRunnings().add(vm);

		SolvingProcess sp = new SolvingProcess();
		sp.getCenter().setSource(cfg);
		// sp.getCenter().setStates(sd);
		// sp.getStrat().setChocoVerbosity(Verbosity.FINEST);
		// sp.getStrat().getDisplayers().add(e)

		sp.solve();
		Assert.assertTrue(sp.getTarget().getSearchSolutions() > 0);
		Configuration target = sp.getTarget().getDestination();
		Assert.assertEquals(target.getLocation(vm), n2);

		n2.setPowerSwitchable(false);
		sp.solve();
		Assert.assertNull(sp.getTarget().getDestination());
	}
}
