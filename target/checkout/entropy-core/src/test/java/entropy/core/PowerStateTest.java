/**
 *
 */
package entropy.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.SolvingProcess;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleStateDefinition;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;

/**
 * test the power management of nodes
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class PowerStateTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PowerStateTest.class);

	@Test
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

		SimpleStateDefinition sd = new SimpleStateDefinition();
		sd.getRunnings().add(vm);

		SolvingProcess sp = new SolvingProcess();
		sp.getCenter().setSource(cfg);
		sp.getCenter().setStates(sd);
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
