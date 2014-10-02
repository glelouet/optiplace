/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.core.heuristics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.util.iterators.DisposableIntIterator;
import solver.search.ValSelector;
import solver.variables.IntVar;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * A heuristic to select a group of nodes to associate to a group of VM. Try the
 * current group if possible.
 *
 * @author Fabien Hermenier
 */
public class NodeGroupSelector implements ValSelector<IntVar> {

	public enum Option {
		wfMem, wfCPU, inf, bfMem, bfCPU
	}

	;

	// private Option opt;

	private final ReconfigurationProblem rp;

	/** The previous location of the running VMs. */
	private final Map<IntVar, List<Integer>> locations;

	/**
	 * Build a selector for a specific solver.
	 *
	 * @param s
	 *            the solver
	 * @param o
	 *            the option to customize the heuristic
	 */
	public NodeGroupSelector(ReconfigurationProblem s, Option o) {
		// opt = o;
		rp = s;

		locations = new HashMap<IntVar, List<Integer>>();

		Set<Set<Node>> groups = rp.getNodesGroups();

		// Get the oldLocation of each group
		// Warn, may be on several groups !
		for (Set<VM> vmset : rp.getVMGroups()) {
			locations.put(rp.getVMGroup(vmset), new LinkedList<Integer>());
			for (VM vm : vmset) {
				for (Set<Node> nodeset : groups) {
					Node hoster = null;
					if (rp.getSourceConfiguration().isRunning(vm)) {
						hoster = rp.getSourceConfiguration().getLocation(vm);
					}
					if (hoster != null && nodeset.contains(hoster)) {
						List<Integer> l = locations.get(rp.getVMGroup(vmset));
						if (!l.contains(rp.getGroup(nodeset))) {
							l.add(rp.getGroup(nodeset));
						}
					}
				}
			}
		}
	}

	/**
	 * Get the index of the node with the biggest amount of free CPU resources
	 * that can host the slice.
	 *
	 * @param v
	 *            the assignment variable of the demanding slice
	 * @return the index of the node
	 */
	protected int worstFitCPU(IntVar v) {
		DisposableIntIterator ite = v.getDomain().getIterator();
		int bestIdx = ite.next();
		int bestCPU = rp.getUsedCPU(rp.node2(bestIdx)).getInf();
		while (ite.hasNext()) {
			int possible = ite.next();
			if (rp.getUsedCPU(rp.node2(possible)).getInf() < bestCPU) {
				bestIdx = possible;
				bestCPU = rp.getUsedCPU(rp.node2(possible)).getInf();
			}
		}
		ite.dispose();
		return bestIdx;
	}

	/**
	 * Get the index of the node with the biggest amount of free memory
	 * resources that can host the slice.
	 *
	 * @param v
	 *            the assignment variable of the demanding slice
	 * @return the index of the node
	 */
	protected int worstFitMem(IntVar v) {

		DisposableIntIterator ite = v.getDomain().getIterator();
		int bestIdx = ite.next();
		int bestMem = rp.getUsedMem(rp.node2(bestIdx)).getInf();
		while (ite.hasNext()) {
			int possible = ite.next();
			if (rp.getUsedMem(rp.node2(possible)).getInf() > bestMem) {
				bestIdx = possible;
				bestMem = rp.getUsedMem(rp.node2(possible)).getInf();
			}
		}
		ite.dispose();
		return bestIdx;
	}

	/**
	 * Get the index of the node with the smallest amount of free memory
	 * resources that can host the slice.
	 *
	 * @param v
	 *            the assignment variable of the demanding slice
	 * @return the index of the node
	 */
	protected int bestFitMem(IntVar v) {

		DisposableIntIterator ite = v.getDomain().getIterator();
		int bestIdx = ite.next();
		int bestMem = rp.getUsedMem(rp.node2(bestIdx)).getInf();
		while (ite.hasNext()) {
			int possible = ite.next();
			if (rp.getUsedMem(rp.node2(possible)).getInf() < bestMem) {
				bestIdx = possible;
				bestMem = rp.getUsedMem(rp.node2(possible)).getInf();
			}
		}
		ite.dispose();
		return bestIdx;
	}

	/**
	 * Get the index of the node with the smallest amount of free CPU resources
	 * that can host the slice.
	 *
	 * @param v
	 *            the assignment variable of the demanding slice
	 * @return the index of the node
	 */
	protected int bestFitCPU(IntVar v) {

		DisposableIntIterator ite = v.getDomain().getIterator();
		int bestIdx = ite.next();
		int bestMem = rp.getUsedMem(rp.node2(bestIdx)).getInf();
		while (ite.hasNext()) {
			int possible = ite.next();
			if (rp.getUsedMem(rp.node2(possible)).getInf() < bestMem) {
				bestIdx = possible;
				bestMem = rp.getUsedMem(rp.node2(possible)).getInf();
			}
		}
		ite.dispose();
		return bestIdx;
	}

	@Override
	public int getBestVal(IntVar var) {
		int v = -1;
		if (locations.containsKey(var)) {
			for (int i : locations.get(var)) {
				if (var.canBeInstantiatedTo(i)) {
					// Plan.logger.info("Same group for " + var.pretty());
					v = i;
					break;
				}
			}
			if (v == -1) {
				v = var.getInf();
				// Plan.logger.info("Another group for " + var.pretty());
			}
		} else {
			// Plan.logger.info("Another group for " + var.pretty());
			v = var.getInf();
		}
		return v;
	}
}
