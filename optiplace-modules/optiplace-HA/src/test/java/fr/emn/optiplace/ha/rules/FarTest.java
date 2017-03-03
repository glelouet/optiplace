package fr.emn.optiplace.ha.rules;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.ha.HAView;
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

	@Test
	public void testFarOnExterns() {
		Configuration src = new Configuration("mem");

		VM v1 = src.addVM("v1", null, 1000);
		VM v2 = src.addVM("v2", null, 1000);
		VM v3 = src.addVM("v3", null, 1000);

		Node n1 = src.addOnline("n1", 500);
		Node n2 = src.addOnline("n2", 500);
		Site local = src.addSite("local", n1, n2);

		Extern e1 = src.addExtern("e1", 1000);
		Site s1 = src.addSite("s1", e1);
		Extern e2 = src.addExtern("e2", 1000);
		Site s2 = src.addSite("s2", e2);
		Extern e3 = src.addExtern("e3", 1000);
		Site s3 = src.addSite("s3", e3);

		HAView ha = new HAView();
		ha.addRule(new Far(v1, v2, v3));

		IConfiguration dest = new Optiplace(src).with(ha).solve().getDestination();
		System.err.println(dest);
		Set<VMLocation> set = Arrays.asList(v1, v2, v3).stream().map(vm -> dest.getLocation(vm)).collect(Collectors.toSet());
		Assert.assertEquals(set.size(), 3);
	}
}
