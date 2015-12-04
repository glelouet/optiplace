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

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.scheduling.TaskVar;
import entropy.view.scheduling.SchedulingView;

/**
 * A demanding slice is a slice that ends at the end of a reconfiguration
 * process. The slice has to be assigned to a node.
 * 
 * @author Fabien Hermenier
 */
public class DemandingSlice extends Slice {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DemandingSlice.class);

	/**
	 * Make a demanding slice.
	 * 
	 * @param core
	 *            the model of the reconfiguration problem
	 * @param name
	 *            the name of the slice
	 * @param cpu
	 *            the CPU height of the slice
	 * @param mem
	 *            the memory height of the slice
	 */
	public DemandingSlice(SchedulingView core, String name, int cpu, int mem) {
		super(name, core.getProblem().createEnumIntVar(name, 0,
				core.getProblem().nodes().length - 1), core.getProblem()
				.createTaskVar(
						"t(" + name + ")",
						core.getProblem().createBoundIntVar("s(" + name + ")",
								0, SchedulingView.MAX_TIME),
						core.getEnd(),
						core.getProblem().createBoundIntVar("d(" + name + ")",
								0, SchedulingView.MAX_TIME)), cpu, mem);
	}

	/**
	 * Make a demanding slice.
	 * 
	 * @param core
	 *            the model of the reconfiguration problem
	 * @param name
	 *            the name of the slice
	 * @param hoster
	 *            the index of the node that will host the slice
	 * @param cpu
	 *            the CPU height of the slice
	 * @param mem
	 *            the memory height of the slice
	 */
	public DemandingSlice(SchedulingView core, String name, int hoster,
			int cpu, int mem) {
		super(name, core.getProblem().createIntegerConstant("h(" + name + ")",
				hoster), core.getProblem().createTaskVar(
				"t(" + name + ")",
				core.getProblem().createBoundIntVar("s(" + name + ")", 0,
						SchedulingView.MAX_TIME),
				core.getEnd(),
				core.getProblem().createBoundIntVar("d(" + name + ")", 0,
						SchedulingView.MAX_TIME)), cpu, mem);
	}

	/**
	 * Make a demanding slice.
	 * 
	 * @param core
	 *            the model of the reconfiguration problem
	 * @param name
	 *            the name of the slice
	 * @param hoster
	 *            the index of the node that will host the slice
	 * @param start
	 *            the moment the action start
	 * @param cpu
	 *            the CPU height of the slice
	 * @param mem
	 *            the memory height of the slice
	 */
	public DemandingSlice(SchedulingView core, String name, int hoster,
			int start, int cpu, int mem) {
		super(name, core.getProblem().createIntegerConstant("h(" + name + ")",
				hoster), core.getProblem().createTaskVar(
				"t(" + name + ")",
				core.getProblem().createIntegerConstant("s(" + name + ")",
						start),
				core.getEnd(),
				core.getProblem().createBoundIntVar("d(" + name + ")", 0,
						SchedulingView.MAX_TIME)), cpu, mem);
	}

	public DemandingSlice(String name, IntVar host, TaskVar<?> t,
			int cpu, int mem) {
		super(name, host, t, cpu, mem);
	}

	/**
	 * Fix the node that will host the slice.
	 * 
	 * @param idx
	 *            the index of the node
	 */
	public void fixHoster(int idx) {
		try {
			hoster().setVal(idx);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Fix the start moment of the slice.
	 * 
	 * @param t
	 *            the moment the action starts
	 */
	public void fixStart(int t) {
		try {
			start().setVal(t);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
