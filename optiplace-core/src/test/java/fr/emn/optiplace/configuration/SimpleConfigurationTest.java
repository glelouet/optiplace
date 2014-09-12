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
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SimpleConfigurationTest.class);

	@Test
	public void testAddVM() {
		SimpleConfiguration test = new SimpleConfiguration();
		test.resources().put("CPU", new MappedResourceSpecification("CPU"));
		test.resources().put("MEM", new MappedResourceSpecification("MEM"));
		VirtualMachine vm1 = test.addVM("vm1", null, 2, 5);
		VirtualMachine vm2 = test.addVM("vm2", null, 10, 20);
		ResourceSpecification cpu = test.resources().get("CPU");
		ResourceSpecification mem = test.resources().get("MEM");
		Assert.assertEquals(cpu.getUse(vm1), 2);
		Assert.assertEquals(cpu.getUse(vm2), 10);
		Assert.assertEquals(mem.getUse(vm1), 5);
		Assert.assertEquals(mem.getUse(vm2), 20);
	}

}
