package fr.emn.optiplace.view.linearpower;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 * 
 */
public class LineaPowerModelTest {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(LineaPowerModelTest.class);

	@Test
	public void testToString() {
		LinearPowerModel test = new LinearPowerModel();
		test.base = 5;
		test.addRes("CPU", 25.0);
		Assert.assertEquals(test.toString(), "linear(5.0;CPU=25.0)");
	}

	@Test
	public void testParse() {
		LinearPowerModel test = new LinearPowerModel();
		test.parse("CPU=2", "RAM=3", "DISK=50.5");
		Assert.assertEquals(test.weight("CPU"), 2.0);
		Assert.assertEquals(test.weight("RAM"), 3.0);
		Assert.assertEquals(test.weight("DISK"), 50.5);
	}

}
