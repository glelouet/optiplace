/**
 *
 */

package fr.emn.optiplace.solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.solver.heuristics.Static2Activated;


/**
 * <p>
 * test when an heuristicList leads to a error on next heuristics . It should
 * then backtrack, we test if the backtrack keeps the listHeuristic
 * </p>
 * <p>
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class HeuristicListTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HeuristicListTest.class);

	/**
	 * Iterate over a variable, and possibly insert a temp constraint on
	 * getDecision
	 *
	 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
	 *
	 */
	public static class IterateOverVar extends IntStrategy {

		protected static class VarSelUnique implements VariableSelector<IntVar> {

			private final IntVar var;

			public VarSelUnique(IntVar var) {
				this.var = var;
			}

			@Override
			public IntVar getVariable(IntVar[] variables) {
				if (var.isInstantiated()) {
					return null;
				} else {
					return var;
				}
			}

		}

		protected static class ValSelLB implements IntValueSelector {

			@Override
			public int selectValue(IntVar var) {
				return var.getLB();
			}
		}

		/** constraint to add on getDecision(), keep on null to do nothing */
		protected Constraint toAdd = null;

		@SuppressWarnings("rawtypes")
		@Override
		public Decision getDecision() {
			if (toAdd != null) {
				try {
					vars[0].getModel().post(toAdd);
					// vars[0].getSolver().propagate();
					// leads to fail, as well as
					// vars[0].getSolver().postTemp(toAdd);
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
					return null;
				}
			}
			return super.getDecision();
		}

		public IterateOverVar(IntVar var) {
			super(new IntVar[] {
					var
			}, new VarSelUnique(var), new ValSelLB());
		}

		/**
		 * set a constraint to add on getDecision() and return this
		 *
		 * @param c
		 *          the constraint to add on first Variable selection.
		 * @return this
		 */
		public IterateOverVar withConstraint(Constraint c) {
			toAdd = c;
			return this;
		}

	}

	@Test
	public void testpostTemp() {
		Model s = new Model();

		IntVar a = s.boolVar("a");
		IterateOverVar ia = new IterateOverVar(a);

		IntVar b = s.boolVar("b");
		IterateOverVar ib = new IterateOverVar(b).withConstraint(s.arithm(a, "!=", 0));

		IntVar c = s.boolVar("c");
		IterateOverVar ic = new IterateOverVar(c);
		s.getSolver().setSearch(Search.sequencer(ia, ib, ic));
		// SearchMonitorFactory.log(s, true, true);
		s.getSolver().findSolution();
	}

	/**
	 * we test the result when an ActivatedHeuristic makes a Decision which leads
	 * to a contradiction.<br />
	 * The internal ActivatedHeuristic makes to selections : the first one leads
	 * to a contradiction, the second to a solution.<br />
	 * To "lead" to a bad solution, a second heuristic will make the search fail
	 * on its first call.
	 */
	@Test(dependsOnMethods = "testpostTemp")
	public void testFirstOptimizeFalse() {
		Model s = new Model();

		IntVar a = s.boolVar("a");
		IterateOverVar ia = new IterateOverVar(a);

		IntVar b = s.boolVar("b");
		IterateOverVar ib = new IterateOverVar(b);

		IntVar c = s.boolVar("c");
		IterateOverVar ic = new IterateOverVar(c).withConstraint(s.arithm(a, "!=", b));

		IntVar d = s.boolVar("d");
		IterateOverVar id = new IterateOverVar(d);

		HeuristicsList hl = new HeuristicsList(s, new Static2Activated<>(ia), new Static2Activated<>(ib));
		s.getSolver().setSearch(Search.sequencer(hl, ic, id));
		// SearchMonitorFactory.log(s, true, true);
		Solution sol = s.getSolver().findSolution();
		Assert.assertEquals(sol.getIntVal(a), 0);
		Assert.assertEquals(sol.getIntVal(b), 1);
		Assert.assertEquals(sol.getIntVal(c), 0);
		Assert.assertEquals(sol.getIntVal(d), 0);
	}
}
