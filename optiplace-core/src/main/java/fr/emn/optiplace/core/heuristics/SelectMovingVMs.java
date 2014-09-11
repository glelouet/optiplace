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

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * A Var selector that focuses on the currently running or sleeping VMs that
 * will be running and move because their current location is no more possible
 * (node has been ban or the VM is fenced). Non-running VMs are ignored.
 *
 * @author Fabien Hermenier
 * @author Guillaume Le Louët[guillaume.lelouet@gmail.com]2013
 */
public class SelectMovingVMs extends AbstractIntVarSelector {

	/** The demanding slices to consider. */
	private final LinkedHashMap<VirtualMachine, IntDomainVar> actions;

	private final Configuration cfg;

	private final ReconfigurationProblem rp;

	/**
	 * Make a new heuristic. By default, the heuristic doesn't touch the
	 * scheduling constraints.
	 *
	 * @param s
	 * the solver to use to extract the assignment variables
	 */
	public SelectMovingVMs(ReconfigurationProblem s, Set<VirtualMachine> vms) {
		super(s);
		cfg = s.getSourceConfiguration();
		rp = s;
		actions = new LinkedHashMap<VirtualMachine, IntDomainVar>();
		for (VirtualMachine vm : vms) {
			if (rp.getSourceConfiguration().hasVM(vm)) {
				actions.put(vm, rp.host(vm));
			}

		}
	}

	@Override
	public IntDomainVar selectVar() {
		for (Entry<VirtualMachine, IntDomainVar> a : actions.entrySet()) {
			IntDomainVar hoster = a.getValue();
			if (!hoster.isInstantiated()) {
				VirtualMachine vm = a.getKey();
				Node n = cfg.getLocation(vm);
				if (n != null && !hoster.canBeInstantiatedTo(rp.node(n))) {
					return hoster;
				}
			}
		}
		return null;
	}
}
