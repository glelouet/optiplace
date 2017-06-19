/**
 *
 */
package fr.emn.optiplace.ha.rules;

import java.util.Arrays;
import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.Rule;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class SpreadTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SpreadTest.class);

	@Test
	public void useCaseSpread() {
		Configuration cfg = new Configuration("mem");
		Node n0 = cfg.addNode("n0", 64);
		Extern e = cfg.addExtern("e", 5);
		VM vm0 = cfg.addVM("vm0", null, 5);
		VM vm1 = cfg.addVM("vm1", null, 5);

		HAView ha = new HAView();
		ha.addRule(new Spread(vm0, vm1));

		Optiplace sp = new Optiplace(cfg);
		sp.views(ha);
		sp.solve();
		IConfiguration dest = sp.getTarget().getDestination();
		// System.err.println("" + dest);

		Assert.assertNotNull(dest);
		Assert.assertNotEquals(dest.getFutureLocation(vm0), dest.getFutureLocation(vm1),
				"vms are hosted on same hoster : " + dest.getFutureLocation(vm0));
		Assert.assertEquals(dest.nbHosted(n0), 1);
		Assert.assertEquals(dest.nbHosted(e), 1);
	}

	@Test
	public void testSimpleInject() {
		nbWaitings = 3;
		prepare();
		Spread c = new Spread(new HashSet<>(Arrays.asList(waitings)));
		IConfiguration d = solve(src, c).getDestination();
		HashSet<Node> hosters = new HashSet<>();
		for (VM v : waitings) {
			Assert.assertTrue(hosters.add(d.getNodeHost(v)));
		}
		Assert.assertEquals(hosters.size(), 3);
	}

	// FIXME
	/**
	 * a spread on 3 VMs, with 1 node and 2 extern. EAch VM should be placed on a
	 * different Node/extern.
	 */
	@Test
	public void testSpreadWithExtern() {
		nbWaitings = 0;
		nbVMPerNode = 3;
		nbNodes = 1;
		resources = null;
		prepare();
		Extern e1 = src.addExtern("ext1");
		Extern e2 = src.addExtern("ext2");

		IConfiguration d = solve(src, new Spread(placed[0])).getDestination();
		Assert.assertEquals(d.nbHosted(nodes[0]), 1, "dest is " + d);
		Assert.assertEquals(d.nbHosted(e1), 1, "dest is" + d);
		Assert.assertEquals(d.nbHosted(e2), 1, "dest is" + d);

	}

	@Test
	public void testParsing() {
		HashSet<VM> vms = new HashSet<>();
		vms.add(new VM("vm1"));
		vms.add(new VM("vm2"));
		Rule r = new Spread(vms);
		String s = r.toString();
		Spread parsed = Spread.parse(s);
		Assert.assertEquals(parsed, r);
	}
}
