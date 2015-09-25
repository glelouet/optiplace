/**
 *
 */
package fr.emn.optiplace.configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.resources.MappedResourceSpecification;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class SimpleConfigurationTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SimpleConfigurationTest.class);

	@Test
	public void testAddVM() {
		SimpleConfiguration test = new SimpleConfiguration();
		test.resources().put("CPU", new MappedResourceSpecification("CPU"));
		test.resources().put("MEM", new MappedResourceSpecification("MEM"));
		VM vm1 = test.addVM("vm1", null, 2, 5);
		VM vm2 = test.addVM("vm2", null, 10, 20);
		ResourceSpecification cpu = test.resources().get("CPU");
		ResourceSpecification mem = test.resources().get("MEM");
		Assert.assertEquals(cpu.getLoad(vm1), 2);
		Assert.assertEquals(cpu.getLoad(vm2), 10);
		Assert.assertEquals(mem.getLoad(vm1), 5);
		Assert.assertEquals(mem.getLoad(vm2), 20);
	}

	@Test
	public void testSameElements() {
		SimpleConfiguration c1 = new SimpleConfiguration();
		Node busy = c1.addOnline("busy");
		c1.addOnline("sleepy");
		c1.addOffline("off");
		c1.addVM("running", busy);
		c1.addVM("sleepy", null);
		SimpleConfiguration c2 = new SimpleConfiguration();
		for (String s : new String[] { "off", "sleepy", "busy" }) {
			c2.addOffline(s);
		}
		for (String s : new String[] { "running", "sleepy" }) {
			c2.addVM(s, null);
		}
		Assert.assertTrue(Configuration.sameElements(c1, c2));
		Assert.assertTrue(Configuration.sameElements(c2, c1));
		Assert.assertTrue(Configuration.sameElements(c1, c1));
		Assert.assertTrue(Configuration.sameElements(c2, c2));
		Node other = c2.addOnline("otherNode");
		Assert.assertFalse(Configuration.sameElements(c1, c2));
		Assert.assertFalse(Configuration.sameElements(c2, c1));
		c2.remove(other);
		Assert.assertTrue(Configuration.sameElements(c1, c1));
		Assert.assertTrue(Configuration.sameElements(c2, c2));
		c2.addVM("otherVM", null);
		Assert.assertFalse(Configuration.sameElements(c1, c2));
		Assert.assertFalse(Configuration.sameElements(c2, c1));
	}

	@Test
	public void testEquals() {
		SimpleConfiguration c1 = new SimpleConfiguration("CPU");
		SimpleConfiguration c2 = new SimpleConfiguration("CPU");
		for (String n : new String[] { "n0", "n1", "n2" }) {
			c1.addOnline(n, 10);
			c2.addOnline(n, 10);
		}
		for (String v : new String[] { "vm0", "vm1", "vm2" }) {
			c1.addVM(v, null, 5);
			c2.addVM(v, null, 5);
		}
		Assert.assertEquals(c2, c1);
		Assert.assertEquals(c1, c2);
		c1.addOnline("n3", 3);
		Assert.assertNotEquals(c2, c1);
		Assert.assertNotEquals(c1, c2);
		Node n = c2.addOnline("n3", 3);
		Assert.assertEquals(c2, c1);
		Assert.assertEquals(c1, c2);
		c2.addVM("vm3", n, 2);
		Assert.assertNotEquals(c2, c1);
		Assert.assertNotEquals(c1, c2);
		c1.addVM("vm3", n, 2);
		Assert.assertEquals(c2, c1);
		Assert.assertEquals(c1, c2);
	}

	@Test
	public void testSimpleSiteMaking() {
		SimpleConfiguration c = new SimpleConfiguration();
		Node[] nodes = new Node[8];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = c.addOnline("n" + i);
		}
		Assert.assertEquals(c.nbSites(), 0);
		Assert.assertEquals(c.getNodes(null).collect(Collectors.toSet()), new HashSet<>(Arrays.asList(nodes)));
		Site site1 = c.addSite("site1", nodes[2], nodes[3]);
		Assert.assertEquals(c.nbSites(), 1);
		Assert.assertEquals(c.getNodes(site1).collect(Collectors.toSet()),
		    new HashSet<>(Arrays.asList(nodes[2], nodes[3])));
		Assert.assertEquals(c.getNodes(null).collect(Collectors.toSet()).size(), 6);
	}

}
