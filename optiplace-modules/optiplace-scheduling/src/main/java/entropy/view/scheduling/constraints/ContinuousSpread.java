/* Copyright (c) 2010 Ecole des Mines de Nantes. This file is part of Entropy.
 * Entropy is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Entropy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with Entropy. If not, see
 * <http://www.gnu.org/licenses/>. */

package entropy.view.scheduling.constraints;

import org.chocosolver.solver.constraints.reified.ReifiedFactory;
import org.chocosolver.solver.variables.IntVar;
import entropy.view.ha.constraints.LazySpread;
import entropy.view.ha.constraints.Spread;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.actionModel.VirtualMachineActionModel;
import entropy.view.scheduling.actionModel.slice.ConsumingSlice;
import entropy.view.scheduling.actionModel.slice.DemandingSlice;
import fr.emn.optiplace.configuration.ManagedElementSet;
import fr.emn.optiplace.configuration.SimpleManagedElementSet;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.solver.choco.Chocos;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * A continuous implementation of Spread with provide stronger guarantee than
 * {@link LazySpread}. All the virtual machines involved in the constraints will
 * never be hosted on the same node, even during the reconfiguration process.
 * For this purpose, migrations may be delayed to enforce the non-overlapping
 * between virtual machines.
 * <p/>
 * For a set of $n$ VMs, it uses $n*(n-1)$ implies constraints. For each couple
 * of VM (Vx,Vy), it enforces (C_x^h = D_y^h => C_x^ed <= D_y^st)
 * 
 * @author Fabien Hermenier
 */
public class ContinuousSpread extends Spread {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ContinuousSpread.class);

	protected SchedulingView view;
	/**
	 * Make a new constraint.
	 * 
	 * @param vms
	 *            the involved virtual machines
	 */
	public ContinuousSpread(SchedulingView view,
			ManagedElementSet<VirtualMachine> vms) {
		super(vms);
		this.view = view;
	}

	/**
	 * Make a new constraint.
	 * 
	 * @param vms
	 *            the involved virtual machines
	 */
	public ContinuousSpread(SchedulingView view, VirtualMachine... vms) {
		super(vms);
		this.view = view;
	}

	@Override
	public String toString() {
		return new StringBuilder("cSpread(").append(vms).append(")").toString();
	}

	/**
	 * Apply the constraint to the plan if the VM must be in the running state.
	 * The constraint is applied on all the future running virtual machines
	 * given at instantiation. Others are ignored.
	 * 
	 * @param core
	 *            the plan to customize. Must implement
	 *            {@link entropy.solver.choco.ChocoCustomRP}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void inject(ReconfigurationProblem core) {

		// Consider only the currently running and the future running VMs
		ManagedElementSet<VirtualMachine> runnings = new SimpleManagedElementSet<VirtualMachine>();
		for (VirtualMachine vm : getVMs()) {
			if (core.getFutureRunnings().contains(vm)
					|| core.getSourceConfiguration().isRunning(vm)) {
				runnings.add(vm);
			}
		}
		if (runnings.isEmpty()) {
			logger.debug(this + " is entailed. No VMs are running");
		} else {
			// The lazy spread implementation for the placement
			new LazySpread(runnings).inject(core);

			for (int i = 0; i < runnings.size(); i++) {
				VirtualMachine vmI = runnings.get(i);
				VirtualMachineActionModel aI = view.getAssociatedAction(vmI);
				for (int j = 0; j < i; j++) {
					VirtualMachine vmJ = runnings.get(j);
					VirtualMachineActionModel aJ = view
							.getAssociatedAction(vmJ);
					DemandingSlice d = aI.getDemandingSlice();
					ConsumingSlice c = aJ.getConsumingSlice();
					if (d != null && c != null) {
						// No need to place the constraints if the slices do not
						// have a chance to overlap
						if (!(c.hoster().isInstantiated() && !d.hoster()
								.canBeInstantiatedTo(c.hoster().getVal()))
								&& !(d.hoster().isInstantiated() && !c.hoster()
										.canBeInstantiatedTo(
												d.hoster().getVal()))) {
							IntVar eq = core.createBooleanVar("eq");
							core.post(ReifiedFactory.builder(eq,
									core.eq(d.hoster(), c.hoster()), core));
							Chocos.postImplies(core, eq,
									core.leq(c.end(), d.start()));
						}
					}
					// The inverse relation
					d = aJ.getDemandingSlice();
					c = aI.getConsumingSlice();

					if (d != null && c != null) {
						// No need to place the constraints if the slices do not
						// have a chance to overlap
						if (!(c.hoster().isInstantiated() && !d.hoster()
								.canBeInstantiatedTo(c.hoster().getVal()))
								&& !(d.hoster().isInstantiated() && !c.hoster()
										.canBeInstantiatedTo(
												d.hoster().getVal()))) {
							IntVar eq = core.createBooleanVar("eq");
							core.post(ReifiedFactory.builder(eq,
									core.eq(d.hoster(), c.hoster()), core));
							Chocos.postImplies(core, eq,
									core.leq(c.end(), d.start()));
						}
					}
				}
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ContinuousSpread that = (ContinuousSpread) o;
		return vms.equals(that.vms);
	}

	@Override
	public int hashCode() {
		return vms.hashCode() + "cSpread".hashCode() * 31;
	}

}
