/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.view.scheduling.choco;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.actionModel.ActionModel;
import entropy.view.scheduling.actionModel.slice.ConsumingSlice;
import entropy.view.scheduling.actionModel.slice.DemandingSlice;
import entropy.view.scheduling.actionModel.slice.Slice;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.View;

/**
 * A global constraint to help to plan all the slices in a reconfiguration
 * problem.
 * 
 * @author Fabien Hermenier
 */
public class SlicesPlanner extends EmptyView implements View {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SlicesPlanner.class);

	private final SchedulingView plan;

	public SlicesPlanner(SchedulingView plan) {
		this.plan = plan;
	}

	@Override
	public void associate(ReconfigurationProblem rp) {
		super.associate(rp);
		List<DemandingSlice> dS = new LinkedList<DemandingSlice>();
		List<ConsumingSlice> cS = new LinkedList<ConsumingSlice>();

		List<int[]> linked = new ArrayList<int[]>();
		int dIdx = 0;
		int cIdx = 0;
		List<ActionModel> allActions = new ArrayList<ActionModel>();
		allActions.addAll(plan.getNodeMachineActions());
		allActions.addAll(plan.getVirtualMachineActions());
		// System.err.println(rp.getNodeMachineActions());
		if (allActions.isEmpty()) {
			return;
		}
		for (ActionModel na : allActions) {
			if (na.getDemandingSlice() != null
					&& na.getConsumingSlice() != null) {
				linked.add(new int[]{dIdx, cIdx});
			}
			if (na.getDemandingSlice() != null) {
				dS.add(dIdx, na.getDemandingSlice());
				dIdx++;
			}
			if (na.getConsumingSlice() != null) {
				cS.add(cIdx, na.getConsumingSlice());
				cIdx++;
			}
		}

		Slice[] dSlices = dS.toArray(new Slice[dS.size()]);
		Slice[] cSlices = cS.toArray(new Slice[cS.size()]);

		int[] cCPUH = new int[cSlices.length];
		int[] cMemH = new int[cSlices.length];
		IntVar[] cHosters = new IntVar[cSlices.length];
		IntVar[] cEnds = new IntVar[cSlices.length];
		for (int i = 0; i < cSlices.length; i++) {
			Slice c = cSlices[i];
			cCPUH[i] = c.getCPUheight();
			cMemH[i] = c.getMemoryheight();
			cHosters[i] = c.hoster();
			cEnds[i] = c.end();
		}

		int[] dCPUH = new int[dSlices.length];
		int[] dMemH = new int[dSlices.length];
		IntVar[] dHosters = new IntVar[dSlices.length];
		IntVar[] dStart = new IntVar[dSlices.length];
		for (int i = 0; i < dSlices.length; i++) {
			Slice d = dSlices[i];
			dCPUH[i] = d.getCPUheight();
			dMemH[i] = d.getMemoryheight();
			dHosters[i] = d.hoster();
			dStart[i] = d.start();
		}

		int[] associations = new int[dHosters.length];
		for (int i = 0; i < associations.length; i++) {
			associations[i] = PlanMySlices.NO_ASSOCIATIONS; // No associations
			// task
		}
		for (int i = 0; i < linked.size(); i++) {
			int[] assoc = linked.get(i);
			associations[assoc[0]] = assoc[1];
		}
		int[] capaCPU = new int[rp.nodes().length];
		int[] capaMem = new int[rp.nodes().length];
		for (int idx = 0; idx < rp.nodes().length; idx++) {
			Node n = rp.nodes()[idx];
			capaMem[idx] = n.getMemoryCapacity();
			capaCPU[idx] = n.getCoreCapacity() * n.getNbOfCores();
		}
		logger.debug("SlicesPlanner branched");

		rp.post(new SlicesScheduler(rp.getEnvironment(), capaCPU, capaMem,
				cHosters, cCPUH, cMemH, cEnds, dHosters, dCPUH, dMemH, dStart,
				associations));
	}
}
