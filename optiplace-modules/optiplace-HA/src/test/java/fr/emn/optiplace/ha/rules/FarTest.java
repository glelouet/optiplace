package fr.emn.optiplace.ha.rules;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.test.SolvingExample;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class FarTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FarTest.class);

	/** two VM from a node have to be on different site */
	@Test
	public void test() {
		nbNodes = 4;
		nbWaitings = 0;
		prepare();
		Node node = nodes[2];
		src.addSite("site1", node);
		VM vm0 = placed[0][0], vm1 = placed[0][1];
		Far test = new Far(vm0, vm1);
		IConfiguration d = solve(src, test).getDestination();
		Assert.assertTrue(test.isSatisfied(d), "rule is not satisfied");
		Assert.assertTrue(d.getLocation(vm0).equals(node) || d.getLocation(vm1).equals(node), "no VM placed on the node "
		    + node + ", " + vm0 + " on " + d.getLocation(vm0) + ", " + vm1 + " on " + d.getLocation(vm1));
		Assert.assertTrue(d.getSite(d.getNodeHost(vm0)) != d.getSite(d.getNodeHost(vm1)), "vms are on same site : " + vm0
		    + "->" + d.getSite(d.getNodeHost(vm0)) + ", " + vm1 + "->" + d.getSite(d.getNodeHost(vm1)));
	}
}
