/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.core.heuristics;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;

/**
 * A dummy placement heuristic. Branch on all the variables in a static manner,
 * and select the minimum value for each selected variable.
 *
 * @author Fabien Hermenier
 * @author Guillaume Le Louët[guillaume.lelouet@gmail.com]2013
 */
public class DummyPlacementHeuristic {

	public static final org.slf4j.Logger        logger   = LoggerFactory.getLogger(DummyPlacementHeuristic.class);

	public static final DummyPlacementHeuristic INSTANCE = new DummyPlacementHeuristic();

	public List<AbstractStrategy<? extends Variable>> getHeuristics(IReconfigurationProblem m) {
		List<AbstractStrategy<? extends Variable>> ret = new ArrayList<>();

		LinkedHashSet<IntVar> vars = new LinkedHashSet<>();
		for (IntVar v : m.getNodes()) {
			vars.add(v);
		}
		IntVar[] exts = m.getExterns();
		if (exts != null) {
			for (IntVar v : exts) {
				vars.add(v);
			}
		}
		for (IntVar v : m.getSolver().retrieveIntVars()) {
			vars.add(v);
		}
		for (BoolVar v : m.getSolver().retrieveBoolVars()) {
			vars.add(v);
		}
		if (vars.size() > 0) {
			ret.add(SearchGoal.makeAssignHeuristic(getClass().getSimpleName() + ".IntVar", new InputOrder<>(),
			    new IntDomainMin(), vars.toArray(new IntVar[] {})));
		}

		ArrayList<SetVar> bar = new ArrayList<>();
		for (SetVar v : m.getSolver().retrieveSetVars()) {
			bar.add(v);
		}
		if (bar.size() > 0) {
			ret.add(SearchGoal.makeAssignHeuristic(getClass().getSimpleName() + ".SetVar", new InputOrder<>(),
			    new SetDomainMin(), true, bar.toArray(new SetVar[] {})));
		}
		return ret;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
