/**
 *
 */
package fr.emn.optiplace.solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class HeuristicsListTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HeuristicsListTest.class);

	private static class EmptyIntVarActivatedHeuristic extends ActivatedHeuristic<IntVar> {

		protected EmptyIntVarActivatedHeuristic(IntVar... vars) {
			super(vars, new Variable[] {});
		}


		@Override
		protected boolean checkActivated() {
			return true;
		}

		@Override
		public Decision<IntVar> getDecision() {
			return null;
		}
	}

	@Test
	public void testConcat() {
		Model s = new Model();
		ActivatedHeuristic<IntVar> a0 = new EmptyIntVarActivatedHeuristic(s.intVar(5), s.intVar(20));
		ActivatedHeuristic<IntVar> a1 = new EmptyIntVarActivatedHeuristic(s.intVar(50), s.intVar(100));
		Variable[] res = HeuristicsList.concatVars(a0, a1);
		Assert.assertEquals(res, new Variable[] { s.intVar(5), s.intVar(20), s.intVar(50), s.intVar(100) });
	}

	/**
	 * if VAL variables from an array VAR are instantiated, proposes to set
	 * VAR[VAL]=VAL
	 *
	 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
	 *
	 */
	private static class AffectIntVarActivatedHeuristic extends ActivatedHeuristic<IntVar> {

		protected final int val;

		protected AffectIntVarActivatedHeuristic(int val, IntVar... vars) {
			super(vars, vars);
			this.val = val;
		}

		@Override
		public Decision<IntVar> getDecision() {
			if (!isActivated()) {
				throw new UnsupportedOperationException("calling a passive heuristic .#getDecision() is forbidden");
			}
			if (!vars[val].isInstantiated()) {
				return decisions.makeIntDecision(vars[val], DecisionOperatorFactory.makeIntEq(), val);
			}
			return null;
		}

		@Override
		protected boolean checkActivated() {
			int nbInst = 0;
			for (Variable v : observed) {
				if (v.isInstantiated()) {
					nbInst += 1;
				}
			}
			return nbInst == val;
		}

		@Override
		public String toString() {
			return "affect(" + val + " to " + vars[val] + " if #instantiated == " + val + ")";
		}
	}

	// @Test
	public void testLaunch2Vars() {
		Model s = new Model();
		IntVar a = s.intVar("a", 0, 1, true);
		IntVar b = s.intVar("b", 0, 1, true);
		IntVar[] vars = new IntVar[] { a, b };

		AffectIntVarActivatedHeuristic haa = new AffectIntVarActivatedHeuristic(0, vars);
		AffectIntVarActivatedHeuristic hab = new AffectIntVarActivatedHeuristic(1, vars);
		HeuristicsList hl = new HeuristicsList(s, hab, haa);
		s.getSolver().setSearch(hl);
		// SearchMonitorFactory.log(s, true, true);

		Solution sol = s.getSolver().findSolution();
		Assert.assertEquals(sol.getIntVal(a), 0);
		Assert.assertEquals(sol.getIntVal(b), 1);
	}

	// @Test(dependsOnMethods = "testLaunch2Vars")
	public void testLaunch3Vars() {
		Model s = new Model();
		IntVar a = s.intVar("a", 0, 3, true);
		IntVar b = s.intVar("b", 0, 3, true);
		IntVar c = s.intVar("c", 0, 3, true);
		IntVar[] vars = new IntVar[] { a, b, c };

		AffectIntVarActivatedHeuristic haa = new AffectIntVarActivatedHeuristic(0, vars);
		AffectIntVarActivatedHeuristic hab = new AffectIntVarActivatedHeuristic(1, vars);
		AffectIntVarActivatedHeuristic hac = new AffectIntVarActivatedHeuristic(2, vars);
		HeuristicsList hl = new HeuristicsList(s, hab, haa, hac);
		s.getSolver().setSearch(hl);
		// SearchMonitorFactory.log(s, true, true);

		Solution sol = s.getSolver().findSolution();
		Assert.assertEquals(sol.getIntVal(a), 0);
		Assert.assertEquals(sol.getIntVal(b), 1);
		Assert.assertEquals(sol.getIntVal(c), 2);
	}
}
