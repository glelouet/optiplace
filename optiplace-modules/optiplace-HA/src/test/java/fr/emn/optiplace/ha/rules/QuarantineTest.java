/**
 *
 */
package fr.emn.optiplace.ha.rules;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.Rule;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class QuarantineTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(QuarantineTest.class);

	@Test
	public void testSimpleInject() {
		prepare();
		Quarantine c = new Quarantine(nodes[1].getName());
		IConfiguration d = solve(src, c).getDestination();
		Assert.assertEquals(d.nbHosted(nodes[1]), src.nbHosted(nodes[1]));
	}

	@Test
	public void testParsing() {
		Rule r = new Quarantine("n1", "n2");
		String s = r.toString();
		Quarantine parsed = Quarantine.parse(s);
		Assert.assertEquals(parsed, r);
	}
}
