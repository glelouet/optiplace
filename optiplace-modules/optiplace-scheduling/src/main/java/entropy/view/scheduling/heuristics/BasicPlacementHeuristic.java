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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.chocosolver.solver.search.integer.branching.AssignVar;
import org.chocosolver.solver.search.integer.valselector.MinVal;
import org.chocosolver.solver.search.integer.varselector.StaticVarOrder;
import org.chocosolver.solver.branch.AbstractIntBranchingStrategy;
import org.chocosolver.solver.variables.Var;
import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.actionModel.ActionModel;
import entropy.view.scheduling.actionModel.MigratableActionModel;
import entropy.view.scheduling.actionModel.ReInstantiateActionModel;
import entropy.view.scheduling.actionModel.ResumeActionModel;
import entropy.view.scheduling.actionModel.VirtualMachineActionModel;
import entropy.view.scheduling.choco.SatisfyDemandingSliceHeights;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.ConfigurationUtils;
import fr.emn.optiplace.configuration.ManagedElementSet;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.ResourcePicker;
import fr.emn.optiplace.configuration.SimpleManagedElementSet;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.configuration.VirtualMachineComparator;
import fr.emn.optiplace.core.heuristics.HosterVarSelector;
import fr.emn.optiplace.core.heuristics.SelectMovingVMs;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.SearchHeuristic;
import gnu.trove.TIntHashSet;
import gnu.trove.TLongIntHashMap;

/**
 * A placement heuristic focused on each VM. First place the VMs, then plan the
 * changes.
 *
 * @author Fabien Hermenier
 */
public class BasicPlacementHeuristic implements SearchHeuristic {

	private final SatisfyDemandingSliceHeights packing = null;

	/** To compare VMs in a descending order, wrt. their memory consumption. */
	private final VirtualMachineComparator dsc = new VirtualMachineComparator(
			false, ResourcePicker.VMRc.memoryConsumption);

	private final SchedulingView view;

	public BasicPlacementHeuristic(SchedulingView view) {
		this.view = view;
	}

	@Override
	public List<AbstractIntBranchingStrategy> getHeuristics(
			ReconfigurationProblem rp) {
		ManagedElementSet<VirtualMachine> managed = rp.getSourceConfiguration()
				.getAllVirtualMachines();
		ArrayList<AbstractIntBranchingStrategy> ret = new ArrayList<AbstractIntBranchingStrategy>();
		Configuration src = rp.getSourceConfiguration();

		// Compute the nodes that will not leave resources. Awesome candidates
		// to place VMs
		// on as they will be scheduled asap.
		TIntHashSet[] favorites = new TIntHashSet[2];
		favorites[0] = new TIntHashSet();
		favorites[1] = new TIntHashSet();
		if (!managed.isEmpty()) {

			// Composed with nodes that do not host misplaced VMs.
			ManagedElementSet<Node> involded = src.getAllNodes().clone();
			for (Node n : involded) {
				favorites[0].add(rp.node(n));
			}
			for (VirtualMachine vm : managed) {
				Node n = src.getLocation(vm);
				if (n != null && involded.remove(n)) {
					int i = rp.node(n);
					favorites[0].remove(i);
					favorites[1].add(i);
				}
			}
			// Then remove nodes that have VMs that must be suspended or
			// terminated
			for (VirtualMachine vm : rp.getSourceConfiguration()
					.getAllVirtualMachines()) {
				if (src.isRunning(vm)) {
					Node n = src.getLocation(vm);
					int i = rp.node(n);
					if (n != null && involded.remove(n)) {
						favorites[1].add(i);
						favorites[0].remove(i);
					}
				}
				// Don't care about sleeping that stay sleeping
			}
			// for (VirtualMachine vm : rp.getFutureTerminated()) {
			// Node n = src.getLocation(vm);
			// int i = rp.node(n);
			// if (involded.remove(n)) {
			// favorites[1].add(i);
			// favorites[0].remove(i);
			// }
			// }
			// System.err.println(involded.size() + " (" + favorites[0].size() +
			// ") idylic nodes over " + src.getAllNodes().size() + " (" +
			// favorites[1].size() + ")");
		}

		// Get the VMs to move
		ManagedElementSet<VirtualMachine> onBadNodes = new SimpleManagedElementSet<VirtualMachine>();

		for (Node n : ConfigurationUtils.futureOverloadedNodes(src)) {
			onBadNodes.addAll(src.getRunnings(n));
		}

		for (VirtualMachine vm : src.getSleepings()) {
				onBadNodes.add(vm);
		}

		ManagedElementSet<VirtualMachine> onGoodNodes = src.getRunnings()
				.minus(onBadNodes);

		Collections.sort(onGoodNodes, dsc);
		Collections.sort(onBadNodes, dsc);

		List<VirtualMachineActionModel> goodActions = view
				.getAssociatedActions(onGoodNodes);
		List<VirtualMachineActionModel> badActions = view
				.getAssociatedActions(onBadNodes);

		// Go for the VMgroup variable
		ManagedElementSet<VirtualMachine> relocalisables = rp
				.getFutureRunnings();
		TLongIntHashMap oldLocation = new TLongIntHashMap(relocalisables.size());

		for (VirtualMachine vm : relocalisables) {
			int idx = rp.vm(vm);
			VirtualMachineActionModel a = view
					.getAssociatedVirtualMachineAction(idx);
			if (a.getClass() == MigratableActionModel.class
					|| a.getClass() == ResumeActionModel.class
					|| a.getClass() == ReInstantiateActionModel.class) {
				oldLocation.put(a.getDemandingSlice().hoster().getIndex(),
						rp.getCurrentLocation(idx));
			}
		}

		// Get the VMs to move for exclusion issue
		ManagedElementSet<VirtualMachine> vmsToExlude = rp
				.getSourceConfiguration().getAllVirtualMachines().clone();
		Collections.sort(vmsToExlude, dsc);
		if (managed.isEmpty()) {
			ret.add(new AssignVar(new SelectMovingVMs(rp, vmsToExlude),
					new StayFirstSelector2(view, packing,
							StayFirstSelector2.Option.bfCPU)));
		} else {
			ret.add(new AssignVar(new SelectMovingVMs(rp, vmsToExlude),
					new StayFirstSelector3(rp, oldLocation, packing, favorites,
							StayFirstSelector3.Option.bfCPU)));
		}

		for (ManagedElementSet<VirtualMachine> vms : rp.getVMGroups()) {
			ManagedElementSet<VirtualMachine> sorted = vms.clone();
			Collections.sort(sorted, dsc);
			List<VirtualMachineActionModel> inGroupActions = view
					.getAssociatedActions(sorted);
			HosterVarSelector selectForInGroups = new HosterVarSelector(rp,
					inGroupActions.toArray(new VirtualMachine[]{}));
			if (managed.isEmpty()) {
				ret.add(new AssignVar(selectForInGroups,
						new StayFirstSelector2(view, packing,
								StayFirstSelector2.Option.bfCPU)));
			} else {
				ret.add(new AssignVar(selectForInGroups,
						new StayFirstSelector3(rp, oldLocation, packing,
								favorites, StayFirstSelector3.Option.bfCPU)));
			}
		}
		HosterVarSelector selectForBads = new HosterVarSelector(rp,
				badActions.toArray(new VirtualMachine[]{}));
		if (managed.isEmpty()) {
			ret.add(new AssignVar(selectForBads, new StayFirstSelector2(view,
					packing, StayFirstSelector2.Option.bfCPU)));
		} else {
			ret.add(new AssignVar(selectForBads, new StayFirstSelector3(rp,
					oldLocation, packing, favorites,
					StayFirstSelector3.Option.bfCPU)));
		}

		HosterVarSelector selectForGoods = new HosterVarSelector(rp,
				goodActions.toArray(new VirtualMachine[]{}));
		if (managed.isEmpty()) {
			ret.add(new AssignVar(selectForGoods, new StayFirstSelector2(view,
					packing, StayFirstSelector2.Option.bfCPU)));
		} else {
			ret.add(new AssignVar(selectForGoods, new StayFirstSelector3(rp,
					oldLocation, packing, favorites,
					StayFirstSelector3.Option.bfCPU)));
		}

		// VMs to run
		ManagedElementSet<VirtualMachine> vmsToRun = rp
				.getSourceConfiguration().getWaitings()
				.minus(rp.getFutureWaitings());// .clone();

		// vmsToRun.removeAll(rp.getFutureWaitings());
		List<VirtualMachineActionModel> runActions = view
				.getAssociatedActions(vmsToRun);
		HosterVarSelector selectForRuns = new HosterVarSelector(rp,
				runActions.toArray(new VirtualMachine[]{}));
		if (managed.isEmpty()) {
			ret.add(new AssignVar(selectForRuns, new StayFirstSelector2(view,
					packing, StayFirstSelector2.Option.bfCPU)));
		} else {
			ret.add(new AssignVar(selectForRuns, new StayFirstSelector3(rp,
					oldLocation, packing, favorites,
					StayFirstSelector3.Option.bfCPU)));
		}

		// /SCHEDULING PROBLEM
		List<ActionModel> actions = new ArrayList<ActionModel>();
		for (VirtualMachineActionModel vma : view.getVirtualMachineActions()) {
			actions.add(vma);
		}
		ret.add(new AssignVar(new PureIncomingFirst(view, actions, rp
				.getCostConstraints()), new MinVal()));
		Var obj = rp.getObjective();
		if (obj != null) {
			ret.add(new AssignVar(new StaticVarOrder(rp, new IntVar[]{
					view.getEnd(), (IntVar) obj}), new MinVal()));
		} else {
			ret.add(new AssignVar(new StaticVarOrder(rp,
					new IntVar[]{view.getEnd()}), new MinVal()));

		}

		return ret;
	}
}
