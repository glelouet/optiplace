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
import fr.emn.optiplace.configuration.Node;

/**
 * A consuming slice is a slice that starts at the beginning of a
 * reconfiguration process. The slice is already hosted on a node.
 * 
 * @author Fabien Hermenier
 */
public class ConsumingSlice extends Slice {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ConsumingSlice.class);

	/**
	 * Make a new consuming slice.
	 * 
	 * @param model
	 *            the model of the reconfiguration problem
	 * @param name
	 *            the identifier of the slice
	 * @param node
	 *            the current hoster of the slice
	 * @param cpu
	 *            the CPU height of the slice
	 * @param mem
	 *            the memory height of the slice
	 */
	public ConsumingSlice(SchedulingView model, String name, Node node,
			int cpu, int mem) {
		super(name, model.getProblem().createIntegerConstant(
				"" + model.getProblem().node(node),
				model.getProblem().node(node)),
				model.getProblem()
						.createTaskVar(
								name,
								model.getStart(),
								model.getProblem().createBoundIntVar(
										"ed(" + name + ")", 0,
										SchedulingView.MAX_TIME),
								model.getProblem().createBoundIntVar(
										"d(" + name + ")", 0,
										SchedulingView.MAX_TIME)), cpu, mem);
	}

	/**
	 * Make a new consuming slice.
	 * 
	 * @param model
	 *            the model of the reconfiguration problem
	 * @param name
	 *            the identifier of the slice
	 * @param node
	 *            the hosting node of the slice
	 * @param cpu
	 *            the CPU height of the slice
	 * @param mem
	 *            the memory height of the slice
	 * @param duration
	 *            the fixed duration of the slice
	 */
	public ConsumingSlice(SchedulingView model, String name, Node node,
			int cpu, int mem, int duration) {
		super(name, model.getProblem().createIntegerConstant("h(" + name + ")",
				model.getProblem().node(node)),
				model.getProblem()
						.createTaskVar(
								name,
								model.getStart(),
								// new IntVarAddCste(model, "ed(" + name +
								// ")",
								// model.getStart(),
								// duration),
								model.getProblem().createBoundIntVar(
										"ed(" + name + ")", 0,
										SchedulingView.MAX_TIME),
								model.getProblem().createIntegerConstant(
										"d(" + name + ")", duration)), cpu, mem);

	}

	public ConsumingSlice(String name, IntVar host, TaskVar<?> t,
			int cpu, int mem) {
		super(name, host, t, cpu, mem);
	}

	/**
	 * Fix the end moment of the slice.
	 * 
	 * @param t
	 *            the moment the action ends
	 */
	public void fixEnd(int t) {
		try {
			end().setVal(t);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
