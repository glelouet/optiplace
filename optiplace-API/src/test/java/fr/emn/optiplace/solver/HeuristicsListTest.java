/**
 *
 */
package fr.emn.optiplace.solver;

import org.testng.Assert;
import org.testng.annotations.Test;

import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.variables.IntVar;
import solver.variables.VF;
import solver.variables.Variable;

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

	private static final long serialVersionUID = 1L;

	@Override
	protected void checkActivated() {
	    activated = true;
	}

	@Override
	public void init() throws ContradictionException {
	}

	@Override
	public Decision<IntVar> getDecision() {
	    return null;
	}
    }

    @Test
    public void testConcat() {
	Solver s = new Solver();
	ActivatedHeuristic<IntVar> a0 = new EmptyIntVarActivatedHeuristic(VF.fixed(5, s), VF.fixed(20, s));
	ActivatedHeuristic<IntVar> a1 = new EmptyIntVarActivatedHeuristic(VF.fixed(50, s), VF.fixed(100, s));
	IntVar[] res = HeuristicsList.concatVars(a0, a1);
	Assert.assertEquals(res, new IntVar[] { VF.fixed(5, s), VF.fixed(20, s), VF.fixed(50, s), VF.fixed(100, s) });
    }

    /**
     * if VAL variables from an array VAR are instantiated, proposes to set
     * VAR[VAL]=VAL
     *
     * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
     *
     */
    private static class AffectIntVarActivatedHeuristic extends ActivatedHeuristic<IntVar> {

	private static final long serialVersionUID = 1L;

	protected final int val;

	protected AffectIntVarActivatedHeuristic(int val, IntVar... vars) {
	    super(vars, vars);
	    this.val = val;
	}

	@Override
	public void init() throws ContradictionException {
	}

	@Override
	public Decision<IntVar> getDecision() {
	    if (!activated) {
		throw new UnsupportedOperationException("calling a passive heuristic .#getDecision() is forbidden");
	    }
	    if (!vars[val].isInstantiated()) {
		FastDecision e = getFastDecision();
		e.set(vars[val], val, DecisionOperator.int_eq);
		return e;
	    }
	    return null;
	}

	@Override
	protected void checkActivated() {
	    int nbInst = 0;
	    for (Variable v : observed) {
		if (v.isInstantiated()) {
		    nbInst += 1;
		}
	    }
	    activated = nbInst == val;
	}

	@Override
	public String toString() {
	    return "affect(" + val + " to " + vars[val] + " if #instantiated == " + val + ")";
	}
    }

    @Test
    public void testLaunch2Vars() {
	Solver s = new Solver();
	IntVar a = VF.bounded("a", 0, 1, s);
	IntVar b = VF.bounded("b", 0, 1, s);
	IntVar[] vars = new IntVar[] { a, b };

	AffectIntVarActivatedHeuristic haa = new AffectIntVarActivatedHeuristic(0, vars);
	AffectIntVarActivatedHeuristic hab = new AffectIntVarActivatedHeuristic(1, vars);
	HeuristicsList<IntVar> hl = new HeuristicsList<>(s, hab, haa);
	s.set(hl);
	// SearchMonitorFactory.log(s, true, true);

	s.findSolution();
	Assert.assertEquals(a.getValue(), 0);
	Assert.assertEquals(b.getValue(), 1);
    }

    @Test(dependsOnMethods = "testLaunch2Vars")
    public void testLaunch3Vars() {
	Solver s = new Solver();
	IntVar a = VF.bounded("a", 0, 3, s);
	IntVar b = VF.bounded("b", 0, 3, s);
	IntVar c = VF.bounded("c", 0, 3, s);
	IntVar[] vars = new IntVar[] { a, b, c };

	AffectIntVarActivatedHeuristic haa = new AffectIntVarActivatedHeuristic(0, vars);
	AffectIntVarActivatedHeuristic hab = new AffectIntVarActivatedHeuristic(1, vars);
	AffectIntVarActivatedHeuristic hac = new AffectIntVarActivatedHeuristic(2, vars);
	HeuristicsList<IntVar> hl = new HeuristicsList<>(s, hab, haa, hac);
	s.set(hl);
	// SearchMonitorFactory.log(s, true, true);

	s.findSolution();
	Assert.assertEquals(a.getValue(), 0);
	Assert.assertEquals(b.getValue(), 1);
	Assert.assertEquals(c.getValue(), 2);
    }
}
