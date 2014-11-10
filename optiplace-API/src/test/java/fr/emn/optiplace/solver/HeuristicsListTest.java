/**
 *
 */
package fr.emn.optiplace.solver;

import org.testng.Assert;
import org.testng.annotations.Test;

import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.variables.IntVar;
import solver.variables.VF;
import solver.variables.Variable;
import util.PoolManager;

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
	protected boolean checkActivated() {
	    return true;
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

    private static class AffectIntVarActivatedHeuristic extends ActivatedHeuristic<IntVar> {

	private static final long serialVersionUID = 1L;

	static PoolManager<FastDecision> manager = new PoolManager<>();

	protected int val;
	protected int nbInst;

	protected AffectIntVarActivatedHeuristic(IntVar affect, int val, Variable[] checked, int nbInst) {
	    super(new IntVar[] { affect }, checked);
	    this.val = val;
	    this.nbInst = nbInst;
	}

	@Override
	public void init() throws ContradictionException {
	}

	@Override
	public Decision<IntVar> getDecision() {

	    if (activated) {
		FastDecision e = manager.getE();
		if (e == null) {
		    e = new FastDecision(manager);
		}
		e.set(vars[0], val, DecisionOperator.int_eq);
		return e;
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
	    System.err.println("checking " + this + ", nb instantiated : " + nbInst);
	    return nbInst == this.nbInst;
	}

	@Override
	public String toString() {
	    return "affect(" + val + " to " + vars[0] + " if #instantiated == " + nbInst + ")";
	}
    }

    @Test
    public void testLaunch(){
	Solver s = new Solver();
	IntVar a = VF.bounded("a", 0, 2, s);
	IntVar b = VF.bounded("b", 0, 2, s);
	IntVar c = VF.bounded("c", 0, 2, s);
	IntVar[] vars = new IntVar[] { a, b, c };

	AffectIntVarActivatedHeuristic haa = new AffectIntVarActivatedHeuristic(a, 0, vars, 0);
	AffectIntVarActivatedHeuristic hab = new AffectIntVarActivatedHeuristic(b, 1, vars, 2);
	AffectIntVarActivatedHeuristic hac = new AffectIntVarActivatedHeuristic(c, 2, vars, 1);
	HeuristicsList<IntVar> hl = new HeuristicsList<>(s, hab, haa, hac);
	s.set(hl);
	SearchMonitorFactory.log(s, true, true);

	s.findSolution();
	Assert.assertEquals(a.getValue(), 0);
	Assert.assertEquals(b.getValue(), 1);
	Assert.assertEquals(c.getValue(), 2);
    }
}
