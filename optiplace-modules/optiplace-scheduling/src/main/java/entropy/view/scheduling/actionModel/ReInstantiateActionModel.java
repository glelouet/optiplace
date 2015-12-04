/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */
package entropy.view.scheduling.actionModel;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.constraints.integer.TimesXYZ;
import org.chocosolver.solver.constraints.reified.ReifiedFactory;
import org.chocosolver.solver.variables.IntVarAddCste;
import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.action.*;
import entropy.view.scheduling.actionModel.slice.ConsumingSlice;
import entropy.view.scheduling.actionModel.slice.DemandingSlice;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.core.choco.reified.FastIFFEq;
import fr.emn.optiplace.core.choco.reified.FastImpliesEq;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * Model a action that may potentially move a VM using a reinstantiation. In
 * practice, a new VM, supposed identical is booted on the destination node.
 * Once booted, the old instance is detroyed
 * <p/>
 * The action is modeled with one consuming slice and one demanding slice. If
 * the demanding slice is hosted on a different node than the consuming slice it
 * will result in a reinstantiation. The demanding slice denotes the action of
 * starting the new VM while the consuming slice denotes the halting process on
 * the old VM.
 * 
 * @author Fabien Hermenier
 */
public class ReInstantiateActionModel extends VirtualMachineActionModel {

	/** The global cost of the action. */
	private IntVar cost;

	private final int forgeDuration;

	private final int startDuration;

	private final int stopDuration;

	public final static int RENAME_DURATION = 1;

	@SuppressWarnings("unchecked")
	public ReInstantiateActionModel(
			entropy.view.scheduling.SchedulingView model, VirtualMachine vm,
			int forgeD, int startD, int stopD, boolean moveable) {
		super(vm);
		ReconfigurationProblem problem = model.getProblem();
		forgeDuration = forgeD;
		startDuration = startD;
		stopDuration = stopD;
		assert forgeDuration >= 0 && startDuration >= 0 && stopDuration >= 0 : "The cost of reinstantiation for "
				+ vm + " equals 0 !";

		//
		// cSlice: default: 0 + var(duration) = var(end)
		// dSlice: default: var(start) + var(duration) = var(end)
		//
		// !moveable:
		// if cpu increase: dSlice: var(start) + 0 = var(end) -> Slice(end, 0,
		// end) -> pas de contraintes
		// else cSlice: 0 + 0 = var(end) -> Slice(0,0,0), pas de contraintes
		//
		// moveable: cSlice -> Slice(0, var(end), var(end)), pas de plus
		//
		//
		if (moveable) {

			duration = problem.createEnumIntVar("overlap(reinst("
					+ getVirtualMachine().getName() + "))", new int[]{0,
					stopD + startD + RENAME_DURATION});
			cSlice = new ConsumingSlice(model, "migS(" + vm.getName() + ")",
					problem.getSourceConfiguration().getLocation(vm),
					vm.getCPUConsumption(), vm.getMemoryConsumption());
			dSlice = new DemandingSlice(model, "migD(" + vm.getName() + ")",
					vm.getCPUDemand(), vm.getMemoryDemand());

			IntVar forgeCost = problem
					.createEnumIntVar("forge(" + getVirtualMachine().getName()
							+ ")", new int[]{0, forgeD});

			IntVar move = problem.createBooleanVar("mv("
					+ getVirtualMachine().getName() + ")");
			model.post(ReifiedFactory.builder(move,
					problem.neq(cSlice.hoster(), dSlice.hoster()), problem));

			IntVar stay = model.getProblem().sum(
					model.getProblem().boolenize(move, null),
					model.getProblem().createIntegerConstant(null, -1));

			// model.eq()
			cost = problem.createBoundIntVar("k(reinst("
					+ getVirtualMachine().getName() + "))", 0,
					SchedulingView.MAX_TIME);
			model.post(new TimesXYZ(move, new IntVarAddCste(problem, "",
					cSlice.end(), -stopD), cost));

			model.post(new FastIFFEq(stay, duration, 0));

			if (dSlice.getCPUheight() <= cSlice.getCPUheight()) {
				model.post(new FastImpliesEq(stay, cSlice.duration(), 0));
			} else if (dSlice.getCPUheight() > cSlice.getCPUheight()) {
				model.post(new FastImpliesEq(stay, dSlice.duration(), 0));
			}
			model.post(problem.eq(dSlice.end(),
					problem.plus(dSlice.start(), dSlice.duration())));
			model.post(problem.eq(cSlice.end(),
					problem.plus(cSlice.start(), cSlice.duration())));
			model.post(problem.leq(duration, cSlice.duration()));
			model.post(problem.leq(duration, dSlice.duration()));
			model.post(problem.eq(end(), problem.plus(start(), duration)));

			model.post(new FastIFFEq(stay, forgeCost, 0));
			model.post(problem.geq(dSlice.start(), forgeCost));
		} else {
			boolean neadIncrease = vm.getCPUConsumption() <= vm.getCPUDemand();
			cost = problem.createIntegerConstant("c(migrate("
					+ getVirtualMachine().getName() + "))", 0);
			if (neadIncrease) {
				cSlice = new ConsumingSlice("", problem.createIntegerConstant(
						"", problem.node(problem.getSourceConfiguration()
								.getLocation(vm))), problem.createTaskVar("",
						model.getStart(), model.getEnd(), model.getEnd()),
						vm.getCPUConsumption(), vm.getMemoryConsumption());

				dSlice = new DemandingSlice("migD(" + vm.getName() + ")",
						problem.createIntegerConstant("", problem.node(problem
								.getSourceConfiguration().getLocation(vm))),
						problem.createTaskVar("", model.getEnd(),
								model.getEnd(),
								problem.createIntegerConstant("", 0)),
						vm.getCPUDemand(), vm.getMemoryDemand()

				);
			} else {
				cSlice = new ConsumingSlice("", problem.createIntegerConstant(
						"", problem.node(problem.getSourceConfiguration()
								.getLocation(vm))), problem.createTaskVar("",
						model.getStart(), model.getStart(), model.getStart()),
						vm.getCPUConsumption(), vm.getMemoryConsumption());

				dSlice = new DemandingSlice("", problem.createIntegerConstant(
						"", problem.node(problem.getSourceConfiguration()
								.getLocation(vm))), problem.createTaskVar("",
						model.getStart(), model.getEnd(), model.getEnd()),
						vm.getCPUDemand(), vm.getMemoryDemand()

				);
			}
			model.post(problem.eq(end(), start()));
		}

		model.post(problem.leq(cSlice.duration(), model.getEnd()));
		model.post(problem.leq(dSlice.duration(), model.getEnd()));
	}

	/**
	 * Get the moment the action ends. The action ends at the moment the slice
	 * on the source node ends.
	 * 
	 * @return <code>getConsumingSlice().end()</code>
	 */
	@Override
	public final IntVar end() {
		return getConsumingSlice().end();
	}

	/**
	 * Get the moment the action starts. The action starts at the moment the
	 * slice on the source node starts.
	 * 
	 * @return <code>getDemandingSlice().start()</code>
	 */
	@Override
	public final IntVar start() {
		return getDemandingSlice().start();
	}

	/**
	 * Return the migration action if the VM have to move.
	 * 
	 * @return a Migration if the source node and the destination node are
	 *         different. null otherwise
	 */
	@Override
	public List<Action> getDefinedAction(ReconfigurationProblem solver) {
		ArrayList<Action> l = new ArrayList<Action>();
		int cIdx = getConsumingSlice().hoster().getVal();
		int dIdx = getDemandingSlice().hoster().getVal();
		if (cIdx != dIdx) {
			int dStart = dSlice.start().getVal();
			int cEnd = cSlice.end().getVal();
			Node srcN = solver.getNode(cIdx);
			Node dstN = solver.getNode(dIdx);
			VirtualMachine cpy = getVirtualMachine().clone();
			cpy.rename(cpy.getName() + "-tmpClone");
			l.add(new Instantiate(cpy, 0, forgeDuration));
			l.add(new Run(cpy, dstN, dStart, dStart + startDuration));
			l.add(new Stop(getVirtualMachine(), srcN, cEnd - stopDuration
					- RENAME_DURATION, cEnd - RENAME_DURATION));
			l.add(new VirtualMachineRename(cpy, srcN, getVirtualMachine()
					.getName(), cEnd - RENAME_DURATION, cEnd));
		}
		return l;
	}

	@Override
	public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
		Node n = solver.getNode(getDemandingSlice().hoster().getVal());
		cfg.addOnline(n);
		return cfg.setRunOn(getVirtualMachine(), n);

	}

	@Override
	public String toString() {
		return "reinstantiate(" + getVirtualMachine().getName() + ")";
	}

	@Override
	public IntVar getGlobalCost() {
		return cost;
	}
}
