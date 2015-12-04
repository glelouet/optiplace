
package fr.emn.optiplace.ha.rules;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class AmongTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AmongTest.class);

	@Test
	public void testSimpleInject() {
		prepare();
		// strat.setLogSolutions(true);
		// strat.setLogChoices(true);
		Set<VM> vms = Stream.concat(src.getHosted(nodes[0]), src.getHosted(nodes[1])).distinct()
		    .collect(Collectors.toSet());
		Node n = nodes[nodes.length - 1];
		Among among = new Among(vms, n.getName());
		IConfiguration d = solve(src, among).getDestination();
		for (VM v : vms) {
			Assert.assertEquals(d.getLocation(v), n, "dest is " + d + " among is" + among);
		}
		// System.err.println("" + d);
	}

	@Test
	public void testSimpleAmong() {
		Configuration cfg = new Configuration("mem");
		Node n0 = cfg.addOnline("n0", 64);
		Extern e = cfg.addExtern("e", 5);
		VM vm0 = cfg.addVM("vm0", null, 5);
		VM vm1 = cfg.addVM("vm1", null, 5);

		HAView ha = new HAView();
		ha.addRule(new Among("n0", vm0));
		ha.addRule(new Among("e", vm1));

		Optiplace sp = new Optiplace();
		sp.views(ha);
		sp.source(cfg);
		sp.solve();
		IConfiguration dest = sp.getTarget().getDestination();

		Assert.assertNotNull(dest);
		Assert.assertEquals(dest.getLocation(vm0), n0);
		Assert.assertEquals(dest.getLocation(vm1), e);
	}

	@Test(dependsOnMethods = "testSimpleInject")
	public void testInjectWithChoice() {
		prepare();
		Set<VM> vms = src.getHosted(nodes[0]).distinct().collect(Collectors.toSet());
		Set<Set<String>> ns = new HashSet<>();
		ns.add(Collections.singleton(nodes[nodes.length - 1].getName()));
		ns.add(Collections.singleton(nodes[nodes.length - 2].getName()));
		Among a = new Among(vms, ns);
		// System.err.println("" + a);
		IConfiguration d = solve(src, a).getDestination();
		Node dest = null;
		for (VM v : vms) {
			Node l_dest = d.getNodeHost(v);
			if (dest == null) {
				dest = l_dest;
			}
			Assert.assertEquals(l_dest, dest, "VM " + v + " is hosted on " + l_dest + " while previous VMs were on " + dest);
		}
		Assert.assertTrue(ns.contains(Collections.singleton(dest.getName())),
		    "allowed nodes : " + ns + " does not contain node " + dest);
		// System.err.println("" + d);
	}

	@Test
	public void testParsing() {
		HashSet<Set<String>> nnodes = new HashSet<>();
		HashSet<String> nodes1 = new HashSet<>();
		nodes1.add("n1");
		nodes1.add("n2");
		nnodes.add(nodes1);
		HashSet<String> nodes2 = new HashSet<>();
		nodes2.add("n3");
		nodes2.add("n4");
		nnodes.add(nodes2);
		HashSet<VM> vms = new HashSet<>();
		vms.add(new VM("vm1"));
		vms.add(new VM("vm2"));
		Rule r = new Among(vms, nnodes);
		String s = r.toString();
		Among parsed = Among.parse(s);
		Assert.assertEquals(parsed, r);
	}
}
