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
import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.action.Action;
import entropy.view.scheduling.action.Migration;
import entropy.view.scheduling.actionModel.slice.ConsumingSlice;
import entropy.view.scheduling.actionModel.slice.DemandingSlice;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.core.choco.reified.FastIFFEq;
import fr.emn.optiplace.core.choco.reified.FastImpliesEq;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * Model a action that may potentially migrate a VM. The action is modeled with
 * one consuming slice and one demanding slice. If the demanding slice is hosted
 * on a different node than the consuming slice it will result in a migration.
 * In this case, the action starts at the beginning of the demanding slice and
 * ends at the end of the consuming slice. Otherwise, the VM will stay on the
 * node.
 * 
 * @author Fabien Hermenier
 */
public class MigratableActionModel extends VirtualMachineActionModel {

	/** The global cost of the action. */
	private IntVar cost;

	/**
	 * Make a new action.
	 * 
	 * @param model
	 *            the model
	 * @param vm
	 *            the virtual machine to make moveable
	 * @param d
	 *            the duration of the migration if it is performed
	 * @param moveable
	 *            {@code true} to indicates the VM can be migrated
	 */
	@SuppressWarnings("unchecked")
	public MigratableActionModel(SchedulingView view, VirtualMachine vm, int d,
			boolean moveable) {
		super(vm);
		ReconfigurationProblem model = view.getProblem();
		moves = model.createBoundIntVar("0", 0, 0);
		assert d >= 0 : "The cost of migration for " + vm + " equals 0 !";

		if (moveable) {
			cost = model.createBoundIntVar("k(migrate("
					+ getVirtualMachine().getName() + "))", 0,
					SchedulingView.MAX_TIME);
			duration = model.createEnumIntVar("d(migrate("
					+ getVirtualMachine().getName() + "))", new int[]{0, d});
			cSlice = new ConsumingSlice(view, "migS(" + vm.getName() + ")",
					model.getSourceConfiguration().getLocation(vm),
					vm.getCPUConsumption(), vm.getMemoryConsumption());
			dSlice = new DemandingSlice(view, vm.getName() + ".h",
					vm.getCPUDemand(), vm.getMemoryDemand());

			moves = model.createBooleanVar("mv("
					+ getVirtualMachine().getName() + ")");
			view.post(ReifiedFactory.builder(moves,
					model.neq(cSlice.hoster(), dSlice.hoster()), model));

			IntVar stay = model.boolenize(
					model.sum(model.boolenize(moves, null),
							model.createIntegerConstant(null, -1)), "");

			view.post(new TimesXYZ(moves, cSlice.end(), cost));

			view.post(new FastIFFEq(stay, duration, 0));

			if (dSlice.getCPUheight() <= cSlice.getCPUheight()) {
				view.post(new FastImpliesEq(stay, cSlice.duration(), 0));
			} else if (dSlice.getCPUheight() > cSlice.getCPUheight()) {
				view.post(new FastImpliesEq(stay, dSlice.duration(), 0));
			}
			view.post(model.eq(dSlice.end(),
					model.plus(dSlice.start(), dSlice.duration())));
			view.post(model.eq(cSlice.end(),
					model.plus(cSlice.start(), cSlice.duration())));
			view.post(model.leq(duration, cSlice.duration()));
			view.post(model.leq(duration, dSlice.duration()));
			view.post(model.eq(end(), model.plus(start(), duration)));
		} else {
			boolean neadIncrease = vm.getCPUConsumption() <= vm.getCPUDemand();
			cost = model.createIntegerConstant("c(migrate("
					+ getVirtualMachine().getName() + "))", 0);
			if (neadIncrease) {
				cSlice = new ConsumingSlice("", model.createIntegerConstant(
						"",
						model.node(model.getSourceConfiguration().getLocation(
								vm))), model.createTaskVar("", view.getStart(),
						view.getEnd(), view.getEnd()), vm.getCPUConsumption(),
						vm.getMemoryConsumption());

				dSlice = new DemandingSlice("migD(" + vm.getName() + ")",
						model.createIntegerConstant("", model.node(model
								.getSourceConfiguration().getLocation(vm))),
						model.createTaskVar("", view.getEnd(), view.getEnd(),
								model.createIntegerConstant("", 0)),
						vm.getCPUDemand(), vm.getMemoryDemand()

				);
			} else {
				cSlice = new ConsumingSlice("", model.createIntegerConstant(
						"",
						model.node(model.getSourceConfiguration().getLocation(
								vm))), model.createTaskVar("", view.getStart(),
						view.getStart(), view.getStart()),
						vm.getCPUConsumption(), vm.getMemoryConsumption());

				dSlice = new DemandingSlice("", model.createIntegerConstant(
						"",
						model.node(model.getSourceConfiguration().getLocation(
								vm))), model.createTaskVar("", view.getStart(),
						view.getEnd(), view.getEnd()), vm.getCPUDemand(),
						vm.getMemoryDemand()

				);
			}
			view.post(model.eq(end(), start()));
		}

		view.post(model.leq(cSlice.duration(), view.getEnd()));
		view.post(model.leq(dSlice.duration(), view.getEnd()));
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
		if (getConsumingSlice().hoster().getVal() != getDemandingSlice()
				.hoster().getVal()) {
			l.add(new Migration(getVirtualMachine(), solver
					.getNode(getConsumingSlice().hoster().getVal()), solver
					.getNode(getDemandingSlice().hoster().getVal()), start()
					.getVal(), end().getVal()));
		}
		return l;
	}

	@Override
	public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
		cfg.addOnline(solver.getNode(getDemandingSlice().hoster().getVal()));
		return cfg.setRunOn(getVirtualMachine(),
				solver.getNode(getDemandingSlice().hoster().getVal()));

	}

	@Override
	public String toString() {
		return "migration(" + getVirtualMachine().getName() + ")";
	}

	@Override
	public IntVar getGlobalCost() {
		return cost;
	}
}
