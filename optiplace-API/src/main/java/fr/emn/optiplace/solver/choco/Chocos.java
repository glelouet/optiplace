package fr.emn.optiplace.solver.choco;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.constraints.ternary.Times;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Utility class to ease the creation of some constraints on Choco.
 *
 * @deprecated most functions are present in choco factories. Also now the
 * constraints embed their solver, the signatures should be updated.
 * @author Fabien Hermenier
 */
// TODO remove this class
@Deprecated
public final class Chocos {

	/**
	 * Make and post an implies constraint: c1 -> c2. The constraint is translated
	 * into (or(not(c1),c2))
	 *
	 * @param s
	 * the solver
	 * @param c1
	 * the first constraint
	 * @param c2
	 * the second constraint
	 */
	public static void postImplies(Solver s, Constraint c1, Constraint c2) {
	LogicalConstraintFactory.ifThen(c1, c2);
	}

	/**
	 * Make and post an implies constraint where the first operand is a boolean:
	 * b1 -> c2. The constraint is translated into (or(not(b1,c2))
	 *
	 * @param s
	 * the solver
	 * @param b1
	 * the first constraint as boolean
	 * @param c2
	 * the second constraint
	 */
	public static void postImplies(Solver s, IntVar b1, Constraint c2) {
		LogicalConstraintFactory.ifThen(boolenize(b1), c2);
	}

	public static BoolVar boolenize(IntVar v) {
		BoolVar ret = VariableFactory.bool("b_" + v.getName(), v.getSolver());
		v.getSolver().post(IntConstraintFactory.arithm(v, "=", ret));
		return ret;
	}

	/**
	 * Make and post a postifOnlyIf constraint that state and(or(b1, non c2),
	 * or(non b1, c2))
	 *
	 * @param s
	 * the solver
	 * @param b1
	 * the first constraint
	 * @param c2
	 * the second constraint
	 */
	public static void postIfOnlyIf(Solver s, IntVar b1, Constraint c2) {
		s.post(IntConstraintFactory.arithm(b1, "=", c2.reif()));
	}

	/** add a constraint such as array[index]=value */
	public static void nth(Solver s, IntVar index, IntVar[] array, IntVar var) {
		s.post(IntConstraintFactory.element(var, array, index, 0));
	}

	/** add a constraint such as array[index]=value */
	public static void nth(Solver s, IntVar index, int[] array, IntVar var) {
		s.post(IntConstraintFactory.element(var, array, index));
	}

	public static IntVar nth(Solver s, IntVar index, IntVar[] array) {
		int[] minmax = getMinMax(array);
		IntVar ret = VariableFactory.bounded(foldSetNames(array), minmax[0],
				minmax[1], s);
		Chocos.nth(s, index, array, ret);
		return ret;
	}

	/** add a constraint, left*right==product */
	public static void mult(Solver s, IntVar left, IntVar right, IntVar product) {
		s.post(new Times(left, right, product));
	}

	public static IntVar mult(Solver s, IntVar left, IntVar right) {
		int min = left.getLB() * right.getLB(), max = min;
		for (int prod : new int[] { left.getLB() * right.getUB(),
				left.getUB() * right.getUB(), left.getLB() * right.getUB() }) {
			if (prod < min) {
				min = prod;
			}
			if (prod > max) {
				max = prod;
			}
		}
		IntVar ret = VariableFactory.bounded(
				"(" + left.getName() + ")*(" + right.getName() + ")", min, max, s);
		mult(s, left, right, ret);
		return ret;
	}

	public static IntVar mult(Solver s, IntVar left, int right) {
		int min = left.getLB() * right, max = min;
		int prod = left.getUB() * right;
		if (prod < min) {
			min = prod;
		}
		if (prod > max) {
			max = prod;
		}
		IntVar ret = VariableFactory.bounded("(" + left.getName() + ")*" + right,
				min, max, s);
		mult(s, left, VariableFactory.fixed(right, s), ret);
		return ret;
	}

	public static IntVar div(Solver s, IntVar var, int i) {
		int a = var.getLB() / i;
		int b = var.getLB() / i;
		int min = Math.min(a, b);
		int max = Math.max(a, b);
		IntVar ret = VariableFactory.bounded("(" + var.getName() + ")/" + i, min,
				max, s);
		IntConstraintFactory.eucl_div(var, VariableFactory.fixed(i, s), ret);
		return ret;
	}

	/** print an array of IntVar as {var0, var1, var2, var3} */
	public static String foldSetNames(IntVar[] values) {
		StringBuilder sb = null;
		for (IntVar idv : values) {
			if (sb == null) {
				sb = new StringBuilder("{");
			} else {
				sb.append(", ");
			}
			sb.append(idv.getName());
		}
		return sb == null ? "{}" : sb.append("}").toString();
	}

	/**
	 * get the min and max values of the inf and sup ranges of an array of IntVar
	 *
	 * @param array
	 * the table of VarIntDomain
	 * @return [min(inf(array)), max(sup(array))]
	 */
	public static int[] getMinMax(IntVar[] array) {
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (IntVar idv : array) {
			if (idv.getLB() < min) {
				min = idv.getLB();
			}
			if (idv.getUB() > max) {
				max = idv.getUB();
			}
		}
		return new int[] { min, max };
	}
}
