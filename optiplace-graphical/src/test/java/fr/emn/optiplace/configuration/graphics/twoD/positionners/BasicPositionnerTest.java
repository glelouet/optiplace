package fr.emn.optiplace.configuration.graphics.twoD.positionners;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class BasicPositionnerTest {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(BasicPositionnerTest.class);

	@Test
	public void testRemap() {
		Integer[] keys1 = {2, 3, 5, 1, 4};
		Double[] vals1 = {12.0, 13.0, 15.0, 11.0, 14.0};
		Integer[] keys2 = {5, 4, 3, 2, 1};
		Double[] vals2 = new Double[vals1.length];
		BasicPositionner.remapArray(keys1, vals1, keys2, vals2);
		Assert.assertEquals(vals2, new Double[]{15.0, 14.0, 13.0, 12.0, 11.0});
	}
}
