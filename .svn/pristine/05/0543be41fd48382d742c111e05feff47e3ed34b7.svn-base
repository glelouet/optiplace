/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.core.heuristics;

import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * A {@link VarSelector} that focuses on the assignment of the vms' hosters.<br />
 * a rewriting of {@link StaticVarOrder} allowing to get the last index.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class HosterVarSelector extends AbstractIntVarSelector {

	private final IStateInt last;

	/**
	 * Make a new heuristic.
	 * 
	 * @param solver
	 *            the solver to use to extract the assignment variables
	 * @param vms
	 *            the VMs to considers the hoster's
	 */
	public HosterVarSelector(ReconfigurationProblem solver, VirtualMachine[] vms) {
		super(solver, solver.getHosters(vms));
		last = solver.getEnvironment().makeInt(-1);
	}

	/**
	 * Select what seems to be most interesting variable, considering the
	 * current state of the variables
	 * 
	 * @return
	 */
	@Override
	public IntDomainVar selectVar() {
		// <hca> it starts at last.get() and not last.get() +1 to be
		// robust to restart search loop
		for (int i = last.get() + 1; i < vars.length; i++) {
			if (!vars[i].isInstantiated()) {
				last.set(i);
				return vars[i];
			}
		}
		return null;
	}

	/** get the index of the last variable selected */
	public int getLastIndex() {
		return last.get();
	}

}
