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

import java.util.BitSet;
import java.util.List;

import common.Constant;
import common.logging.ChocoLogging;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.constraints.SConstraint;
import org.chocosolver.solver.search.integer.AbstractIntVarSelector;
import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.actionModel.ActionModel;
import entropy.view.scheduling.actionModel.ActionModels;
import entropy.view.scheduling.actionModel.VirtualMachineActionModel;
import entropy.view.scheduling.actionModel.slice.DemandingSlice;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * An heuristic to branch first on the start moment of actions that arrive on
 * nodes without any outgoing actions.
 * 
 * @author Fabien Hermenier
 */
public class PureIncomingFirst extends AbstractIntVarSelector {

	private final IntVar[] hoster;

	private final IntVar[] starts;

	private final int[] oldPos;

	private final BitSet[] outs;

	private final BitSet[] ins;

	@SuppressWarnings("rawtypes")
	private final List<SConstraint> lateConstraints;

	/**
	 * Make a new heuristics
	 * 
	 * @param solver
	 *            the solver to use
	 * @param actions
	 *            the actions to consider.
	 * @param costConstraints
	 *            the constraints to add the first time this heuristic is
	 *            called.
	 */
	@SuppressWarnings("rawtypes")
	public PureIncomingFirst(SchedulingView plan, List<ActionModel> actions,
			List<SConstraint> costConstraints) {
		super(plan.getProblem(), ActionModels.extractStarts(actions
				.toArray(new ActionModel[actions.size()])));
		pb = plan.getProblem();
		lateConstraints = costConstraints;
		Configuration cfg = pb.getSourceConfiguration();

		hoster = new IntVar[plan.getVirtualMachineActions().size()];
		starts = new IntVar[plan.getVirtualMachineActions().size()];
		List<VirtualMachineActionModel> vmActions = plan
				.getVirtualMachineActions();
		VirtualMachine[] vms = new VirtualMachine[vmActions.size()];
		oldPos = new int[c.nbVMs()];
		outs = new BitSet[pb.nodes().length];
		ins = new BitSet[pb.nodes().length];
		for (int i = 0; i < pb.nodes().length; i++) {
			outs[i] = new BitSet();
			ins[i] = new BitSet();
		}

		for (int i = 0; i < hoster.length; i++) {
			VirtualMachineActionModel action = vmActions.get(i);
			DemandingSlice slice = action.getDemandingSlice();
			if (slice != null) {
				IntVar h = vmActions.get(i).getDemandingSlice().hoster();
				IntVar s = vmActions.get(i).getDemandingSlice().start();
				hoster[i] = h;
				starts[i] = s;
				vms[i] = action.getVirtualMachine();
				Node n = cfg.getLocation(vms[i]);
				if (n == null) {
					oldPos[i] = -1;
				} else {
					oldPos[i] = pb.location(n);
					outs[pb.location(n)].set(i); // VM i was on node n
				}
			}
		}
	}

	private boolean first = true;

	private final ReconfigurationProblem pb;

	@Override
	public IntVar selectVar() {
		if (first) {
			first = false;
			for (SConstraint<?> sc : lateConstraints) {
				pb.postCut(sc);
			}
			try {
				pb.propagate();
			} catch (ContradictionException e) {
				pb.setFeasible(false);
				pb.post(Constant.FALSE);
			}
		}
		for (BitSet in : ins) {
			in.clear();
		}

		BitSet stays = new BitSet();
		// At this moment, all the hoster of the demanding slices are computed.
		// for each node, we compute the number of incoming and outgoing
		for (int i = 0; i < hoster.length; i++) {
			if (hoster[i] != null && hoster[i].isInstantiated()) {
				int newPos = hoster[i].getVal();
				if (oldPos[i] != -1 && newPos != oldPos[i]) {
					// The VM has move
					ins[newPos].set(i);
				} else if (oldPos[i] != -1 && newPos == oldPos[i]) {
					stays.set(i);
				}
			}
		}

		// TODO: start with nodes with a sufficient amount of free resources at
		// startup
		for (int x = 0; x < outs.length; x++) {
			if (outs[x].cardinality() == 0) { // no outgoing VMs
				BitSet in = ins[x];
				for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
					if (starts[i] != null && !starts[i].isInstantiated()) {
						return starts[i];
					}
				}
			}
		}
		// ChocoLogging.getBranchingLogger().finest("No more pure incoming");
		// TODO: Decreasing stay at end
		// TODO: association between slice on the same node
		for (int i = stays.nextSetBit(0); i >= 0; i = stays.nextSetBit(i + 1)) {
			if (starts[i] != null && !starts[i].isInstantiated()) {
				return starts[i];
			}
		}
		for (int x = 0; x < outs.length; x++) {
			BitSet in = ins[x];
			// For all the incoming
			for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
				if (starts[i] != null && !starts[i].isInstantiated()) {
					return starts[i];
				}
			}
		}

		for (IntVar start2 : starts) {
			IntVar start = start2;
			if (start2 != null && !start.isInstantiated()) {
				return start;
			}
		}
		ChocoLogging.getBranchingLogger().finest(
				"No more variables to instantiate here");
		return null;
	}
}
