/**
 *
 */
package fr.emn.optiplace.ha.rules;

import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.Rule;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class RootTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(RootTest.class);

	@Test
	public void testSimpleInject() {
		// we overload the last node and try to root some vms.
		nbWaitings = 3;
		prepare();
		for (VM v : waitings) {
			src.setHost(v, nodes[2]);
		}
		Root c = new Root(waitings);
		IConfiguration d = solve(src, c).getDestination();
		for (VM v : waitings) {
			Assert.assertEquals(d.getLocation(v), nodes[2]);
		}
		Assert.assertEquals(d.nbHosted(nodes[2]), 4);
	}

	@Test
	public void testParsing() {
		HashSet<VM> vms = new HashSet<>();
		vms.add(new VM("vm1"));
		vms.add(new VM("vm2"));
		Rule r = new Root(vms);
		String s = r.toString();
		Root parsed = Root.parse(s);
		Assert.assertEquals(parsed, r);
	}
}
