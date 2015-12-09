package fr.emn.optiplace.solver;

/**
 *
 */

import java.util.Arrays;
import java.util.stream.Collectors;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.ESat;
import org.chocosolver.util.PoolManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class TestActivatedHeuristics {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestActivatedHeuristics.class);

	/**
	 * if VAL variables from an array VAR are instantiated, proposes to set
	 * VAR[VAL]=VAL
	 *
	 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
	 *
	 */
	public static class IntVarActivatedStrategy extends AbstractStrategy<IntVar> {

		private static final long serialVersionUID = 1L;

		static PoolManager<IntDecision> manager = new PoolManager<>();

		boolean activated = false;

		public boolean isActivated() {
			return activated;
		}

		final int val;

		/**
		 * @param variables
		 */
		protected IntVarActivatedStrategy(int val, IntVar... variables) {
			super(variables);
			this.val = val;
		}

		@Override
		public Decision<IntVar> getDecision() {
			if (!activated) {
				throw new UnsupportedOperationException("cannot get decision from an unactivated heuristic");
			}
			if (!vars[val].isInstantiated()) {
				IntDecision e = manager.getE();
				if (e == null) {
					e = new IntDecision(manager);
				}
				e.set(vars[val], val, DecisionOperator.int_eq);
				return e;
			}
			return null;
		}

		protected void checkActivated() {
			int nbInst = 0;
			for (IntVar v : vars) {
				if (v.isInstantiated()) {
					nbInst += 1;
				}
			}
			activated = nbInst == val;
		}

		Propagator<IntVar> prop = new Propagator<IntVar>(vars) {

			private static final long serialVersionUID = 1L;

			@Override
			public void propagate(int evtmask) throws ContradictionException {
				checkActivated();
			}

			@Override
			public ESat isEntailed() {
				return vars[val].isInstantiated() ? ESat.TRUE : ESat.UNDEFINED;
			}
		};

		public Propagator<IntVar> getPropagator() {
			return prop;
		}

	}

	/**
	 * contains a list of activatedheuristics to activate and select decision.
	 * each internal heuristic has a propagator to insert into the solver on the
	 * first call to getDecision(). They are also removed from the solver once
	 * they all returned null, meaning they can't be used anymore since this
	 * Strategy returned null and will not be used.
	 *
	 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
	 *
	 */
	public static class IntVarStrategyList extends AbstractStrategy<IntVar> {

		public static IntVar[] extractVars(IntVarActivatedStrategy... vars) {
			return Arrays.asList(vars).stream().flatMap(s -> Arrays.asList(s.getVariables()).stream())
					.collect(Collectors.toList()).toArray(new IntVar[0]);
		}

		private static final long serialVersionUID = 1L;

		private final IntVarActivatedStrategy[] list;

		private final Solver s;

		/**
		 * @param variables
		 */
		protected IntVarStrategyList(Solver s, IntVarActivatedStrategy... strats) {
			super(extractVars(strats));
			list = strats;
			this.s = s;
		}

		boolean inserted = false;

		@Override
		public Decision<IntVar> getDecision() {
			if (!inserted) {
				for (IntVarActivatedStrategy element : list) {
					Propagator<IntVar> p = element.getPropagator();
					s.getEngine().dynamicAddition(true, new Constraint("" + p, p).getPropagators());
					try {
						p.propagate(0);
					} catch (ContradictionException e) {
						logger.warn("", e);
					}
				}
				inserted = true;
			}
			for (IntVarActivatedStrategy ah : list) {
				if (ah.isActivated()) {
					Decision<IntVar> d = ah.getDecision();
					if (d != null) {
						// System.err.println("heuristic " + ah +
						// " activated and chose " + d);
						return d;
					} else {
						// System.err.println("heuristic " + ah +
						// " activated and returned null");
					}
				} else {
					// System.err.println(" " + ah + " not activated");
				}
			}
			// System.err.println("no heuristic available, returning null");
			for (IntVarActivatedStrategy element : list) {
				s.getEngine().desactivatePropagator(element.getPropagator());
			}
			// System.err.println("removing heuristics done");
			return null;
		}

	}

	@Test
	public void test() {
		Solver s = new Solver();
		// SearchMonitorFactory.log(s, true, true);
		IntVar a = VF.bounded("a", 0, 3, s);
		IntVar b = VF.bounded("b", 0, 3, s);
		IntVar c = VF.bounded("c", 0, 3, s);
		IntVar[] vars = new IntVar[] { a, b, c };
		IntVarActivatedStrategy sa = new IntVarActivatedStrategy(0, vars);
		IntVarActivatedStrategy sb = new IntVarActivatedStrategy(1, vars);
		IntVarActivatedStrategy sc = new IntVarActivatedStrategy(2, vars);
		IntVarStrategyList ss = new IntVarStrategyList(s, sc, sb, sa);
		s.set(ss);
		s.findSolution();
		Assert.assertEquals(a.getValue(), 0);
		Assert.assertEquals(b.getValue(), 1);
		Assert.assertEquals(c.getValue(), 2);
	}
}
