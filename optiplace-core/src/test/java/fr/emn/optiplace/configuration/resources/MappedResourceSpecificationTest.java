/**
 *
 */
package fr.emn.optiplace.configuration.resources;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.SimpleNode;
import fr.emn.optiplace.configuration.SimpleVirtualMachine;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class MappedResourceSpecificationTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MappedResourceSpecificationTest.class);

	@Test
	public void testParsing() {
		MappedResourceSpecification test = new MappedResourceSpecification("test");
		test.readLine("" + MappedResourceSpecification.START_NODE_CAPA + "nod = 25");
		test.readLine("" + MappedResourceSpecification.START_VM_USE + "vma = 12");
		Assert.assertEquals(test.getCapacity(new SimpleNode("nod")), 25);
		Assert.assertEquals(test.getUse(new SimpleVirtualMachine("vma")), 12);
	}
}
