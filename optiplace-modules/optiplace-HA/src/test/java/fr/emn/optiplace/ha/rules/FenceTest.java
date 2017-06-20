/**
 *
 */
package fr.emn.optiplace.ha.rules;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
public class FenceTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	.getLogger(FenceTest.class);

	@Test
	public void testSimpleInject() {
		nbWaitings = 0;
		nbVMPerComputer = 1;
		prepare();
		Set<VM> vms = src.getVMs().collect(Collectors.toSet());
		Set<String> nodes = Collections.singleton(this.nodes[0].name);
		Fence c = new Fence(vms, nodes);
		IConfiguration d = solve(src, c).getDestination();
		Assert.assertEquals(d.nbHosted(this.nodes[0]), 3, "cfg is " + d);
		Assert.assertEquals(d.nbHosted(this.nodes[2]), 0, "cfg is " + d);
	}

	@Test
	public void testInjectTwoComputers() {
		nbWaitings = 0;
		nbVMPerComputer = 1;
		prepare();
		Set<VM> vms = src.getVMs().collect(Collectors.toSet());
		Set<String> nodes = new HashSet<>(Arrays.asList(this.nodes[0].name, this.nodes[1].name));
		Fence c = new Fence(vms, nodes);
		IConfiguration d = solve(src, c).getDestination();
		Assert.assertEquals(d.nbHosted(this.nodes[0]) + d.nbHosted(this.nodes[1]),
				3);
		Assert.assertEquals(d.nbHosted(this.nodes[2]), 0);
	}

	@Test
	public void testParsing() {
		HashSet<String> nodes = new HashSet<>();
		nodes.add("n1");
		nodes.add("n2");
		HashSet<VM> vms = new HashSet<>();
		vms.add(new VM("vm1"));
		vms.add(new VM("vm2"));
		Rule r = new Fence(vms, nodes);
		String s = r.toString();
		Fence parsed = Fence.parse(s);
		Assert.assertEquals(parsed, r);
	}
}
