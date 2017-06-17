/**
 *
 */
package fr.emn.optiplace.solver.choco;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class RangePropagatorTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RangePropagatorTest.class);

	@Test
	public void testRangeGetter() {
		Model s = new Model();
		IntVar x = s.intVar("X", Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, true);
		IntVar idx = s.intVar("idx", 0, 50, false);
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
		Model s = new Model();
		IntVar x = s.intVar("X", Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, true);
		IntVar idx = s.intVar("idx", 0, 50, false);
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
		Model s = new Model();
		IntVar x = s.intVar("X", Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, true);
		IntVar idx = s.intVar("idx", 0, 50, false);
		RangePropagator test = new RangePropagator(x, idx, -5, 10, 5000);
		x.instantiateTo(1, Cause.Null);
		test.propagate(0);
		Assert.assertEquals(idx.getValue(), 1);
	}
}
