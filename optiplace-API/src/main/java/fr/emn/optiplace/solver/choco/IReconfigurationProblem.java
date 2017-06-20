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

package fr.emn.optiplace.solver.choco;

import java.util.Arrays;
import java.util.stream.Stream;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.ProblemStatistics;
import fr.emn.optiplace.solver.SolvingStatistics;
import fr.emn.optiplace.view.access.CoreView;


/**
 * Specification of a reconfiguration problem. A bridge between the VMs, the
 * Computers, and a Choco problem
 *
 * @author Fabien Hermenier
 * @author Guillaume Le Louët
 */
public interface IReconfigurationProblem extends CoreView {

	public static final Logger logger = LoggerFactory.getLogger(IReconfigurationProblem.class);

	public Model getModel();

	public default Solver getSolver() {
		return getModel().getSolver();
	}

	/** shortcut for getSolver().post(c) */
	default void post(Constraint c) {
		getModel().post(c);
	}

	/**
	 * Get statistics about the solving process
	 *
	 * @return some statistics
	 */
	SolvingStatistics getSolvingStatistics();

	/***** Variables linked to Computers and VMs *************************/

	/**
	 * Get the source configuration, that is, the original configuration to
	 * optimize.
	 *
	 * @return a configuration
	 */
	IConfiguration getSourceConfiguration();

	/******************** variables linked to resources ***********************/

	default IntVar getComputerUse(String resource, Computer n) {
		return getUse(resource).getComputersLoad()[b().location(n)];
	}

	default int getComputerCap(String resource, Computer n) {
		return getResourceSpecification(resource).getCapacities()[b().location(n)];
	}

	default IntVar getUsedCPU(Computer n) {
		return getComputerUse("CPU", n);
	}

	default IntVar getUsedMem(Computer n) {
		return getComputerUse("MEM", n);
	}

	public IntVar getHostUse(String resource, int vmIndex);

	public IntVar getHostCapa(String resource, int vmIndex);

	/**
	 * add a new resource specification
	 *
	 * @param rs
	 *          the resource specification.
	 */
	void addResource(ResourceSpecification rs);

	/********************* Operations on variables *****************/



	/**
	 * affect a variable according to the state of a VM.
	 * <p>
	 * if one of the potential IntVar (onX) is null, then the corresponding state
	 * is removed from the IntVar state of the vm.This reduces the need to check
	 * for VM state or removing the values manually
	 * </p>
	 *
	 * @param v
	 *          the VM to follow the state
	 * @param var
	 *          the variable to assign
	 * @param onRunning
	 *          the value of var if VM is running
	 * @param onExtern
	 *          the value of var if VM is externed
	 * @param onWaiting
	 *          the value of var ir VM is waiting
	 */
	default void switchState(VM v, IntVar var, IntVar onRunning, IntVar onExtern, IntVar onWaiting) {
		IntVar[] vars = { onRunning, onExtern, onWaiting };
		IntVar state = getState(v);
		int nbNonNull = (int) Stream.of(vars).filter(a -> a != null).count();
		switch (nbNonNull) {
		case 0:
			throw new UnsupportedOperationException("can't assign an IntVar( " + var + ")to null value");
		case 1:
			post(getModel().arithm(var, "=", onRunning != null ? onRunning : onExtern != null ? onExtern : onWaiting));
			try {
				state.instantiateTo(onRunning != null ? VM_RUNNODE : onExtern != null ? VM_RUNEXT : VM_WAITING, Cause.Null);
			} catch (ContradictionException e) {
				throw new UnsupportedOperationException(e);
			}
			break;
		case 2:
			try {
				if (onRunning == null) {
					state.removeValue(VM_RUNNODE, Cause.Null);
				}
				if (onExtern == null) {
					state.removeValue(VM_RUNEXT, Cause.Null);
				}
				if (onWaiting == null) {
					state.removeValue(VM_WAITING, Cause.Null);
				}
			} catch (ContradictionException e) {
				throw new UnsupportedOperationException(e);
			}
			break;
		case 3:
			post(getModel().arithm(var, "=", v().nth(getState(v), vars)));
			break;
		default:
			throw new UnsupportedOperationException(
					"unsupported nuber of non null variables " + nbNonNull + " in " + Arrays.asList(vars));
		}
	}

	public ProblemStatistics getStatistics();
}
