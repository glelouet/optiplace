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

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.set.SetVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.action.Action;
import entropy.view.scheduling.action.Startup;
import entropy.view.scheduling.actionModel.slice.ConsumingSlice;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.solver.choco.Chocos;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * An action to model a potential boot of a node. The action is modeled with a
 * consuming action.
 * <p/>
 * TODO: Use sets for bins to detect future hosting nodes easily.
 * 
 * @author Fabien Hermenier
 */
public class BootableNodeActionModel extends NodeActionModel {

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(BootableNodeActionModel.class);

	private final IntVar cost;

	/**
	 * Make a new action.
	 * <p/>
	 * The following constraint is added into the model:
	 * <ul>
	 * <li>{@code getConsumingSlice().duration() = cost }</li>
	 * </ul>
	 * 
	 * @param model
	 *            the model of the reconfiguration problem
	 * @param n
	 *            the node involved in the action
	 * @param d
	 *            the duration of the action if it occurred
	 */
	@SuppressWarnings("unchecked")
	public BootableNodeActionModel(SchedulingView model, Node n, int d) {
		super(n);
		ReconfigurationProblem problem = model.getProblem();
		cSlice = new ConsumingSlice(model, "boot?(" + n.getName() + ")", n,
				n.getCoreCapacity() * n.getNbOfCores(), n.getMemoryCapacity(),
				d);

		cost = problem.createEnumIntVar("cost(" + toString() + ")", new int[]{
				0, d});
		SetVar s = problem.getSetModel(getNode());
		Chocos.postImplies(problem, problem.gt(s.getCard(), 0),
				problem.eq(cost, d));
		try {
			cSlice.end().setInf(d);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		cSlice.addToModel(model);
	}

	/**
	 * Return the start of the slice.
	 * 
	 * @return <code>getConsumingSlice().start()</code>
	 */
	@Override
	public final IntVar start() {
		return cSlice.start();
	}

	/**
	 * Return the end of the slice.
	 * 
	 * @return <code>getConsumingSlice().end()</code>
	 */
	@Override
	public final IntVar end() {
		return cSlice.end();
	}

	@Override
	public List<Action> getDefinedAction(ReconfigurationProblem solver) {
		ArrayList<Action> l = new ArrayList<Action>();
		if (cost.getVal() != 0) {
			l.add(new Startup(getNode(), start().getVal(), end().getVal()));
		}
		return l;
	}

	@Override
	public boolean putResult(ReconfigurationProblem solver, Configuration cfg) {
		// Check weither a VM will be on the node to boot it.
		if (cost.getVal() != 0) {
			cfg.addOnline(getNode());
		} else {
			cfg.addOffline(getNode());
		}
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder("boot(").append(getNode().getName())
				.append(")").toString();
	}

	@Override
	public IntVar getDuration() {
		return cost;
	}
}
