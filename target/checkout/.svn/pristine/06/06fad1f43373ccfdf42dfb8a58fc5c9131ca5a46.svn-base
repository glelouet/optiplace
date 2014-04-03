/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.core.heuristics;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.slf4j.LoggerFactory;

import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.cp.solver.search.set.StaticSetVarOrder;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;
import entropy.solver.choco.ReconfigurationProblem;
import entropy.view.SearchHeuristic;

/**
 * A dummy placement heuristic. Branch on all the variables in a static manner,
 * and select the minimum value for each selected variable.
 * 
 * @author Fabien Hermenier
 * @author Guillaume Le Louët[guillaume.lelouet@gmail.com]2013
 */
public class DummyPlacementHeuristic implements SearchHeuristic {

	public static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(DummyPlacementHeuristic.class);

	public static final DummyPlacementHeuristic INSTANCE = new DummyPlacementHeuristic();

	@Override
	public List<AbstractIntBranchingStrategy> getHeuristics(
			ReconfigurationProblem m) {
		ArrayList<AbstractIntBranchingStrategy> ret = new ArrayList<AbstractIntBranchingStrategy>();

		LinkedHashSet<IntDomainVar> vars = new LinkedHashSet<>();
		for (IntDomainVar v : m.getHosters()) {
			vars.add(v);
		}
		for (int i = 0; i < m.getNbIntVars(); i++) {
			vars.add(m.getIntVarQuick(i));
		}

		SetVar[] bar = new SetVar[m.getNbSetVars()];
		for (int i = 0; i < bar.length; i++) {
			bar[i] = m.getSetVarQuick(i);
		}
		if (vars.size() > 0) {
			ret.add(new AssignVar(new StaticVarOrder(m, vars
					.toArray(new IntDomainVar[]{})) {
				@Override
				public IntDomainVar selectVar() {
					IntDomainVar ret = super.selectVar();
					if (ret != null) {
						System.err.println("dummy selected " + ret.pretty());
					}
					return ret;
				}
			}, new MinVal()));
		}
		if (bar.length > 0) {
			ret.add(new AssignVar(new StaticSetVarOrder(m, bar), new MinVal()));
		}
		return ret;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
