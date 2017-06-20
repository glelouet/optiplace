package fr.emn.optiplace.ha.rules;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.test.SolvingExample;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class NearTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NearTest.class);

	/** two VM from a node have to be on same site */
	@Test
	public void test() {
		nbComputers = 4;
		nbWaitings = 0;
		prepare();
		Computer node = nodes[2];
		src.addSite("mysite", node);
		VM vm0 = placed[0][0], vm1 = placed[2][0];
		Near test = new Near(vm0, vm1);
		IConfiguration d = solve(src, test).getDestination();
		Assert.assertTrue(test.isSatisfied(d), "rule is not satisfied");
		Assert.assertTrue(d.getSite(d.getComputerHost(vm0)) == d.getSite(d.getComputerHost(vm1)),
				"vms are on different site : " + vm0 + "->" + d.getSite(d.getComputerHost(vm0)) + ", " + vm1 + "->"
						+ d.getSite(d.getComputerHost(vm1)));
	}
}
