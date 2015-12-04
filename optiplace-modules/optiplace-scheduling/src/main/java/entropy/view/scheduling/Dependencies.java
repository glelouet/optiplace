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

package entropy.view.scheduling;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import entropy.view.scheduling.action.Action;

/**
 * Dependencies defines the actions that must be terminated to make another
 * action feasible.
 * 
 * @author Fabien Hermenier
 */
public class Dependencies {

	private final Action action;

	/** All the dependencies. */
	private final Set<Action> dependencies;

	/**
	 * Make a new direct dependencies for a specific action.
	 * 
	 * @param a
	 *            the action
	 */
	public Dependencies(Action a) {
		this.action = a;
		this.dependencies = new HashSet<Action>();
	}

	/**
	 * Get the action that have the dependencies.
	 * 
	 * @return an action as I say earlier
	 */
	public Action getAction() {
		return this.action;
	}

	/**
	 * Indicates the feasibility of the action. An action is feasible iff there
	 * is no dependencies.
	 * 
	 * @return true if the action is feasible
	 */
	public boolean isFeasible() {
		return this.dependencies.isEmpty();
	}

	/**
	 * Get the unsatisfied dependencies.
	 * 
	 * @return a set of action. May be empty
	 */
	public Set<Action> getUnsatisfiedDependencies() {
		return this.dependencies;
	}

	/**
	 * Add a dependency.
	 * 
	 * @param a
	 *            an action
	 */
	public void addDependency(Action a) {
		this.dependencies.add(a);
	}

	/**
	 * Remove a dependency.
	 * 
	 * @param a
	 *            the action to remove
	 * @return true if the dependency has been removed. false if the action was
	 *         not a dependency
	 */
	public boolean removeDependency(Action a) {
		return this.dependencies.remove(a);
	}

	/**
	 * Textual representation of the direct dependencies.
	 * 
	 * @return a formatted String. May be empty
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(action).append(" depends on [");
		if (this.dependencies.isEmpty()) {
			buffer.append(" - ");
		} else {
			for (Iterator<Action> ite = this.dependencies.iterator(); ite
					.hasNext();) {
				buffer.append(ite.next());
				if (ite.hasNext()) {
					buffer.append(", ");
				}
			}
		}
		buffer.append("]");
		return buffer.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Dependencies that = (Dependencies) o;

		return !(!action.equals(that.action) || !dependencies
				.equals(that.dependencies));
	}

	@Override
	public int hashCode() {
		return 31 * action.hashCode() + dependencies.hashCode();
	}
}
