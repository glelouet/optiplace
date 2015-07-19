/**
 *
 */
package fr.emn.optiplace.test;

import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.center.configuration.Configuration;
import fr.emn.optiplace.center.configuration.Node;
import fr.emn.optiplace.center.configuration.Configuration.VMSTATES;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class SolvingExampleTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SolvingExampleTest.class);

	@Test
	public void testCreation() {
		prepare();
		Configuration d = solve(src).getDestination();
		Assert.assertNotNull(d);
		Assert.assertEquals(d.nbVMs(null), nbWaitings + nbNodes * nbVMPerNode);
		Assert.assertEquals(d.nbVMs(VMSTATES.RUNNING), nbWaitings + nbNodes
				* nbVMPerNode);
		Assert.assertEquals(d.nbVMs(VMSTATES.WAITING), 0, ""
				+ d.getWaitings().collect(Collectors.toList()));
		for (Node n : nodes) {
			Assert.assertTrue(d.nbHosted(n) >= nbVMPerNode, "node " + n
					+ " has only vms : " + d.getHosted(n).collect(Collectors.toList()));
		}
	}
}
