/**
 *
 */
package fr.emn.optiplace.ha.rules;

import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.Rule;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class LazyTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(LazyTest.class);

	@Test
	public void testSimpleInject() {
		prepare();
		Lazy c = new Lazy("CPU", 10, nodes[0]);
		IConfiguration d = solve(src, c).getDestination();
		Assert.assertEquals(d.nbHosted(nodes[0]), 1);
	}

	@Test
	public void testParsing() {
		HashSet<Computer> nodes = new HashSet<>();
		nodes.add(new Computer("n1"));
		nodes.add(new Computer("n2"));
		Rule r = new Lazy("CPU", 15, nodes);
		String s = r.toString();
		Lazy parsed = Lazy.parse(s);
		Assert.assertEquals(parsed, r);
	}
}
