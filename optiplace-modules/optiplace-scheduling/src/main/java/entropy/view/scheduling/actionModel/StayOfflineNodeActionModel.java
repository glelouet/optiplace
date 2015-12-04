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

import java.util.List;

import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.action.Action;
import entropy.view.scheduling.actionModel.slice.DemandingSlice;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * An action to model the shutdown process of a node.
 * 
 * @author Fabien Hermenier
 */
public class StayOfflineNodeActionModel extends NodeActionModel {

	private final IntVar zero;

	/**
	 * Make a new action.
	 * 
	 * @param model
	 *            the model of the reconfiguration problem
	 * @param n
	 *            the node involved in the action
	 */
	public StayOfflineNodeActionModel(SchedulingView model, Node n) {
		super(n);
		ReconfigurationProblem problem = model.getProblem();
		zero = problem.createIntegerConstant("cost", 0);
		dSlice = new DemandingSlice(model, "stayOffline(" + n.getName() + ")",
				problem.node(n), 0, n.getCPUCapacity(), n.getMemoryCapacity());
	}

	/**
	 * No action, so start moment equals 0
	 * 
	 * @return <code>the constant 0</code>
	 */
	@Override
	public IntVar start() {
		return zero;
	}

	/**
	 * No action, so end moment equals 0
	 * 
	 * @return <code>the constant 0</code>
	 */
	@Override
	public IntVar end() {
		return zero;
	}

	/**
	 * No action, so duration equals 0
	 * 
	 * @return <code>the constant 0</code>
	 */
	@Override
	public IntVar getDuration() {
		return zero;
	}

	/** @return {@code null} */
	@Override
	public List<Action> getDefinedAction(ReconfigurationProblem solver) {
		return null; // To change body of implemented methods use File |
		// Settings | File Templates.
	}

	@Override
	public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
		return cfg.addOffline(getNode());
	}

	@Override
	public String toString() {
		return new StringBuilder("stayOffline(").append(getNode().getName())
				.append(")").toString();
	}

	@Override
	public DemandingSlice getDemandingSlice() {
		return dSlice;
	}

	/**
	 * No action, so cost equals 0
	 * 
	 * @return <code>the constant 0</code>
	 */
	@Override
	public IntVar getGlobalCost() {
		return zero;
	}
}
