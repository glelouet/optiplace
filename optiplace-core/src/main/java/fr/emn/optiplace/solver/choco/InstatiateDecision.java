/**
 *
 */
package fr.emn.optiplace.solver.choco;

import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.variables.IntVar;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class InstatiateDecision extends Decision<IntVar> {

    private static final long serialVersionUID = 1;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InstatiateDecision.class);

    final IntVar[] vars;
    final int[] vals;

    public InstatiateDecision(IntVar[] vars, int[] vals) {
	this.vals = vals;
	this.vars = vars;
	if (vars.length != vals.length) {
	    throw new UnsupportedOperationException("arrays have different length");
	}

    }

    @Override
    public void apply() throws ContradictionException {
	for (IntVar var2 : vars) {

	}
    }

    @Override
    public Object getDecisionValue() {
	// TODO Auto-generated method stub
	throw new UnsupportedOperationException();
    }

    @Override
    public void free() {
	// do nothing, no free
    }
}
