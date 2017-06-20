/**
 *
 */
package fr.emn.optiplace.ha.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.ha.rules.LoadInc;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.Rule;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class LoadIncTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(LoadIncTest.class);

	@Test
	public void testSimpleInject() {
		prepare();
		LoadInc c = new LoadInc("CPU", nodes);
		IConfiguration d = solve(src, c).getDestination();
		ResourceSpecification cpu = d.resources().get("CPU");
		int last = 0;
		for (Computer n : nodes) {
			int res = cpu.getUse(d, n);
			Assert.assertTrue(res >= last, "Node " + n + " has less CPU use(" + res
					+ ") than previous(" + last + ")");
			last = res;
		}
	}

	@Test
	public void testParsing() {
		List<Computer> nodes = new ArrayList<>();
		nodes.add(new Computer("n1"));
		nodes.add(new Computer("n2"));
		HashSet<VM> vms = new HashSet<>();
		vms.add(new VM("vm1"));
		vms.add(new VM("vm2"));
		Rule r = new LoadInc("BLA", nodes);
		String s = r.toString();
		LoadInc parsed = LoadInc.parse(s);
		Assert.assertEquals(parsed, r);
	}
}
