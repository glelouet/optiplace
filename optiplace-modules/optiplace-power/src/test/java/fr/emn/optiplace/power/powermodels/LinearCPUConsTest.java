package fr.emn.optiplace.power.powermodels;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LinearCPUConsTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LinearCPUConsTest.class);

	@Test
	public void testParsing() {
		LinearCPUCons a = new LinearCPUCons(2, 60);
		LinearCPUCons b = (LinearCPUCons) LinearCPUCons.PARSER.parse(a.toString());
		Assert.assertEquals(b.min, 2.0);
		Assert.assertEquals(b.max, 60.0);
	}
}
