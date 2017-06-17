
package fr.emn.optiplace.solver.choco;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class ConstraintHelper {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConstraintHelper.class);

	final Model model;

	public ConstraintHelper(Model s) {
		model = s;
	}

	/**
	 * post equality of variables
	 *
	 * @param vars
	 *          the variables to post equality on
	 */
	public void equality(IntVar... vars) {
		if (vars == null || vars.length < 2) {
			return;
		}
		for (int i = 1; i < vars.length; i++) {
			model.post(model.arithm(vars[i - 1], "=", vars[i]));
		}
	}

	/**
	 * post a constraint IF a series of bool var are ALL true<br />
	 * basically a "and(condition) implies result"
	 *
	 * @param result
	 *          the constraint we want posted
	 * @param conditions
	 *          the conditions for the constraint to apply
	 */
	public void postIf(Constraint result, BoolVar... conditions) {
		if (result == null || conditions == null || conditions.length == 0) {
			return;
		}
		if (conditions.length == 1) {
			model.ifThen(conditions[0], result);
			return;
		}
		// and(vars) implies result.reif :
		// result.reif or !(and(vars))
		// result.reif | !vars0 | !vars1 ...
		BoolVar[] orVars = new BoolVar[conditions.length + 1];
		for (int i = 0; i < conditions.length; i++) {
			orVars[i] = conditions[i].not();
		}
		orVars[conditions.length] = result.reify();
		model.post(model.or(orVars));
	}

	/** add a constraint, such as max = max(values) */
	public void maxOfList(IntVar max, IntVar... values) {
		model.post(model.max(max, values));
	}

	/** add a constraint, such as min = min(values) */
	public void minOfList(IntVar min, IntVar... values) {
		model.post(model.min(min, values));
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
	public void element(IntVar index, IntVar[] array, IntVar var) {
		model.post(model.element(var, array, index, 0));
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
	public void element(IntVar index, int[] array, IntVar var) {
		model.post(model.element(var, array, index));
	}

	/** add a constraint, left*right==product */
	public void mult(IntVar left, IntVar right, IntVar product) {
		model.post(model.times(left, right, product));
	}
}
