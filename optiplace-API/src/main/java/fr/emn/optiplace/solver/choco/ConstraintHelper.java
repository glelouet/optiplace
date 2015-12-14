
package fr.emn.optiplace.solver.choco;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class ConstraintHelper {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConstraintHelper.class);
	Solver solver;

	public Solver getSolver() {
		return solver;
	}

	protected void post(Constraint c) {
		solver.post(c);
	}

	public ConstraintHelper(Solver s) {
		solver = s;
	}

	public void matrixSet(SetVar[][] matrix, IntVar idxRow, IntVar idxCol, SetVar ret) {
		// TODO
		throw new UnsupportedOperationException();
	}
}
