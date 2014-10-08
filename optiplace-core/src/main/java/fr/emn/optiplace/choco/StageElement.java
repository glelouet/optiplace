package fr.emn.optiplace.choco;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.ESat;


/**
 * A constraint to represent the mapping of a value X to a value Y following a
 * partition of X's values <br />
 * considers a variable X, a variable Y, a variable index idx, a table of
 * thresholds thresholds and a table of values values of length &gt;
 * thresholds.length
 * <ul>
 * <li>idx = smaller value such as thresholds[idx]>X. If no threshold is gt X,
 * then threshold.length</li>
 * <li>Y=values[idx]</li>
 * </ul>
 *
 * @author Guillaume Le Louët
 */
public class StageElement extends Propagator<IntVar>
// extends AbstractTernIntSConstraint
{

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(StageElement.class);

	private int[] thresholds;
	private int[] values;


	/** faster algorithms if the values are strictly inreasing */
	private boolean valuesIncreasing = false;

	/**
	 * @param index
	 * @param x
	 * @param y
	 * @param thresholds
	 * a table of increasing int
	 * @param values
	 * values to associate to y, should be of length thresholds.length+1
	 */
	public StageElement(IntVar index, IntVar x, IntVar y, int[] thresholds,
			int[] values) {
		super(index, x, y);
		this.thresholds = thresholds;
		this.values = values;
		assert thresholds.length < values.length;
		assert checkIncreasingThresholds();
		valuesIncreasing = checkIncreasingValues();
	}

	/**
	 * @return true if all elemnts of Thresholds are strictly increasing
	 */
	public boolean checkIncreasingThresholds() {
		for (int i = 1; i < thresholds.length; i++) {
			if (thresholds[i] <= thresholds[i - 1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * remove duplicate following values, and their corresponding thresholds
	 *
	 * @return was their duplicates ?
	 */
	public boolean reduceValues() {
		int duplicates = 0;
		for (int i = 1; i < values.length; i++) {
			if (values[i] == values[i - 1]) {
				duplicates++;
			}
		}
		if (duplicates > 0) {
			int[] n_values = new int[values.length - duplicates];
			int[] n_thresholds = new int[thresholds.length - duplicates];
			int skipped = 0;
			n_values[0] = values[0];
			for (int i = 1; i < values.length; i++) {
				if (values[i] == values[i - 1]) {
					skipped++;
				} else {
					n_values[i - skipped] = values[i];
					n_thresholds[i - skipped - 1] = thresholds[i - 1];
				}
			}
			values = n_values;
			thresholds = n_thresholds;
		}
		return duplicates > 0;
	}

	/**
	 * @return true if all the values are in increasing or constant order
	 */
	public boolean checkIncreasingValues() {
		for (int i = 1; i < values.length; i++) {
			if (values[i - 1] > values[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the index variable
	 */
	public final IntVar index() {
		return vars[0];
	}

	/**
	 * @return the x variable
	 */
	public final IntVar X() {
		return vars[1];
	}

	/**
	 * @return the y variable
	 */
	public final IntVar Y() {
		return vars[2];
	}

	public final int[] getValues() {
		return values;
	}

	public final int[] getThresholds() {
		return thresholds;
	}

	/**
	 * @return the minimum value that can be reached by X if the index is idx
	 */
	public int getMinX(int idx) {
		return idx < 1 ? Integer.MIN_VALUE : thresholds[idx - 1];
	}

	/**
	 * @return the maximum value that can be reached by X if the index is idx
	 */
	public int getMaxX(int idx) {
		return idx >= thresholds.length ? Integer.MIN_VALUE : thresholds[idx] - 1;
	}

	public int getY(int idx) {
		return values[idx];
	}

	/**
	 * In case the values are increasing, much better performances.
	 *
	 * @param y
	 * the key to find in values starting from index offset
	 * @param offset
	 * the minimum index to look for the key y
	 * @return the first index idx &ge; offset such as values[idx]=y, or -1 if not
	 * present
	 */
	public int getYIdx(int y, int offset) {
		if (offset >= values.length) {
			return -1;
		}
		if (valuesIncreasing) {
			if (offset > 0 && values[offset - 1] >= y) {
				// since we're increasing, maybe offset-1 was the last found. in
				// that case check if offset is correct or return -1
				return values[offset] == y ? offset : -1;
			}
			int idx = Arrays.binarySearch(values, offset, values.length, y);
			if (idx < offset) {
				return -1;
			}
			// Arrays.binarySearch can return ANY index with the good key, we
			// want the first one.
			while (idx > offset && values[idx - 1] == y) {
				idx--;
			}
			return idx;
		} else {// values not increasing : full search starting from offset
			for (int i = offset; i < values.length; i++) {
				if (values[i] == y) {
					return i;
				}
			}
			return -1;
		}
	}

	/**
	 * @return the first possible idx value so that values[idx]=y, or -1 if not
	 * present
	 */
	public int getYIdx(int y) {
		return getYIdx(y, 0);
	}

	/**
	 * get the max index of x in the thresholds, such as x &lt; thresholds[idx]
	 */
	public int getXidx(int x) {
		return findIndex(x, thresholds);
	}

	/**
	 * @param x
	 * the int to find in the array
	 * @param thresholds
	 * the table of increasing ints to lcate x between.
	 * @return the smaller index idx such as threshold[idx]>x. can be
	 * thresholds.length if x>thresholds.last
	 */
	public static int findIndex(int x, int[] thresholds) {
		int min = 0;
		int max = thresholds.length;
		while (max - min > 1) {
			int pivot = (min + max) / 2;
			if (thresholds[pivot] > x) {
				max = pivot;
			} else {
				min = pivot;
			}
		}
		int ret = thresholds.length > 0 && thresholds[min] > x ? min : max;
		return ret;
	}

	@Override
	public String toString() {
		return "StageElement(thres:" + Arrays.asList(thresholds) + ";vals:"
				+ Arrays.asList(values) + ")";
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ESat isEntailed() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	// // TODO WTF ? copied from Element
	// @Override
	// public int getFilteredEventMask(int idx) {
	// if (vars[idx].hasEnumeratedDomain()) {
	// return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
	// } else {
	// return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
	// }
	// }
	//
	// @Override
	// public String pretty() {
	// return "StageElement(x:" + X().pretty() + ";thres:"
	// + StringUtils.pretty(thresholds) + ";idx:" + index().pretty()
	// + ";vals:" + StringUtils.pretty(values) + ";y:" + Y().pretty()
	// + ")";
	// }
	//
	// @Override
	// public void propagate() throws ContradictionException {
	// updateIndex();
	// updateXFromIndex();
	// updateYFromIndex();
	// }
	//
	// /**
	// * update the index values using X, Y, threshold and values data.
	// *
	// * @throws ContradictionException
	// */
	// public boolean updateIndex() throws ContradictionException {
	// IntVar index = index();
	// IntVar x = X();
	// IntVar y = Y();
	// boolean ret = false;
	//
	// // first find the index due to X values and the index restriction
	// int minFeasibleIndex = Math.max(findIndex(x.getLB(), thresholds),
	// index.getLB());
	// int maxFeasibleIndex = Math.min(findIndex(x.getUB(), thresholds),
	// index.getUB());
	//
	// // then remove all the index that lead to values outside of Y
	// while (minFeasibleIndex <= maxFeasibleIndex
	// && !y.canBeInstantiatedTo(values[minFeasibleIndex])) {
	// minFeasibleIndex = index.getNextDomainValue(minFeasibleIndex);
	// }
	// while (minFeasibleIndex <= maxFeasibleIndex
	// && !y.canBeInstantiatedTo(values[maxFeasibleIndex])) {
	// maxFeasibleIndex = index.getPrevDomainValue(maxFeasibleIndex);
	// }
	//
	// if (minFeasibleIndex > maxFeasibleIndex) {
	// fail();
	// }
	//
	// // System.err.println("updating index to [" + minFeasibleIndex + " : "
	// // + maxFeasibleIndex+"]");
	// ret |= index.updateInf(minFeasibleIndex, this, false);
	// ret |= index.updateSup(maxFeasibleIndex, this, false);
	//
	// // if index is enumerated, we enumerate its values and check
	// if (index.hasEnumeratedDomain()
	// && (x.hasEnumeratedDomain() || y.hasEnumeratedDomain())) {
	// int i = index.getLB();
	// while (i <= index.getUB()) {
	// if (index.canBeInstantiatedTo(i)) {
	// // if x interval removed the value from index don't check in
	// // y
	// // interval
	// boolean removed = false;
	// if (x.hasEnumeratedDomain()) {
	// int max = getMaxX(i);
	// int min = getMinX(i);
	// // smallest value in x >= min
	// int xNext = x.getDomain().getNextValue(min - 1);
	// if (xNext > max || xNext > x.getUB()) {
	// ret |= index.removeVal(i, this, false);
	// removed = true;
	// }
	// }
	// if (!removed && y.hasEnumeratedDomain()
	// && !y.canBeInstantiatedTo(values[i])) {
	// ret |= index.removeVal(i, this, false);
	// }
	// }
	// i = index.getNextDomainValue(i);
	// }
	// }
	// return ret;
	// }
	//
	// /**
	// * restrict the values of X to the ranges of values index can reach in
	// * {@link #thresholds}
	// */
	// public boolean updateXFromIndex() throws ContradictionException {
	// IntVar index = index();
	// IntVar x = X();
	// boolean ret = false;
	// ret |= x.updateInf(getMinX(index.getLB()), this, false);
	// ret |= x.updateSup(getMaxX(index.getUB()), this, false);
	// if (x.hasEnumeratedDomain() && index.hasEnumeratedDomain()) {
	// for (int i = index.getLB() + 1; i <= index.getUB() - 1; i++) {
	// if (!index.canBeInstantiatedTo(i)) {
	// ret |= x.removeInterval(getMinX(i), getMaxX(i), this, false);
	// }
	// }
	// }
	// return ret;
	// }
	//
	// /** restrict the values of Y to the values Y can reach in {@link #values}
	// */
	// public boolean updateYFromIndex() throws ContradictionException {
	// IntVar index = index();
	// IntVar y = Y();
	//
	// // reduce min and max
	// int min = values[index.getLB()], max = min;
	// boolean ret = false;
	// if (valuesIncreasing) {
	// max = values[index.getUB()];
	// } else {
	// for (int i = index.getLB(); i < index.getUB(); i = index
	// .getNextDomainValue(i)) {
	// if (values[i] < min) {
	// min = values[i];
	// }
	// if (values[i] > max) {
	// max = values[i];
	// }
	// }
	// }
	// ret |= y.updateInf(min, this, false);
	// ret |= y.updateSup(max, this, false);
	//
	// // remove values of y with no correct idx for y=values[idx]
	// if (y.hasEnumeratedDomain()) {
	// for (int val = y.getLB(); val <= y.getUB(); val = y
	// .getNextDomainValue(val)) {
	// int i = getYIdx(val);
	// if (!index.canBeInstantiatedTo(i)) {
	// y.removeVal(val, this, false);
	// }
	// }
	// }
	// return ret;
	//
	// }
	//
	// @Override
	// public void awakeOnInst(int i) throws ContradictionException {
	// if (i == 0) {// index instantiated
	// updateXFromIndex();
	// Y().instantiate(values[index().getVal()], this, false);
	// } else if (i == 1) {// x instantiated
	// index().instantiate(findIndex(X().getVal(), thresholds), this,
	// false);
	// Y().instantiate(values[index().getVal()], this, false);
	// } else {// y instanciated
	// propagate();
	// }
	// }
	//
	// @Override
	// public void awakeOnInf(int i) throws ContradictionException {
	// if (i == 0) {// index modified
	// X().updateInf(getMinX(index().getLB()), this, false);
	// updateYFromIndex();
	// } else if (i == 1) {// X modified
	// if (index().updateInf(getXidx(X().getLB()), this, false)) {
	// updateYFromIndex();
	// }
	// } else {
	// updateIndex();
	// updateXFromIndex();
	// }
	// }
	//
	// @Override
	// public void awakeOnSup(int i) throws ContradictionException {
	// if (i == 0) {// index modified
	// X().updateSup(getMaxX(index().getUB()), this, false);
	// updateYFromIndex();
	// } else if (i == 1) {// X modified
	// if (index().updateSup(getXidx(X().getUB()), this, false)) {
	// updateYFromIndex();
	// }
	// } else {
	// updateIndex();
	// updateXFromIndex();
	// }
	// }
	//
	// @Override
	// public void awakeOnRem(int i, int x) throws ContradictionException {
	// if (i == 0) {// index modified : just propagate to X and Y
	// updateXFromIndex();
	// updateYFromIndex();
	// } else {// X or Y modified : we need to check index then X and Y again.
	// propagate();
	// }
	// }
	//
	// @Override
	// public Boolean isEntailed() {
	// IntVar index = index(), x = X(), y = Y();
	// if (index.isInstantiated()) {
	// if (y.isInstantiatedTo(values[index.getVal()])) {// y=values[i]
	// // correct
	// if (x.getLB() >= getMinX(index.getVal())
	// && x.getUB() <= getMaxX(index.getVal())) {
	// return true;
	// }
	// }
	// }
	// for (int i = index.getLB(); i <= index.getUB(); i = index
	// .getNextDomainValue(i)) {
	// // is there an index feasible ?
	// if (y.canBeInstantiatedTo(values[i])) {
	// int commonVal = x.getNextDomainValue(getMinX(i) - 1);
	// if (commonVal <= x.getUB() && commonVal <= getMaxX(i)) {
	// return null;
	// }
	// }
	// }
	// return false;
	// }
	//
	// // @Override
	// @Override
	// public boolean isSatisfied(int[] tuple) {
	// int idx = tuple[0];
	// int x = tuple[1];
	// int y = tuple[2];
	// if (idx < 0 || idx > thresholds.length || y != values[idx]) {
	// return false;
	// }
	// return idx == findIndex(x, thresholds);
	// }
}
