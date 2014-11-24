/**
 *
 */
package fr.emn.optiplace.solver;

import org.testng.Assert;
import org.testng.annotations.Test;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.selectors.IntValueSelector;
import solver.search.strategy.selectors.VariableSelector;
import solver.search.strategy.strategy.IntStrategy;
import solver.variables.IntVar;
import solver.variables.VF;
import fr.emn.optiplace.solver.heuristics.EmbededActivatedHeuristic;

/**
 * <p>
 * test when an heuristicList leads to a error on next heuristics . It should
 * then backtract, and we test if the backtrack keeps the listHeuristic
 * </p>
 * <p>
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
@SuppressWarnings("serial")
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

	@Override
	public Decision<?> getDecision() {
	    if (toAdd != null) {
		try {
		    vars[0].getSolver().post(toAdd);
		    // vars[0].getSolver().propagate();
		    // leads to fail, as well as
		    // vars[0].getSolver().postTemp(toAdd);
		} catch (Exception e) {
		    e.printStackTrace(System.err);
		    return null;
		}
	    }
	    return super.getDecision();
	}

	public IterateOverVar(IntVar var) {
	    super(new IntVar[] { var }, new VarSelUnique(var), new ValSelLB());
	}

	/**
	 * set a constraint to add on getDecision() and return this
	 *
	 * @param c
	 *            the constraint to add on first Variable selection.
	 * @return this
	 */
	public IterateOverVar withConstraint(Constraint c) {
	    this.toAdd = c;
	    return this;
	}

    }

    @Test
    public void testpostTemp() {
	Solver s = new Solver();

	IntVar a = VF.bool("a", s);
	IterateOverVar ia = new IterateOverVar(a);

	IntVar b = VF.bool("b", s);
	IterateOverVar ib = new IterateOverVar(b).withConstraint(ICF.arithm(a, "!=", 0));

	IntVar c = VF.bool("c", s);
	IterateOverVar ic = new IterateOverVar(c);
	s.set(IntStrategyFactory.sequencer(ia, ib, ic));
	// SearchMonitorFactory.log(s, true, true);
	s.findSolution();
    }


    /**
     * we test the result when an ActivatedHeuristic makes a Decision which
     * leads to a contradiction.<br />
     * The internal ActivatedHeuristic makes to selections : the first one leads
     * to a contradiction, the second to a solution.<br />
     * To "lead" to a bad solution, a second heuristic will make the search fail
     * on its first call.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test(dependsOnMethods = "testpostTemp")
    public void testFirstOptimizeFalse() {
	Solver s = new Solver();

	IntVar a = VF.bool("a", s);
	IterateOverVar ia = new IterateOverVar(a);

	IntVar b = VF.bool("b", s);
	IterateOverVar ib = new IterateOverVar(b);

	IntVar c = VF.bool("c", s);
	IterateOverVar ic = new IterateOverVar(c).withConstraint(ICF.arithm(a, "!=", b));

	IntVar d = VF.bool("d", s);
	IterateOverVar id = new IterateOverVar(d);

	HeuristicsList<IntVar> hl = new HeuristicsList(s, new EmbededActivatedHeuristic<IntVar>(ia),
		new EmbededActivatedHeuristic<IntVar>(ib));
	s.set(IntStrategyFactory.sequencer(hl, ic, id));
	// SearchMonitorFactory.log(s, true, true);
	s.findSolution();
	Assert.assertEquals(a.getValue(), 0);
	Assert.assertEquals(b.getValue(), 1);
	Assert.assertEquals(c.getValue(), 0);
	Assert.assertEquals(d.getValue(), 0);
    }
}
