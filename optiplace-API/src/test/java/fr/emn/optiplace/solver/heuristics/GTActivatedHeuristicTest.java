package fr.emn.optiplace.solver.heuristics;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;

import fr.emn.optiplace.solver.ActivatedHeuristic;
import fr.emn.optiplace.solver.HeuristicsList;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class GTActivatedHeuristicTest {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GTActivatedHeuristicTest.class);

	/**
	 * create a problem which can only be resolved as var[i]=i. each var has a
	 * range as big as the number of vars, and propagators chose to enforce
	 * vars[i-1]&lt;var[i] ; so the only first solution found is 0, 1, 2…
	 */
	@SuppressWarnings("unchecked")
	// @Test
	public void testLaunchvars() {
		int nbVars = 10;
		Model s = new Model();
		// SMF.log(s, true, true);
		IntVar[] vars = new IntVar[nbVars];
		ActivatedHeuristic<IntVar>[] hs = new ActivatedHeuristic[nbVars - 1];
		for (int i = 0; i < nbVars; i++) {
			vars[i] = s.intVar("v_" + i, 0, nbVars - 1, true);
			if (i != 0) {
				hs[i - 1] = new GTActivatedHeuristic(vars[i - 1], vars[i]);
			}
		}
		s.getSolver().setSearch(new HeuristicsList(s, hs));
		Solution sol = s.getSolver().findSolution();
		for (int i = 0; i < nbVars; i++) {
			Assert.assertEquals(sol.getIntVal(vars[i]), i);
		}
	}
}
