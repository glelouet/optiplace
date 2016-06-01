/**
 *
 */
package fr.emn.optiplace.distance;

import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.IOptiplace;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class DistanceViewTest {

	@Test
	public void testVMSplit() {
		Configuration source = new Configuration("mem");
		// 4 servers with 5-4-5-4 mem capacity
		IntStream.rangeClosed(1, 4).forEach(i -> source.addOnline("s" + i, i % 2 == 0 ? 4 : 5));
		// 4 VM with 5-4-5-4 mem usage
		IntStream.rangeClosed(1, 4).forEach(i -> source.addVM("v" + i, null, i % 2 == 0 ? 4 : 5));

		DistanceData d = new DistanceData();
		d.setDist("s1", "s2", 1);
		d.setDist("s1", "s3", 5);
		d.setDist("s1", "s4", 5);
		d.setDist("s2", "s3", 5);
		d.setDist("s2", "s4", 5);
		d.setDist("s3", "s4", 2);

		// only possible solution is to put them on s1 ; s2
		d.setLimit(1, "v1", "v2");

		// only remaining solution is to put them on s3;s4
		d.setLimit(3, "v3", "v4");

		IOptiplace solver = new Optiplace(source).with(new DistanceView(d));
		IConfiguration dest = solver.solve().getDestination();
		Assert.assertNotNull(dest);
		// vi is on si
		IntStream.rangeClosed(1, 4).forEach(i -> Assert.assertEquals(dest.getLocation(new VM("v" + i)).getName(), "s" + i));
	}

	/**
	 * VM are grouped on same server by a distance requirement of 0 by the
	 */
	@Test
	public void testVMGroup() {
		Configuration source = new Configuration();
		Node n1 = source.addOnline("n1");
		Node n2 = source.addOnline("n2");
		VM v1 = source.addVM("v1", n1);
		VM v2 = source.addVM("v2", n2);

		DistanceData d = new DistanceData();
		d.setDist("n1", "n2", 2);
		d.setLimit(1, "v1", "v2");

		IOptiplace solver = new Optiplace(source).with(new DistanceView(d));
		IConfiguration dest = solver.solve().getDestination();
		Assert.assertNotNull(dest);
		Assert.assertEquals(dest.getLocation(v1), dest.getLocation(v2));

	}

}
