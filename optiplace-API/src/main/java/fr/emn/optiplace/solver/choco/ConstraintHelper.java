
package fr.emn.optiplace.solver.choco;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;


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

	/**
	 * post equality of variables
	 * 
	 * @param vars
	 *          the variables to post equality on
	 */
	public void equality(IntVar... vars) {
		if (vars == null || vars.length < 2)
			return;
		for (int i = 1; i < vars.length; i++) {
			solver.post(ICF.arithm(vars[i - 1], "=", vars[i]));
		}
	}

	/**
	 * post a constraint IF a series of bool var are ALL true
	 * 
	 * @param result
	 *          the constraint we want posted
	 * @param vars
	 *          the conditions for the constraint to apply
	 */
	public void onCondition(Constraint result, BoolVar... vars) {
		if (result == null || vars == null || vars.length == 0)
			return;
		if (vars.length == 1)
			LCF.ifThen(vars[0], result);
		// and(vars) implies result.reif :
		// result.reif or !(and(vars))
		// result.reif | !vars0 | !vars1 ...
		BoolVar[] orVars = new BoolVar[vars.length + 1];
		for (int i = 0; i < vars.length; i++)
			orVars[i] = vars[i].not();
		orVars[vars.length] = result.reif();
		LCF.or(orVars);
	}

	/** add a constraint, such as max = max(values) */
	public void maxOfList(IntVar max, IntVar... values) {
		getSolver().post(ICF.maximum(max, values));
	}

	/** add a constraint, such as min = min(values) */
	public void minOfList(IntVar min, IntVar... values) {
		getSolver().post(ICF.minimum(min, values));
	}

	/**
	 * ensures var belongs to array[index], ie var.inf>=array[index].min and
	 * var.sup<=array[index].inf
	 *
	 * @param index
	 *          variable to index
	 * @param array
	 *          array of variables
	 * @param var
	 *          variable
	 */
	public void nth(IntVar index, IntVar[] array, IntVar var) {
		getSolver().post(ICF.element(var, array, index, 0));
	}

	/**
	 * ensures var belongs to array[index], ie var.inf>=array[index].min and
	 * var.sup<=array[index].inf
	 *
	 * @param index
	 *          variable to index
	 * @param array
	 *          array of int
	 * @param var
	 *          variable
	 */
	public void nth(IntVar index, int[] array, IntVar var) {
		getSolver().post(ICF.element(var, array, index));
	}

	/** add a constraint, left*right==product */
	public void mult(IntVar left, IntVar right, IntVar product) {
		getSolver().post(ICF.times(left, right, product));
	}
}
