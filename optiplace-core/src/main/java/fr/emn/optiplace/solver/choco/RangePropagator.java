/**
 *
 */

package fr.emn.optiplace.solver.choco;

import java.util.Arrays;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.impl.BitsetIntVarImpl;
import org.chocosolver.util.ESat;


/**
 * <p>
 * A propagator on an IntVar x, an index idx and a table of increasing values v.
 * Ensure x in v[idx-1]+1...v[idx], with v[-1]=-inf and v[v.length]=+inf
 * </p>
 * <p>
 * example : IntVar x is 0..50, IntVar idx is 0..3, ranges are 10,15,45<br />
 * if idx =3, then x must be within 46..50 ; if x = 10, then idx=0
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class RangePropagator extends Propagator<IntVar> {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RangePropagator.class);

	protected static boolean testIncreasing(int[] vars) {
		for (int i = 1; i < vars.length; i++) {
			if (vars[i] <= vars[i - 1]) {
				return false;
			}
		}
		return true;
	}

	private final int[] ranges;

	/**
	 * @param X
	 *          the variable to get the range
	 * @param index
	 *          the variable representing the range index, should be a
	 *          {@link BitsetIntVarImpl} as its domain will be holed
	 * @param ranges
	 *          the range limits, They must all be ordered(for any i,
	 *          ranges[i]<ranges[i+1] )
	 */
	public RangePropagator(IntVar X, IntVar index, int... ranges) {
		super(new IntVar[] {
		    X, index }, PropagatorPriority.LINEAR, true);
		assert testIncreasing(ranges) : "variables for range are not increasing " + Arrays.asList(ranges);
		this.ranges = ranges;
	}

	public IntVar getIdx() {
		return vars[1];
	}

	public IntVar getX() {
		return vars[0];
	}

	/**
	 * @return the number of ranges index. We can iterate them from 0 to
	 *         nbRanges-1
	 */
	public int getNbRanges() {
		return ranges.length + 1;
	}

	/**
	 * @param rangeIdx
	 *          the index of an internal range
	 * @return the lowest int of this range
	 */
	public int getLB(int rangeIdx) {
		if (rangeIdx == 0) {
			return Integer.MIN_VALUE;
		} else {
			return ranges[rangeIdx - 1] + 1;
		}
	}

	/**
	 * @param rangeIdx
	 *          the index of an internal range
	 * @return the highest int of this range
	 */
	public int getUB(int rangeIdx) {
		if (rangeIdx == ranges.length) {
			return Integer.MAX_VALUE;
		} else {
			return ranges[rangeIdx];
		}
	}

	/**
	 * get the range index a value belongs to
	 *
	 * @param val
	 *          the value
	 * @return the index of the internal range this value belongs to
	 */
	public int getIdx(int val) {
		if (val == Integer.MIN_VALUE) {
			return 0;
		}
		if (val == Integer.MAX_VALUE) {
			return getNbRanges();
		}
		int min = 0, max = getNbRanges();
		while (true) {
			int av = (min + max) / 2;
			if (getLB(av) > val) {
				max = av;
			} else if (getUB(av) < val) {
				min = av;
			} else {
				return av;
			}
		}
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		IntVar idx = getIdx();
		IntVar x = getX();
		idx.updateLowerBound(0, this);
		idx.updateUpperBound(getNbRanges() - 1, this);
		// we assume the index is a SetVar
		for (int i = 0; i < getNbRanges(); i++) {
			int lb = getLB(i), ub = getUB(i);
			if (idx.contains(i)) {
				// if the range appears in the index :
				// if X can not contain range, we remove it from the index
				if (!x.contains(lb) && (x.nextValue(lb) > ub || x.nextValue(lb) == Integer.MAX_VALUE)) {
					idx.removeValue(i, this);
				}
			} else {
				// if the range does not appear in the index: remove it from X
				for (int v = lb; v <= ub; v++) {
					x.removeValue(v, this);
				}
			}
		}
	}

	@Override
	public ESat isEntailed() {
		IntVar x = getX();
		IntVar index = getIdx();

		if (index.isInstantiated()) {
			// index instantiated : if x is in the range, it's ok.
			// if it's out of range it's false, else it's undefined.
			int idx = index.getValue();
			int LB = getLB(idx), UB = getUB(idx);
			if (x.getLB() >= LB && x.getUB() <= UB) {
				return ESat.TRUE;
			}
			if (!x.contains(LB) && x.nextValue(LB) > UB) {
				return ESat.FALSE;
			}
			return ESat.UNDEFINED;
		} else if (x.isInstantiated()) {
			// index is NOT instantiated, x is :
			// which range does x belongs to ?
			// if this range's index is out of idx, return false, else undefined
			int idx = getIdx(x.getValue());
			if (index.contains(idx)) {
				return ESat.UNDEFINED;
			} else {
				return ESat.FALSE;
			}
		} else {
			// none of them is instantiated : we check for each range available
			// if x
			// can belong to it. it is FALSe iff no range remaining in idx can
			// match x
			for (int i = index.getLB(); i <= index.getUB(); i = index.nextValue(i)) {
				int LB = getLB(i), UB = getUB(i);
				if (x.getLB() <= UB && x.getUB() >= LB) {
					return ESat.UNDEFINED;
				}
			}
			return ESat.FALSE;
		}
	}
}
