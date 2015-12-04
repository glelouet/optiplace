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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import entropy.view.scheduling.action.Action;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.solver.ReconfigurationResult;

/**
 * A reconfiguration plan based on Action.
 * 
 * @author Fabien Hermenier
 */
public class DefaultTimedReconfigurationPlan extends ReconfigurationResult
		implements
			TimedReconfigurationPlan {

	/** The theorical duration of the reconfiguration plan. */
	private int duration;

	/** The list of actions to perform. */
	private final Set<Action> actions;

	/**
	 * Indicates wether we have to check the compatibility with the destination
	 * configuration (when {@code true}) or update the destination
	 * configuration.
	 */
	private boolean statedDestination = true;

	/**
	 * Make a new plan that starts from a specified configuration. The
	 * destination configuration will be updated for each added action.
	 * 
	 * @param src
	 *            the source configuration
	 */
	public DefaultTimedReconfigurationPlan(Configuration src) {
		this(src, null);
	}

	/**
	 * Make a new plan, starting from a specified configuration. The plan has to
	 * reach the destination configuration. Each action must be compatible with
	 * the destination configuration.
	 * 
	 * @param src
	 *            the initial configuration
	 * @param dst
	 *            the configuration to reach
	 */
	public DefaultTimedReconfigurationPlan(Configuration src, Configuration dst) {
		actions = new HashSet<Action>();
		source = src;
		destination = dst;

		if (destination == null) {
			statedDestination = false;
			destination = source.clone();
		}
	}

	@Override
	public boolean add(Action ta) {

		// Check the action is compatible with the configuration
		if (statedDestination && !ta.isCompatibleWith(source, destination)) {
			return false;

		} else {
			if (!ta.apply(destination)) {
				return false;
			}
		}
		boolean ret = actions.add(ta);
		if (ret && ta.getFinishMoment() > duration) {
			duration = ta.getFinishMoment();
		}
		return ret;
	}

	@Override
	public String toString() {
		Map<Integer, StringBuilder> planning = new HashMap<Integer, StringBuilder>();
		for (Action action : actions) {
			StringBuilder str = planning.get(action.getStartMoment());
			if (str == null) {
				str = new StringBuilder();
			} else {
				str.append(" ");
			}
			str.append("start(").append(action.toString()).append(")");
			planning.put(action.getStartMoment(), str);

			str = planning.get(action.getFinishMoment());
			if (str == null) {
				str = new StringBuilder();
			} else {
				str.append(" ");
			}
			str.append("stop(").append(action.toString()).append(")");
			planning.put(action.getFinishMoment(), str);
		}
		StringBuilder buffer = new StringBuilder();
		SortedSet<Integer> keys = new TreeSet<Integer>(planning.keySet());
		for (Integer moment : keys) {
			buffer.append(moment);
			buffer.append(": ");
			buffer.append(planning.get(moment));
			buffer.append("\n");
		}
		return buffer.toString();
	}

	@Override
	public Iterator<Action> iterator() {
		return actions.iterator();
	}

	@Override
	public int size() {
		return actions.size();
	}

	@Override
	public TimedExecutionGraph extractExecutionGraph() {
		TimedExecutionGraph g = new TimedExecutionGraph();
		for (Action as : actions) {
			as.insertIntoGraph(g);
		}
		return g;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public Set<Action> getActions() {
		return actions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof DefaultTimedReconfigurationPlan)) {
			return false;
		}
		DefaultTimedReconfigurationPlan that = (DefaultTimedReconfigurationPlan) o;
		return source.equals(that.getSource())
				&& actions.equals(that.getActions());
	}

	@Override
	public int hashCode() {
		int result = actions.hashCode();
		result = 31 * result + source.hashCode();
		return result;
	}
}
