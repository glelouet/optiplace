/**
 *
 */
package fr.emn.optiplace.choco;

import org.testng.Assert;
import org.testng.annotations.Test;

import solver.Cause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.VF;
import util.ESat;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class RangePropagatorTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(RangePropagatorTest.class);

	@Test
	public void testRangeGetter() {
		Solver s = new Solver();
		IntVar x = VF.bounded("X", Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, s);
		IntVar idx = VF.enumerated("idx", 0, 50, s);
		RangePropagator test = new RangePropagator(x, idx, -5, 10, 5000);
		Assert.assertEquals(test.getNbRanges(), 4);
		Assert.assertEquals(test.getLB(0), Integer.MIN_VALUE);
		Assert.assertEquals(test.getUB(0), -5);
		Assert.assertEquals(test.getLB(2), 11);
		Assert.assertEquals(test.getUB(2), 5000);
		Assert.assertEquals(test.getLB(3), 5001);
		Assert.assertEquals(test.getUB(3), Integer.MAX_VALUE);
	}

	@Test(dependsOnMethods = "testRangeGetter")
	public void testSatisfaction() throws ContradictionException {
		Solver s = new Solver();
		IntVar x = VF.bounded("X", Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, s);
		IntVar idx = VF.enumerated("idx", 0, 50, s);
		RangePropagator test = new RangePropagator(x, idx, -5, 10, 5000);
		Assert.assertEquals(test.isEntailed(), ESat.UNDEFINED);
		idx.instantiateTo(0, Cause.Null);
		x.updateUpperBound(-1, Cause.Null);
		Assert.assertEquals(test.isEntailed(), ESat.UNDEFINED);
		x.updateUpperBound(-10, Cause.Null);
		Assert.assertEquals(test.isEntailed(), ESat.TRUE);
	}

	@Test(dependsOnMethods = "testRangeGetter")
	public void testPropagate() throws ContradictionException {
		Solver s = new Solver();
		IntVar x = VF.bounded("X", Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, s);
		IntVar idx = VF.enumerated("idx", 0, 50, s);
		RangePropagator test = new RangePropagator(x, idx, -5, 10, 5000);
		x.instantiateTo(1, Cause.Null);
		test.propagate(0);
		Assert.assertEquals(idx.getValue(), 1);
	}
}
