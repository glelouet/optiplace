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

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class DistanceViewTest {

	@Test
	public void testExample() {
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
		d.setDist("s3", "s4", 3);

		// only possible solution is to put them on s1 ; s2
		d.setLimit(1, "v1", "v2");

		// only remaining solution is to put them on s3;s4
		d.setLimit(2, "v3", "v4");

		IOptiplace solver = new Optiplace(source).with(new DistanceView(d));
		solver.getStrat().setLogChoices(true);
		solver.getStrat().setLogContradictions(true);
		IConfiguration dest = solver.solve().getDestination();
		Assert.assertNotNull(dest);
		System.err.println(dest);
	}

}
