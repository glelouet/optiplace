/**
 *
 */
package fr.emn.optiplace.ha.rules;

import java.util.Collections;
import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.Rule;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class SplitTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SplitTest.class);

	@Test
	public void testSimpleInject() {
		prepare();
		Split c = new Split(Collections.singleton(placed[0][0]), Collections.singleton(placed[0][1]));
		IConfiguration d = solve(src, c).getDestination();
		Assert.assertNotEquals(d.getLocation(placed[0][0]), d.getLocation(placed[0][1]));
	}

	@Test
	public void testParsing() {
		HashSet<VM> vm1s = new HashSet<>();
		vm1s.add(new VM("vm1"));
		vm1s.add(new VM("vm2"));
		HashSet<VM> vm2s = new HashSet<>();
		vm2s.add(new VM("vm3"));
		vm2s.add(new VM("vm4"));
		Rule r = new Split(vm1s, vm2s);
		String s = r.toString();
		Split parsed = Split.parse(s);
		Assert.assertEquals(parsed, r);
	}
}
