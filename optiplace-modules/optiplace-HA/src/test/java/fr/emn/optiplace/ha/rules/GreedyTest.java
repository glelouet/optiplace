/**
 *
 */
package fr.emn.optiplace.ha.rules;

import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.Rule;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class GreedyTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(GreedyTest.class);

	@Test
	public void testSimpleInject() {
		prepare();
		Greedy c = new Greedy(10, "CPU", placed[0][0]);
		IConfiguration d = solve(src, c).getDestination();
		Node hoster = d.getNodeHost(placed[0][0]);
		Assert.assertEquals(d.resources().get("CPU").getUse(d, hoster), 100);
	}

	@Test
	public void testParsing() {
		HashSet<VM> vms = new HashSet<>();
		vms.add(new VM("vm1"));
		vms.add(new VM("vm2"));
		Rule r = new Greedy(25, "CPU", vms);
		String s = r.toString();
		Greedy parsed = Greedy.parse(s);
		Assert.assertEquals(parsed, r);
	}
}
