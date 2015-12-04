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
import java.util.Collections;
import java.util.List;

import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.actionModel.slice.DemandingSlice;
import entropy.view.scheduling.actionModel.slice.SliceComparator;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.core.packers.CustomPack;
import fr.emn.optiplace.core.packers.FastBinPacking;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;

/**
 * A constraint to assign a host with a sufficient amount of resources to
 * satisfy all the heights of the demanding slices. The constraint is based on
 * two dynamic bin packing constraints.
 * 
 * @author Fabien Hermenier
 */
public class SatisfyDemandingSlicesHeightsFastBP extends EmptyView
		implements
			SatisfyDemandingSliceHeights {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SatisfyDemandingSlicesHeightsFastBP.class);

	private FastBinPacking cPack;

	private FastBinPacking mPack;

	private final SchedulingView plan;

	public SatisfyDemandingSlicesHeightsFastBP(SchedulingView plan) {
		this.plan = plan;
	}

	@Override
	public void associate(ReconfigurationProblem rp) {
		// SetVar []bins = new SetVar[rp.getNodes().length];
		List<DemandingSlice> demandingCPU = new ArrayList<DemandingSlice>();// rp.getDemandingSlices());
		List<DemandingSlice> demandingMem = new ArrayList<DemandingSlice>();// rp.getDemandingSlices());

		// Remove slices with an height = 0
		for (DemandingSlice d : plan.getDemandingSlices()) {
			if (d.getCPUheight() != 0) {
				demandingCPU.add(d);
			}
			if (d.getMemoryheight() != 0) {
				demandingMem.add(d);
			}
		}

		Node[] ns = rp.getFutureOnlines().toArray(new Node[]{});
		if (!demandingCPU.isEmpty()) {
			IntVar[] demandCPU = new IntVar[demandingCPU.size()];
			IntVar[] assignsCPU = new IntVar[demandingCPU.size()];

			IntVar[] usedCPU = new IntVar[ns.length];
			for (int i = 0; i < ns.length; i++) {
				usedCPU[i] = rp.getUsedCPU(ns[i]);
				assert usedCPU[i] != null;
			}

			// Sort in descending order
			Collections.sort(demandingCPU, new SliceComparator(false,
					SliceComparator.ResourceType.cpuConsumption));
			for (int i = 0; i < demandingCPU.size(); i++) {
				DemandingSlice s = demandingCPU.get(i);
				demandCPU[i] = rp.createIntegerConstant(""/* i + " #dCPU" */,
						s.getCPUheight());
				assignsCPU[i] = s.hoster();
			}

			cPack = new FastBinPacking(rp.getEnvironment(), usedCPU, demandCPU,
					assignsCPU);
			rp.post(cPack);
		}

		if (!demandingMem.isEmpty()) {
			IntVar[] demandMem = new IntVar[demandingMem.size()];
			IntVar[] assignsMem = new IntVar[demandingMem.size()];
			IntVar[] capaMem = new IntVar[ns.length];
			for (int i = 0; i < ns.length; i++) {
				capaMem[i] = rp.getUsedMem(ns[i]);
			}

			Collections.sort(demandingMem, new SliceComparator(false,
					SliceComparator.ResourceType.memoryConsumption));
			for (int i = 0; i < demandingMem.size(); i++) {
				DemandingSlice task = demandingMem.get(i);
				demandMem[i] = rp.createIntegerConstant(""/*
														 * task.getName() +
														 * "#dMem"
														 */,
						task.getMemoryheight());
				assignsMem[i] = task.hoster();
			}

			mPack = new FastBinPacking(rp.getEnvironment(), capaMem, demandMem,
					assignsMem);
			rp.post(mPack);
		}
		logger.debug("SatisfyDemandingSlicesHeightsFastBP branched");
	}

	@Override
	public CustomPack getCoreCPUPacking() {
		return cPack;
	}

	@Override
	public CustomPack getCoreMemPacking() {
		return mPack;
	}

	@Override
	public int getRemainingCPU(int bin) {
		return cPack.getRemainingSpace(bin);
	}

	@Override
	public int getRemainingMemory(int bin) {
		return mPack.getRemainingSpace(bin);
	}
}
