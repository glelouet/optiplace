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

import java.util.*;

import model.variables.integer.IntegerVariable;
import model.variables.set.SetVariable;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.set.SetVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.actionModel.slice.DemandingSlice;
import entropy.view.scheduling.actionModel.slice.Slice;
import entropy.view.scheduling.actionModel.slice.SliceComparator;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.core.packers.CustomPack;
import fr.emn.optiplace.core.packers.SimpleBinPacking;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;

/**
 * A constraint to assign a host with a sufficient amount of resources to
 * satisfy all the heights of the demanding slices. The constraint is based on
 * two dynamic bin packing constraints.
 * 
 * @author Fabien Hermenier
 */
public class SatisfyDemandingSlicesHeightsSimpleBP extends EmptyView
		implements
			SatisfyDemandingSliceHeights {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SatisfyDemandingSlicesHeightsSimpleBP.class);

	private SetVariable[] bins;

	private final Map<IntegerVariable, Integer> idxVMHoster = new HashMap<IntegerVariable, Integer>();

	// private Constraint cpuPack;

	// private Constraint memPack;

	private SimpleBinPacking cPack;

	private SimpleBinPacking mPack;

	private final SchedulingView plan;

	public SatisfyDemandingSlicesHeightsSimpleBP(SchedulingView plan) {
		this.plan = plan;
	}

	public SetVariable getBin(int idx) {
		return bins[idx];
	}

	public int getHosterBinIndex(IntegerVariable v) {
		return idxVMHoster.get(v);
	}

	@Override
	public void associate(ReconfigurationProblem rp) {
		// SetVar []bins = new SetVar[rp.getNodes().length];
		List<DemandingSlice> demandingCPU = new ArrayList<DemandingSlice>(
				plan.getDemandingSlices());
		List<DemandingSlice> demandingMem = new ArrayList<DemandingSlice>(
				plan.getDemandingSlices());

		// Remove slices with an height = 0
		for (ListIterator<DemandingSlice> ite = demandingCPU.listIterator(); ite
				.hasNext();) {
			DemandingSlice d = ite.next();
			if (d.getCPUheight() == 0) {
				ite.remove();
			}
		}

		for (ListIterator<DemandingSlice> ite = demandingMem.listIterator(); ite
				.hasNext();) {
			DemandingSlice d = ite.next();
			if (d.getMemoryheight() == 0) {
				ite.remove();
			}
		}

		// ManagedElementSet<Node> ns = cfg.getAllNodes();
		// Node[] ns = model.getNodes();
		Node[] ns = rp.nodes();
		if (!demandingCPU.isEmpty()) {
			List<IntVar> demandCPU = new ArrayList<IntVar>();
			List<IntVar> assignsCPU = new ArrayList<IntVar>();

			SetVar[] sets = new SetVar[ns.length];
			IntVar[] capaCPU = new IntVar[sets.length];
			for (int i = 0; i < ns.length; i++) {
				// if (model.getSourceConfiguration().isOnline(ns[i])) {
				capaCPU[i] = rp.getUsedCPU(ns[i]);
				sets[i] = rp.createEnumSetVar("slicesCPU(" + ns[i].getName()
						+ ")", 0, demandingCPU.size() - 1);
				/*
				 * } else { capaCPU[i] = constant(0); sets[i] =
				 * emptySet();//makeSetVar("slicesCPU(" + n.getName() + ")", new
				 * int[]{}); }
				 */
				// bins = sets;
			}

			// Sort in descending order
			Collections.sort(demandingCPU, new SliceComparator(false,
					SliceComparator.ResourceType.cpuConsumption));
			for (int i = 0; i < demandingCPU.size(); i++) {
				demandCPU.add(rp.createIntegerConstant(i + " #dCPU",
						demandingCPU.get(i).getCPUheight()));
				assignsCPU.add(demandingCPU.get(i).hoster());
				// this.idxVMHoster.put(assignsCPU.get(i), i);
			}
			IntVar[] demands = demandCPU
					.toArray(new IntVar[demandCPU.size()]);
			// Plan.logger.debug("Pack \n\tcapa:" + Arrays.toString(capaCPU) +
			// "\n\tdemand: " + demandCPU + "\n\tassigns" + assignsCPU);

			IntVar nbNonEmpty = rp.createBoundIntVar("non-empty", 0,
					sets.length);
			cPack = new SimpleBinPacking(rp.getEnvironment(), sets, capaCPU,
					demands, assignsCPU.toArray(new IntVar[assignsCPU
							.size()]),

					nbNonEmpty);
			// cPack.readOptions(opts);
			// TODO: options
			rp.post(cPack);
		}

		// opts.add(SimpleBinPacking.ADDITIONAL_RULES.getOption());
		if (!demandingMem.isEmpty()) {
			List<IntVar> demandMem = new ArrayList<IntVar>();
			List<IntVar> assignsMem = new ArrayList<IntVar>();
			SetVar[] sets = new SetVar[ns.length];
			IntVar[] capaMem = new IntVar[sets.length];
			for (int i = 0; i < ns.length; i++) {
				capaMem[i] = rp.getUsedMem(ns[i]);
				sets[i] = rp.createEnumSetVar("slicesMem(" + ns[i].getName()
						+ ")", 0, demandingMem.size() - 1);
			}

			Collections.sort(demandingMem, new SliceComparator(false,
					SliceComparator.ResourceType.memoryConsumption));
			for (Slice task : demandingMem) {
				demandMem.add(rp.createIntegerConstant(
						task.getName() + "#dMem", task.getMemoryheight()));
				assignsMem.add(task.hoster());
			}
			IntVar[] demands = demandMem
					.toArray(new IntVar[demandMem.size()]);
			// Plan.logger.debug("Pack \n\tcapa:" + Arrays.toString(capaMem) +
			// "\n\tdemand: " + demandMem + "\n\tassigns" + assignsMem);
			IntVar nbNonEmpty = rp.createBoundIntVar("non-empty", 0,
					sets.length);

			mPack = new SimpleBinPacking(rp.getEnvironment(), sets, capaMem,
					demands, assignsMem.toArray(new IntVar[assignsMem
							.size()]), nbNonEmpty);

			// mPack.readOptions(opts);
			rp.post(mPack);
		}
		logger.debug("SatisfyDemandingSlicesHeightsSimpleBP branched");
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
		return cPack.getRemainingSpace(bin);
	}
}
