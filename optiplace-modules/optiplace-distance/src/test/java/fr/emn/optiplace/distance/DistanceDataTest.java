/**
 *
 */
package fr.emn.optiplace.distance;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class DistanceDataTest {

	@Test
	public void testCompletion() {
		DistanceData data = new DistanceData();
		String s1 = "s1", s2 = "s2", s3 = "s3";
		data.setDist(s1, s2, 2);
		Assert.assertTrue(data.isComplete(s1));
		Assert.assertTrue(data.isComplete(s2));
		Assert.assertTrue(data.isComplete(s3));
		Assert.assertTrue(data.isComplete(s1, s2));
		Assert.assertFalse(data.isComplete(s1, s3));
		Assert.assertFalse(data.isComplete(s2, s3));
		Assert.assertFalse(data.isComplete(s1, s2, s3));
	}

}
