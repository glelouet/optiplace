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

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.action.Action;
import entropy.view.scheduling.action.Shutdown;
import entropy.view.scheduling.actionModel.slice.DemandingSlice;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * An action to model the shutdown process of a node.
 * 
 * @author Fabien Hermenier
 */
public class ShutdownNodeActionModel extends NodeActionModel {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ShutdownNodeActionModel.class);

	private final IntVar end;

	private final IntVar cost;

	/**
	 * Make a new action.
	 * <p/>
	 * TODO: describe additional constraints
	 * 
	 * @param model
	 *            the model of the reconfiguration problem
	 * @param n
	 *            the node involved in the action
	 * @param d
	 *            the duration of the action
	 */
	public ShutdownNodeActionModel(SchedulingView model, Node n, int d) {
		super(n);
		ReconfigurationProblem problem = model.getProblem();
		dSlice = new DemandingSlice(model, "shutdown(" + n.getName() + ")",
				problem.node(n), n.getCoreCapacity() * n.getNbOfCores(),
				n.getMemoryCapacity());
		dSlice.addToModel(model);
		end = model.getEnd();
		cost = model.getEnd();
		duration = problem.createIntegerConstant("d(shutdown(" + n.getName()
				+ ")", d);
		try {

			dSlice.duration().setInf(d);
		} catch (ContradictionException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * The action starts at the moment the slice starts.
	 * 
	 * @return <code>getDemandingSlice().start()</code>
	 */
	@Override
	public IntVar start() {
		return dSlice.start();
	}

	/**
	 * The action ends at the moment the slice ends.
	 * 
	 * @return <code>getDemandingSlice().end()</code>
	 */
	@Override
	public IntVar end() {
		return end;
	}

	@Override
	public List<Action> getDefinedAction(ReconfigurationProblem solver) {
		// If the node was online, shutdown action. No action otherwise
		ArrayList<Action> l = new ArrayList<Action>();
		if (solver.getSourceConfiguration().isOnline(getNode())) {
			l.add(new Shutdown(getNode(), start().getVal(), end().getVal()));
		}
		return l;
	}

	@Override
	public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
		return cfg.addOffline(getNode());
	}

	@Override
	public String toString() {
		return new StringBuilder("shutdown(").append(getNode().getName())
				.append(")").toString();
	}

	@Override
	public DemandingSlice getDemandingSlice() {
		return dSlice;
	}

	@Override
	public IntVar getGlobalCost() {
		return cost;
	}
}
