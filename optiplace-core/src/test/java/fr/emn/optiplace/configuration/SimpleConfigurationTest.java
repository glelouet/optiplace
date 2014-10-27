/**
 *
 */
package fr.emn.optiplace.configuration;

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
	Assert.assertEquals(cpu.getUse(vm1), 2);
	Assert.assertEquals(cpu.getUse(vm2), 10);
	Assert.assertEquals(mem.getUse(vm1), 5);
	Assert.assertEquals(mem.getUse(vm2), 20);
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

}
