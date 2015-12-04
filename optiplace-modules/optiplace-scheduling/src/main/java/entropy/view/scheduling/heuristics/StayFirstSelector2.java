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

import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.actionModel.MigratableActionModel;
import entropy.view.scheduling.actionModel.ResumeActionModel;
import entropy.view.scheduling.actionModel.VirtualMachineActionModel;
import entropy.view.scheduling.choco.SatisfyDemandingSliceHeights;
import fr.emn.optiplace.configuration.ManagedElementSet;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
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
public class StayFirstSelector2 implements ValSelector<IntVar> {

	public enum Option {
		wfMem, wfCPU, bfMem, bfCPU
	}

	private final Option opt;

	/** The previous location of the running VMs. */
	private TLongIntHashMap oldLocation = new TLongIntHashMap();

	private final SatisfyDemandingSliceHeights pack;

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
	public StayFirstSelector2(SchedulingView p,
			SatisfyDemandingSliceHeights pack, Option o) {
		ReconfigurationProblem problem = p.getProblem();
		opt = o;
		this.pack = pack;

		ManagedElementSet<VirtualMachine> relocalisables = problem
				.getSourceConfiguration().getAllVirtualMachines();
		oldLocation = new TLongIntHashMap(relocalisables.size());

		for (VirtualMachine vm : relocalisables) {
			int idx = problem.vm(vm);
			VirtualMachineActionModel a = p
					.getAssociatedVirtualMachineAction(idx);
			if (a.getClass() == MigratableActionModel.class
					|| a.getClass() == ResumeActionModel.class) {
				oldLocation.put(a.getDemandingSlice().hoster().getIndex(),
						problem.getCurrentLocation(idx));
			}
		}
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
		int maxIdx = -1;
		int maxVal = -1;

		try {
			while (ite.hasNext()) {
				int bIdx = ite.next();
				int bVal = dim == 0 ? pack.getRemainingCPU(bIdx) : pack
						.getRemainingMemory(bIdx);
				// Plan.logger.debug("Node N" + bIdx + " free=" + bVal);
				if (bVal > maxVal) {
					maxVal = bVal;
					maxIdx = bIdx;
				}
			}
		} finally {
			ite.dispose();
		}
		// Plan.logger.debug("Choose N" + maxIdx);
		return maxIdx;
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
		// int val = oldLocation.get(var.getIndex());
		// Check wether the VM can stay on the same node.
		// if (oldLocation.containsKey(var.getIndex()) &&
		// var.canBeInstantiatedTo(val)) {
		// Plan.logger.debug/* ChocoLogging.getSearchLogger().finest
		// */(var.pretty()
		// + " stay on " + val + " " + rp.getNode(val));
		// return val;
		// }
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
