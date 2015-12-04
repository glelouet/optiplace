package fr.emn.optiplace.thermal;

import static fr.emn.optiplace.thermal.ImpactMap.makeConnex;
import static fr.emn.optiplace.thermal.ImpactMap.makeIdentity;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.thermal.ImpactMap;

public class ImpactMapTest {

	@SuppressWarnings("unused")
	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ImpactMapTest.class);

	@Test
	public void testComparison() {
		ImpactMap ic1 = makeIdentity(2, 1), ic2 = makeIdentity(2, 1), ic3 = makeIdentity(
				2, 1);
		// small modification here
		ic3.set("s1", "s2", 1);
		// check transitivity
		for (ImpactMap ic : new ImpactMap[]{ic1, ic2, ic3}) {
			assertEquals(ic, ic);
		}
		assertEquals(ic1, ic2);
		assertEquals(ic2, ic1);
		assertNotEquals(ic1, ic3);
		assertNotEquals(ic2, ic3);
		assertNotEquals(ic3, ic1);
		assertNotEquals(ic3, ic2);
	}

	@Test(dependsOnMethods = "testComparison")
	public void testClone() {
		ImpactMap from = makeIdentity(10, 0.5);
		assertEquals(from, from.clone());
	}

	@Test(dependsOnMethods = "testComparison")
	public void testWriteAndParse() {
		ImpactMap from = makeConnex(3, 0.5);
		CharArrayWriter car = new CharArrayWriter();
		from.write(new PrintWriter(car, true));
		car.flush();
		ImpactMap to = new ImpactMap();
		to.parse(new BufferedReader(new CharArrayReader(car.toCharArray())));
		Assert.assertEquals(from, to);
	}
}
