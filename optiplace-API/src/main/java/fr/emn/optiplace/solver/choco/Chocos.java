package fr.emn.optiplace.solver.choco;

import solver.constraints.integer.Element;
import solver.constraints.integer.ElementV;
import solver.constraints.integer.EuclideanDivisionXYZ;
import solver.constraints.integer.TimesXYZ;
import solver.constraints.integer.bool.BooleanFactory;
import solver.constraints.reified.ReifiedFactory;
import common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.SConstraint;
import solver.variables.IntVar;

/**
 * Utility class to ease the creation of some constraints on Choco.
 * 
 * @author Fabien Hermenier
 */
public final class Chocos {

	private Chocos() {
	}

	/**
	 * Make and post an implies constraint: c1 -> c2. The constraint is
	 * translated into (or(not(c1),c2))
	 * 
	 * @param s
	 *            the solver
	 * @param c1
	 *            the first constraint
	 * @param c2
	 *            the second constraint
	 */
	public static void postImplies(Solver s, SConstraint<IntVar> c1,
			SConstraint<IntVar> c2) {
		IntVar bC1 = s.createBooleanVar("bC1");
		s.post(ReifiedFactory.builder(bC1, c1, s));

		IntVar bC2 = s.createBooleanVar("bC2");
		s.post(ReifiedFactory.builder(bC2, c2, s));

		SConstraint<IntVar> cNotC1 = BooleanFactory.not(bC1);
		IntVar bNotC1 = s.createBooleanVar("!c1");
		s.post(ReifiedFactory.builder(bNotC1, cNotC1, s));

		s.post(BooleanFactory.or(s.getEnvironment(), bNotC1, bC2));
	}

	/**
	 * Make and post an implies constraint where the first operand is a boolean:
	 * b1 -> c2. The constraint is translated into (or(not(b1,c2))
	 * 
	 * @param s
	 *            the solver
	 * @param b1
	 *            the first constraint as boolean
	 * @param c2
	 *            the second constraint
	 */
	public static void postImplies(Solver s, IntVar b1,
			SConstraint<IntVar> c2) {

		IntVar bC2 = s.createBooleanVar("bC2");
		s.post(ReifiedFactory.builder(bC2, c2, s));

		IntVar notB1 = s.createBooleanVar("!b1");
		s.post(s.neq(b1, notB1));

		s.post(BooleanFactory.or(s.getEnvironment(), notB1, bC2));
	}

	/**
	 * Make and post a postifOnlyIf constraint that state and(or(b1, non c2),
	 * or(non b1, c2))
	 * 
	 * @param s
	 *            the solver
	 * @param b1
	 *            the first constraint
	 * @param c2
	 *            the second constraint
	 */
	public static void postIfOnlyIf(Solver s, IntVar b1,
			SConstraint<IntVar> c2) {
		IntVar notBC1 = s.createBooleanVar("!(" + b1.pretty() + ")");
		s.post(s.neq(b1, notBC1));

		IntVar bC2 = s.createBooleanVar("boolean(" + c2.pretty() + ")");
		s.post(ReifiedFactory.builder(bC2, c2, s));

		IntVar notBC2 = s.createBooleanVar("!(" + c2.pretty() + ")");
		s.post(s.neq(notBC2, bC2));

		IntVar or1 = s.createBooleanVar("or1");
		s.post(ReifiedFactory.builder(or1,
				BooleanFactory.or(s.getEnvironment(), b1, notBC2), s));

		IntVar or2 = s.createBooleanVar("or2");
		s.post(ReifiedFactory.builder(or2,
				BooleanFactory.or(s.getEnvironment(), notBC1, bC2), s));

		s.post(BooleanFactory.and(or1, or2));
	}

	/** add a constraint such as array[index]=value */
	public static void nth(Solver s, IntVar index, IntVar[] array,
			IntVar var) {
		s.post(new ElementV(ArrayUtils.append(array, new IntVar[]{index,
				var}), 0, s.getEnvironment()));
	}

	/** add a constraint such as array[index]=value */
	public static void nth(Solver s, IntVar index, int[] array,
			IntVar var) {
		s.post(new Element(index, array, var));
	}

	public static IntVar nth(Solver s, IntVar index,
			IntVar[] array) {
		int[] minmax = getMinMax(array);
		IntVar ret = s.createBoundIntVar(foldSetNames(array), minmax[0],
				minmax[1]);
		Chocos.nth(s, index, array, ret);
		return ret;
	}

	/** add a constraint, left*right==product */
	public static void mult(Solver s, IntVar left, IntVar right,
			IntVar product) {
		s.post(new TimesXYZ(left, right, product));
	}

	public static IntVar mult(Solver s, IntVar left,
			IntVar right) {
		int min = left.getInf() * right.getInf(), max = min;
		for (int prod : new int[]{left.getInf() * right.getSup(),
				left.getSup() * right.getSup(), left.getInf() * right.getSup()}) {
			if (prod < min) {
				min = prod;
			}
			if (prod > max) {
				max = prod;
			}
		}
		IntVar ret = s.createBoundIntVar("(" + left.getName() + ")*("
				+ right.getName() + ")", min, max);
		mult(s, left, right, ret);
		return ret;
	}

	public static IntVar mult(Solver s, IntVar left, int right) {
		int min = left.getInf() * right, max = min;
		int prod = left.getSup() * right;
		if (prod < min) {
			min = prod;
		}
		if (prod > max) {
			max = prod;
		}
		IntVar ret = s.createBoundIntVar("(" + left.getName() + ")*"
				+ right, min, max);
		mult(s, left, s.createIntegerConstant("" + right, right), ret);
		return ret;
	}

	public static IntVar div(Solver s, IntVar var, int i) {
		int a = var.getInf() / i;
		int b = var.getSup() / i;
		int min = Math.min(a, b);
		int max = Math.max(a, b);
		IntVar ret = s.createBoundIntVar("(" + var.getName() + ")/" + i,
				min, max);
		s.post(new EuclideanDivisionXYZ(var,
				s.createIntegerConstant("" + i, i), ret));
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
	 * get the min and max values of the inf and sup ranges of an array of
	 * IntVar
	 * 
	 * @param array
	 *            the table of VarIntDomain
	 * @return [min(inf(array)), max(sup(array))]
	 */
	public static int[] getMinMax(IntVar[] array) {
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (IntVar idv : array) {
			if (idv.getInf() < min) {
				min = idv.getInf();
			}
			if (idv.getSup() > max) {
				max = idv.getSup();
			}
		}
		return new int[]{min, max};
	}
}
