/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */
package entropy.view.scheduling.actionModel.slice;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.scheduling.TaskVar;
import entropy.view.scheduling.SchedulingView;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * Model a period where a managed element is consuming CPU and memory resources
 * during a bounded amount of time on a node.
 * 
 * @author Fabien Hermenier
 */
public class Slice {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(Slice.class);

	private final TaskVar<?> task;

	/** Indicates the identifier of slice hoster. */
	private final IntVar hoster;

	/** The CPU height of the slice. */
	private final int cpuHeight;

	/** The memory height of the slice. */
	private final int memHeight;

	private final String name;

	/**
	 * Make a new slice.
	 * 
	 * @param name
	 *            the name of the slice
	 * @param h
	 *            the hoster of the slice (its identifier)
	 * @param t
	 *            The associated task variable
	 * @param cpuHeight
	 *            the CPU height of the slice
	 * @param memHeight
	 *            the memory height of the slice
	 */
	public Slice(String name, IntVar h, TaskVar<?> t, int cpuHeight,
			int memHeight) {
		this.name = name;
		task = t;
		hoster = h;
		this.cpuHeight = cpuHeight;
		this.memHeight = memHeight;
	}

	/**
	 * Get the CPU consumption of the slice during its activity.
	 * 
	 * @return a positive integer
	 */
	public int getCPUheight() {
		return cpuHeight;
	}

	/**
	 * Get the memory consumption of the slice during its activity.
	 * 
	 * @return a positive integer
	 */
	public int getMemoryheight() {
		return memHeight;
	}

	/**
	 * Get the node that host the slice.
	 * 
	 * @return the index of the node.
	 */
	public IntVar hoster() {
		return hoster;
	}

	/** @return <code>this.pretty()</code> */
	@Override
	public String toString() {
		return pretty();
	}

	/**
	 * Nice print of the slice.
	 * 
	 * @return a formatted String
	 */
	public String pretty() {
		StringBuilder builder = new StringBuilder();
		builder.append(name).append("{[").append(start().getLB()).append(",");
		if (start().getUB() == SchedulingView.MAX_TIME) {
			builder.append("MAX");
		} else {
			builder.append(start().getUB());
		}
		builder.append("] + [").append(duration().getLB()).append(",");
		if (duration().getUB() == SchedulingView.MAX_TIME) {
			builder.append("MAX");
		} else {
			builder.append(duration().getUB());
		}
		builder.append("] = [").append(end().getLB()).append(",");
		if (end().getLB() == SchedulingView.MAX_TIME) {
			builder.append("MAX");
		} else {
			builder.append(end().getUB());
		}
		builder.append("] on [").append(hoster().getLB()).append(",")
				.append(hoster().getUB()).append("]}");
		return builder.toString();
	}

	public static int improvable = 0;
	public static int nonImpr = 0;

	/**
	 * Add the slice to the model.The following are added:
	 * <ul>
	 * <li>The variables {@code end()}, {@code start()}, {@code duration()} and
	 * {@code hoster()}</li>
	 * <li>the constraint <code>start() + duration() = end()</code></li>
	 * <li>A constraint to enforce all the variables to be inferior or equals to
	 * <code>model.getEnd()</code></li>
	 * </ul>
	 * 
	 * @param core
	 *            the current model of the reconfiguration problem
	 */
	@SuppressWarnings("unchecked")
	public void addToModel(SchedulingView core) {
		ReconfigurationProblem problem = core.getProblem();
		core.post(problem.leq(duration(), core.getEnd()));
		if (start().isInstantiated() && duration().isInstantiated()) {
			try {
				end().setVal(duration().getVal() + start().getVal());
			} catch (ContradictionException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			core.post(problem.eq(end(), problem.plus(start(), duration())));
		}

	}

	/**
	 * Get the moment the slice starts.
	 * 
	 * @return a positive moment
	 */
	public IntVar start() {
		return task.start();
	}

	/**
	 * Get the duration of the slice.
	 * 
	 * @return a positive moment
	 */
	public IntVar duration() {
		return task.duration();
	}

	/**
	 * Get the moment the slice ends.
	 * 
	 * @return a positive moment
	 */
	public IntVar end() {
		return task.end();
	}

	/**
	 * Get the name of the slice.
	 * 
	 * @return a String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the duration of the slice as a constant.
	 * 
	 * @param d
	 *            a positive duration
	 */
	public void fixDuration(int d) {
		try {
			duration().setVal(d);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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

		Slice slice = (Slice) o;

		if (cpuHeight != slice.cpuHeight || memHeight != slice.memHeight) {
			return false;
		}
		if (!hoster.equals(slice.hoster)) {
			return false;
		}
		if (!task.start().equals(slice.task.start())
				|| !task.end().equals(slice.task.end())
				|| !task.duration().equals(slice.task.duration())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = task.hashCode();
		result = 31 * result + hoster.hashCode();
		result = 31 * result + cpuHeight;
		result = 31 * result + memHeight;
		return result;
	}
}
