/**
 *
 */
package fr.emn.optiplace.distance;

import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.VM;

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

	@Test
	public void testExport() {
		DistanceData data = new DistanceData();
		data.setDist("s1", "s2", 2);
		data.setDist("s3", "s4", 5);
		data.setLimit(12, new VM("v1"), new VM("v2"), new VM("v3"));
		data.setPatLimit(Pattern.compile("VM_.*"), 35);

		DistanceData data2 = new DistanceData();
		data.exportTxt().forEach(data2::readLine);

		Assert.assertEquals(data2.getDist("s1", "s2"), 2);
		Assert.assertEquals(data2.getDist("s3", "s4"), 5);
		Assert.assertEquals(data2.getGroup(new VM("v1")).size(), 3);
	}

}
