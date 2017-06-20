/**
 *
 */

package fr.emn.optiplace.test;

import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.IConfiguration.VMSTATES;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class SolvingExampleTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SolvingExampleTest.class);

	@Test
	public void testCreation() {
		nbVMPerComputer = 0;
		nbComputers = 1;
		nbWaitings = 1;
		// resources = new String[] {};

		prepare();
		// strat.setMoveMigratingVMs(true);
		// strat.setLogChoices(true);
		// strat.setLogHeuristicsSelection(true);
		// strat.setLogStats(true);
		IConfiguration d = solve(src).getDestination();
		Assert.assertNotNull(d);
		Assert.assertEquals(d.nbVMs(null), nbWaitings + nbComputers * nbVMPerComputer, "dest is : " + d);
		Assert.assertEquals(d.nbVMs(VMSTATES.RUNNING), nbWaitings + nbComputers * nbVMPerComputer, "dest is : " + d);
		Assert.assertEquals(d.nbVMs(VMSTATES.WAITING), 0, "" + d.getWaitings().collect(Collectors.toList()));
		for (Computer n : nodes) {
			Assert.assertTrue(d.nbHosted(n) >= nbVMPerComputer,
					"node " + n + " has only vms : " + d.getHosted(n).collect(Collectors.toList()));
		}
	}
}
