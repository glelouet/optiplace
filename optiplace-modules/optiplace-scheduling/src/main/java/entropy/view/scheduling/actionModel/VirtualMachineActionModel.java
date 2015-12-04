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

import org.chocosolver.solver.variables.IntVar;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * An abstract action to model an action focused on a virtual machine
 * 
 * @author Fabien Hermenier
 */
public abstract class VirtualMachineActionModel extends ActionModel {

	/**
	 * the boolean interval specifying whether or ot the vm is migrated from one
	 * host to another.
	 */
	protected IntVar moves;

	/** The virtual machine associated to the action. */
	private VirtualMachine vm;

	/**
	 * Make a new action on a specific virtual machine.
	 * 
	 * @param vm
	 *            the virtual machine
	 */
	public VirtualMachineActionModel(VirtualMachine vm) {
		this.vm = vm;
		moves = null;
	}

	/**
	 * Get the virtual machine associated to the action.
	 * 
	 * @return a virtual machine
	 */
	public VirtualMachine getVirtualMachine() {
		return vm;
	}

	/**
	 * @return the boolean var specifying whether or not the vm is migrated. A
	 *         vm is migrated when the hosters of the consuming and demanding
	 *         slices are different
	 */
	public IntVar moves() {
		return moves;
	}
}
