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
package entropy.view;

import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.solver.ReconfigurationResult;
import entropy.solver.choco.ReconfigurationProblem;

/**
 * An interface to specify some constraints related to the final state of the
 * vms and pms.
 * 
 * @author Fabien Hermenier
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public interface Rule {

	/**
	 * Textual representation of the constraint.
	 * 
	 * @return a String
	 */
	@Override
	String toString();

	/**
	 * Check that the constraint is satified in a configuration.
	 * 
	 * @param cfg
	 *            the configuration to check
	 * @return true if the constraint is satistied
	 */
	boolean isSatisfied(Configuration cfg);

	public void inject(ReconfigurationProblem core);

	/**
	 * should call {@link #isSatisfied(plan.getDestination())}
	 */
	public boolean isSatisfied(ReconfigurationResult plan);

	/**
	 * Get the virtual machines involved in the constraints.
	 * 
	 * @return a set of virtual machines.
	 */
	ManagedElementSet<VirtualMachine> getVMs();

	/**
	 * Get the nodes explicitely involved in the constraints.
	 * 
	 * @return a set of nodes that may be empty
	 */
	ManagedElementSet<Node> getNodes();

	/** The possible types for the constraint. */
	enum Type {
		/**
		 * The constraint restricts the placement of VMs to nodes. Nodes and VMs
		 * are given
		 */
		absolute,
		/**
		 * The constraint restrict the relative placement of VMs or the hosting
		 * capacities of nodes.
		 */
		relative
	}

	/**
	 * Get the type of the constraint.
	 * 
	 * @return a possible type
	 */
	Type getType();
}
