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
public class ConfigurationTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigurationTest.class);

	@Test
	public void testAddVM() {
		Configuration test = new Configuration();
		test.resources().put("CPU", new MappedResourceSpecification("CPU"));
		test.resources().put("MEM", new MappedResourceSpecification("MEM"));
		VM vm1 = test.addVM("vm1", null, 2, 5);
		VM vm2 = test.addVM("vm2", null, 10, 20);
		ResourceSpecification cpu = test.resources().get("CPU");
		ResourceSpecification mem = test.resources().get("MEM");
		Assert.assertEquals(cpu.getUse(vm1), 2);
		Assert.assertEquals(cpu.getUse(vm2), 10);
		Assert.assertEquals(mem.getUse(vm1), 5);
		Assert.assertEquals(mem.getUse(vm2), 20);
	}

	@Test
	public void testSameElements() {
		Configuration c1 = new Configuration();
		Computer busy = c1.addComputer("busy");
		c1.addComputer("idle");
		c1.addVM("running", busy);
		c1.addVM("sleepy", null);
		Configuration c2 = new Configuration();
		for (String s : new String[] { "running", "sleepy" }) {
			c2.addVM(s, null);
		}
		for (String s : new String[] { "busy", "idle" }) {
			c2.addComputer(s);
		}

		Assert.assertTrue(IConfiguration.sameElements(c1, c2));
		Assert.assertTrue(IConfiguration.sameElements(c2, c1));
		Assert.assertTrue(IConfiguration.sameElements(c1, c1));
		Assert.assertTrue(IConfiguration.sameElements(c2, c2));
		Computer other = c2.addComputer("otherComputer");
		Assert.assertFalse(IConfiguration.sameElements(c1, c2));
		Assert.assertFalse(IConfiguration.sameElements(c2, c1));
		c2.remove(other);
		Assert.assertTrue(IConfiguration.sameElements(c1, c1));
		Assert.assertTrue(IConfiguration.sameElements(c2, c2));
		c2.addVM("otherVM", null);
		Assert.assertFalse(IConfiguration.sameElements(c1, c2));
		Assert.assertFalse(IConfiguration.sameElements(c2, c1));
	}

	@Test
	public void testEquals() {
		Configuration c1 = new Configuration("CPU");
		Configuration c2 = new Configuration("CPU");
		for (String n : new String[] { "n0", "n1", "n2" }) {
			c1.addComputer(n, 10);
			c2.addComputer(n, 10);
		}
		for (String v : new String[] { "vm0", "vm1", "vm2" }) {
			c1.addVM(v, null, 5);
			c2.addVM(v, null, 5);
		}
		Assert.assertEquals(c2, c1);
		Assert.assertEquals(c1, c2);
		c1.addComputer("n3", 3);
		Assert.assertNotEquals(c2, c1);
		Assert.assertNotEquals(c1, c2);
		Computer n = c2.addComputer("n3", 3);
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
		Configuration c = new Configuration();
		Computer[] computers = new Computer[8];
		for (int i = 0; i < computers.length; i++) {
			computers[i] = c.addComputer("n" + i);
		}
		Assert.assertEquals(c.nbSites(), 0);
		Assert.assertEquals(c.getSiteLocations(null).collect(Collectors.toSet()), new HashSet<>(Arrays.asList(computers)));
		Site site1 = c.addSite("site1", computers[2], computers[3]);
		Assert.assertEquals(c.nbSites(), 1);
		Assert.assertEquals(c.getSiteLocations(site1).collect(Collectors.toSet()),
				new HashSet<>(Arrays.asList(computers[2], computers[3])));
		Assert.assertEquals(c.getSiteLocations(null).collect(Collectors.toSet()).size(), 6);
	}

	@Test
	public void testGetElementByName() {
		Configuration c = new Configuration();
		Extern extern = c.addExtern("extern");
		Computer online = c.addComputer("online");
		Site site = c.addSite("site");
		c.addVM("vmexterned", extern);
		c.addVM("vmonline", online);
		c.addVM("vmwaiting", null);

		for (String s : new String[] { extern.getName(), "eXtern", online.getName(), "onLINE",
				site.getName(), "SitE", "vmexterned", "vmExterned", "vmOnlinE", "vmonline", "vmwaiting", "vmWaiting", }) {
			Assert.assertNotNull(c.getElementByName(s), "can't find element " + s);
		}

		extern = c.getElementByName("extErn", Extern.class);
		online = c.getElementByName("onLINE", Computer.class);
		site = c.getElementByName("sIte", Site.class);
	}

	@Test
	public void testRemoveVM() {
		Configuration c = new Configuration();
		VM v = c.addVM("vm", null);
		Assert.assertEquals(c.nbVMs(), 1);
		Assert.assertTrue(c.remove(v));
		Assert.assertEquals(c.nbVMs(), 0);
	}

	@Test(expectedExceptions = ClassCastException.class)
	public void testGetElementByNameException() {
		Configuration c = new Configuration();
		c.addVM("vm", null);
		c.getElementByName("vm", Computer.class);
	}

}
