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
import fr.emn.optiplace.core.packers.FastMultiBinPacking;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;

/**
 * A constraint to assign a host with a sufficient amount of resources to
 * satisfy all the heights of the demanding slices. The constraint is based on
 * two dynamic bin packing constraints.
 * 
 * @author Fabien Hermenier
 */
public class SatisfyDemandingSlicesHeightsFastMultiDimensionBP
		extends
			EmptyView implements SatisfyDemandingSliceHeights {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SatisfyDemandingSlicesHeightsFastMultiDimensionBP.class);

	private FastMultiBinPacking pack;

	private final SchedulingView plan;

	public SatisfyDemandingSlicesHeightsFastMultiDimensionBP(SchedulingView plan) {
		this.plan = plan;
	}

	@Override
	public void associate(ReconfigurationProblem rp) {
		List<DemandingSlice> dSlices = new ArrayList<DemandingSlice>(
				plan.getDemandingSlices());

		Collections.sort(dSlices, new SliceComparator(false,
				SliceComparator.ResourceType.cpuConsumption));

		int[][] sizes = new int[2][];
		sizes[0] = new int[dSlices.size()];
		sizes[1] = new int[dSlices.size()];

		IntVar[] assigns = new IntVar[dSlices.size()];
		for (int i = 0; i < dSlices.size(); i++) {
			sizes[0][i] = dSlices.get(i).getCPUheight();
			sizes[1][i] = dSlices.get(i).getMemoryheight();
			assigns[i] = dSlices.get(i).hoster();
		}

		Node[] ns = rp.nodes();
		IntVar[][] capas = new IntVar[2][];
		capas[0] = new IntVar[ns.length];
		capas[1] = new IntVar[ns.length];

		for (int i = 0; i < ns.length; i++) {
			capas[0][i] = rp.getUsedCPU(ns[i]);
			capas[1][i] = rp.getUsedMem(ns[i]);
		}

		pack = new FastMultiBinPacking(rp.getEnvironment(), capas, sizes,
				assigns);
		rp.post(pack);

		logger.debug("SatisfyDemandingSlicesHeightsCustomBP branched");
	}

	@Override
	public CustomPack getCoreCPUPacking() {
		return null;
	}

	@Override
	public CustomPack getCoreMemPacking() {
		return null;
	}

	@Override
	public int getRemainingCPU(int bin) {
		return pack.getRemainingSpace(0, bin);
	}

	@Override
	public int getRemainingMemory(int bin) {
		return pack.getRemainingSpace(1, bin);
	}
}
