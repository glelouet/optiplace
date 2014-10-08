package fr.emn.optiplace.choco;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.VF;

/**
 *
 * @author guillaume Le Louët
 *
 */
public class StageElementTest {

	// /////////////////////////////////
	// test utilities methods, getters and setters
	// /////////////////////////////////

	@Test
	public void testfindYIdxIncreasingValues() {
		int[] xVals = {10, 20, 30, 40};
		int[] yVals = {1, 1, 5, 10, 50};
		IntVar mock = Mockito.mock(IntVar.class);
		test = new StageElement(mock, mock, mock, xVals,
				yVals);
		Assert.assertEquals(test.getYIdx(1, 0), 0);
		Assert.assertEquals(test.getYIdx(1, 1), 1);
		Assert.assertEquals(test.getYIdx(1, 2), -1);// last is at pos 1
		Assert.assertEquals(test.getYIdx(1, 4), -1);// random bad value
		Assert.assertEquals(test.getYIdx(1, 5), -1);// out of index
		Assert.assertEquals(test.getYIdx(5), 2);
		Assert.assertEquals(test.getYIdx(50), 4);
		Assert.assertEquals(test.getYIdx(50, 4), 4);
	}

	@Test
	public void testFindIndex() {
		int[] thresholds = {3, 10};
		Assert.assertEquals(StageElement.findIndex(-5, thresholds), 0);
		Assert.assertEquals(StageElement.findIndex(2, thresholds), 0);
		Assert.assertEquals(StageElement.findIndex(3, thresholds), 1);
		Assert.assertEquals(StageElement.findIndex(5, thresholds), 1);
		Assert.assertEquals(StageElement.findIndex(9, thresholds), 1);
		Assert.assertEquals(StageElement.findIndex(10, thresholds), 2);
		Assert.assertEquals(StageElement.findIndex(15, thresholds), 2);
		int[] thresholds2 = {1};
		Assert.assertEquals(StageElement.findIndex(0, thresholds2), 0);
		Assert.assertEquals(StageElement.findIndex(1, thresholds2), 1);
		Assert.assertEquals(StageElement.findIndex(2, thresholds2), 1);
	}

	@Test
	public void testReduceValues() {
		int[] xVals = {10, 20, 30, 40};
		int[] yVals = {1, 1, 5, 10, 50};
		IntVar mock = Mockito.mock(IntVar.class);
		test = new StageElement(mock, mock, mock, xVals, yVals);
		Assert.assertTrue(test.reduceValues());
		Assert.assertEquals(test.getValues(), new int[]{1, 5, 10, 50});
		Assert.assertEquals(test.getThresholds(), new int[]{20, 30, 40});
		Assert.assertFalse(test.reduceValues());
	}

	// //////////////////////////////
	// test propagation
	// //////////////////////////////

	IntVar x, y, idx;
	Solver solver;
	int[] thresholds, values;
	StageElement test;

	/**
	 * create values in the solver. The values are already correct in regard to
	 * the constraint.
	 */
	public void createTestCase1() {
		thresholds = new int[]{1, 4};
		values = new int[]{4, 5, 6};
		solver = new Solver();
		x = VF.bounded("x", Integer.MIN_VALUE, Integer.MAX_VALUE, solver);
		y = VF.bounded("y", Integer.MIN_VALUE, Integer.MAX_VALUE, solver);
		idx = VF.bounded("idx", Integer.MIN_VALUE, Integer.MAX_VALUE, solver);
		test = new StageElement(idx, x, y, thresholds, values);
		solver.post(new Constraint("testStageElement", test));
	}

	@Test
	public void testUpdateOnX() throws ContradictionException {
		// test with lowest interval(idx=0)
		for (int v : new int[]{-5, 0}) {
			createTestCase1();
			x.instantiateTo(v, Cause.Null);
			Assert.assertTrue(test.);
			Assert.assertFalse(test.updateXFromIndex());
			Assert.assertTrue(test.updateYFromIndex());
			Assert.assertEquals(idx.getLB(), 0);
			Assert.assertEquals(idx.getUB(), 0);
			Assert.assertEquals(y.getLB(), 4);
			Assert.assertEquals(y.getUB(), 4);
		}
		// test with internal interval
		for (int v : new int[]{1, 2, 3}) {
			createTestCase1();
			x.setVal(v);
			Assert.assertTrue(test.updateIndex());
			Assert.assertFalse(test.updateXFromIndex());
			Assert.assertTrue(test.updateYFromIndex());
			Assert.assertEquals(idx.getLB(), 1);
			Assert.assertEquals(idx.getUB(), 1);
			Assert.assertEquals(y.getLB(), 5);
			Assert.assertEquals(y.getUB(), 5);
		}
		// test with highest interval(idx=threshold.length)
		for (int v : new int[]{5, 6}) {
			createTestCase1();
			x.setVal(v);
			Assert.assertTrue(test.updateIndex());
			Assert.assertFalse(test.updateXFromIndex());
			Assert.assertTrue(test.updateYFromIndex());
			Assert.assertEquals(idx.getLB(), 2);
			Assert.assertEquals(idx.getUB(), 2);
			Assert.assertEquals(y.getLB(), 6);
			Assert.assertEquals(y.getUB(), 6);
		}
	}

	@Test
	public void testUpdateOnIndex() throws ContradictionException {
		// test with index=lowest
		createTestCase1();
		idx.setVal(0);
		Assert.assertFalse(test.updateIndex());
		Assert.assertTrue(test.updateXFromIndex());
		Assert.assertTrue(test.updateYFromIndex());
		Assert.assertEquals(x.getLB(), Choco.MIN_LOWER_BOUND);
		Assert.assertEquals(x.getUB(), 0);
		Assert.assertEquals(y.getLB(), 4);
		Assert.assertEquals(y.getUB(), 4);

		// test with index=middle
		createTestCase1();
		idx.setVal(1);
		Assert.assertFalse(test.updateIndex());
		Assert.assertTrue(test.updateXFromIndex());
		Assert.assertTrue(test.updateYFromIndex());
		Assert.assertEquals(x.getLB(), 1);
		Assert.assertEquals(x.getUB(), 3);
		Assert.assertEquals(y.getLB(), 5);
		Assert.assertEquals(y.getUB(), 5);

		// test with index=last
		createTestCase1();
		idx.setVal(2);
		Assert.assertFalse(test.updateIndex());
		Assert.assertTrue(test.updateXFromIndex());
		Assert.assertTrue(test.updateYFromIndex());
		Assert.assertEquals(x.getLB(), 4);
		Assert.assertEquals(x.getUB(), Choco.MAX_UPPER_BOUND);
		Assert.assertEquals(y.getLB(), 6);
		Assert.assertEquals(y.getUB(), 6);
	}

	@Test
	public void testUpdateOnY() throws ContradictionException {
		createTestCase1();
		y.setVal(4);
		Assert.assertTrue(test.updateIndex());
		Assert.assertTrue(test.updateXFromIndex());
		Assert.assertFalse(test.updateYFromIndex());
		Assert.assertEquals(x.getLB(), Choco.MIN_LOWER_BOUND);
		Assert.assertEquals(x.getUB(), 0);
		Assert.assertEquals(idx.getLB(), 0);
		Assert.assertEquals(idx.getUB(), 0);

		createTestCase1();
		y.setVal(5);
		Assert.assertTrue(test.updateIndex());
		Assert.assertTrue(test.updateXFromIndex());
		Assert.assertFalse(test.updateYFromIndex());
		Assert.assertEquals(x.getLB(), 1);
		Assert.assertEquals(x.getUB(), 3);
		Assert.assertEquals(idx.getLB(), 1);
		Assert.assertEquals(idx.getUB(), 1);

		createTestCase1();
		y.setVal(6);
		Assert.assertTrue(test.updateIndex());
		Assert.assertTrue(test.updateXFromIndex());
		Assert.assertFalse(test.updateYFromIndex());
		Assert.assertEquals(x.getLB(), 4);
		Assert.assertEquals(x.getUB(), Choco.MAX_UPPER_BOUND);
		Assert.assertEquals(idx.getLB(), 2);
		Assert.assertEquals(idx.getUB(), 2);
	}

	// /////////////////////////
	// test satisfaction check
	// //////////////////////////

	@Test
	public void testIsSatisfied() {
		createTestCase1();
		int[][] xVals = {{-5, 0}, {1, 2, 3}, {5, 6}};
		int[] idxVals = {0, 1, 2};
		int[] yVals = values;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < xVals[i].length; j++) {
				Assert.assertTrue(test.isSatisfied(new int[]{idxVals[i],
						xVals[i][j], yVals[i]}));
				Assert.assertFalse(test.isSatisfied(new int[]{idxVals[i] + 1,
						xVals[i][j], yVals[i]}));
				Assert.assertFalse(test.isSatisfied(new int[]{idxVals[i],
						xVals[i][j], yVals[i] + 1}));
			}
		}
	}
}
