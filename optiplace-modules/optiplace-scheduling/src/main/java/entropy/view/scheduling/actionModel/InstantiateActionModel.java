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

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.action.Action;
import entropy.view.scheduling.action.Instantiate;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * Model an instantiation action. No consuming nor demanding slice. Instead, it
 * restricts the variable that indicates the moment the VM is ready.
 * 
 * @author Fabien Hermenier
 */
public class InstantiateActionModel extends VirtualMachineActionModel {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(InstantiateActionModel.class);

	private final IntVar start;

	@SuppressWarnings("unchecked")
	public InstantiateActionModel(entropy.view.scheduling.SchedulingView rp,
			VirtualMachine vm, int d) {
		super(vm);
		start = rp.getStart();
		duration = rp.getTimeVMReady(vm);
		assert d > 0 : "forge duration equals to 0";
		rp.post(rp.getProblem().geq(rp.getEnd(), d));
		try {
			duration.setVal(d);
		} catch (ContradictionException e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public IntVar start() {
		return start;
	}

	@Override
	public IntVar end() {
		return duration;
	}

	@Override
	public List<Action> getDefinedAction(ReconfigurationProblem solver) {
		List<Action> l = new ArrayList<Action>();
		l.add(new Instantiate(getVirtualMachine(), start.getVal(), duration
				.getVal()));
		return l;
	}

	@Override
	public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
		if (!cfg.contains(getVirtualMachine())) {
			cfg.addWaiting(getVirtualMachine());
			return true;
		}
		return false;
	}

	@Override
	public IntVar getDuration() {
		return duration;
	}

	@Override
	public IntVar getGlobalCost() {
		return duration;
	}
}
