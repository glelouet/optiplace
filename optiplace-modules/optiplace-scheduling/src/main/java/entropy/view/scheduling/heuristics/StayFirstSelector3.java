/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.view.scheduling.heuristics;

import entropy.view.scheduling.choco.SatisfyDemandingSliceHeights;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import gnu.trove.TIntHashSet;
import gnu.trove.TLongIntHashMap;
import common.util.iterators.DisposableIntIterator;
import org.chocosolver.solver.search.ValSelector;
import org.chocosolver.solver.variables.IntVar;

/**
 * A heuristic to try to assign the virtual machines to migrate or to resume to
 * its current (or previous) location. If it is not possible, it consider the
 * current residual capacity of the nodes to choose the one to test.
 * 
 * @author Fabien Hermenier
 */
public class StayFirstSelector3 implements ValSelector<IntVar> {
	public enum Option {
		wfMem, wfCPU, bfMem, bfCPU
	}

	private Option opt;

	private TIntHashSet[] favorites;
	/** The previous location of the running VMs. */
	private TLongIntHashMap oldLocation = new TLongIntHashMap();

	private SatisfyDemandingSliceHeights pack;

	// private ReconfigurationProblem rp;

	/**
	 * Build a selector for a specific solver.
	 * 
	 * @param s
	 *            the solver
	 * @param pack
	 *            the pack constraint
	 * @param o
	 *            the option to customize the heuristic
	 */
	public StayFirstSelector3(ReconfigurationProblem s,
			TLongIntHashMap oldLocation, SatisfyDemandingSliceHeights pack,
			TIntHashSet[] favorites, Option o) {
		opt = o;
		this.pack = pack;
		// rp = s;
		this.favorites = favorites;

		this.oldLocation = oldLocation;
	}

	/**
	 * Get the bin with the maximum remaining space.
	 * 
	 * @param place
	 *            the hoster variable of the slice to place.
	 * @param dim
	 *            0 for the CPU dimension, 1 for the memory dimension
	 * @return {@code -1} if no host is available, otherwise the index of the
	 *         node.
	 */
	private int worstFit(IntVar place, int dim) {
		DisposableIntIterator ite = place.getDomain().getIterator();
		int[] maxIdxs = new int[favorites.length];
		int[] maxVals = new int[favorites.length];

		// Initialization
		for (int i = 0; i < maxIdxs.length; i++) {
			maxIdxs[i] = -1;
			maxVals[i] = -1;
		}

		try {
			while (ite.hasNext()) {
				int bIdx = ite.next();
				// Get the group its belong to
				for (int i = 0; i < favorites.length; i++) {
					if (favorites[i].contains(bIdx)) { // Got a candidate in
						// group i
						int bVal = dim == 0 ? pack.getRemainingCPU(bIdx) : pack
								.getRemainingMemory(bIdx);
						if (bVal > maxVals[i]) {
							maxVals[i] = bVal;
							maxIdxs[i] = bIdx;
						}
					}
				}
			}
		} finally {
			ite.dispose();
		}
		for (int i = 0; i < maxVals.length; i++) {
			if (maxIdxs[i] >= 0) {
				// Plan.logger.debug("Choose value in group " + i);
				return maxIdxs[i];
			}
		}
		return -1;
	}

	/**
	 * Get the bin with the minimum remaining space.
	 * 
	 * @param place
	 *            the hoster variable of the slice to place.
	 * @param dim
	 *            0 for the CPU dimension, 1 for the memory dimension
	 * @return {@code -1} if no host is available, otherwise the index of the
	 *         node.
	 */
	private int bestFit(IntVar place, int dim) {
		int minIdx = -1;
		int minVal = Integer.MAX_VALUE;

		for (int bIdx = place.getLB(); bIdx <= place.getUB(); bIdx = place
				.getDomain().getNextValue(bIdx)) {
			int bVal = dim == 0 ? pack.getRemainingCPU(bIdx) : pack
					.getRemainingMemory(bIdx);
			if (bVal < minVal) {
				minVal = bVal;
				minIdx = bIdx;
			}
		}
		return minIdx;
	}

	@Override
	public int getBestVal(IntVar var) {
		int val = oldLocation.get(var.getIndex());
		// Check wether the VM can stay on the same node.
		if (oldLocation.containsKey(var.getIndex())
				&& var.canBeInstantiatedTo(val)) {
			// Plan.logger.debug/*ChocoLogging.getSearchLogger().finest*/(var.pretty()
			// + " stay on " + val + " " + rp.getNode(val));
			return val;
		}
		int to;
		switch (opt) {
			case wfCPU :
				to = worstFit(var, 0);
				break;
			case wfMem :
				to = worstFit(var, 1);
				break;
			case bfCPU :
				to = bestFit(var, 0);
				break;
			case bfMem :
				to = bestFit(var, 1);
				break;
			default :
				to = var.getLB();
		}
		// Plan.logger.debug/*ChocoLogging.getSearchLogger().finest*/(var.pretty()
		// + " move from " + val + " to " + to);
		return to;
	}
}
