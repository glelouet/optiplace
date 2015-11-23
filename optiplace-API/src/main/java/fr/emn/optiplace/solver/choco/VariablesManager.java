package fr.emn.optiplace.solver.choco;

import java.util.Arrays;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.set.SCF;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Grants acces to the management of variables in a view.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class VariablesManager {

	private static final Logger logger = LoggerFactory.getLogger(VariablesManager.class);

	Solver solver;

	public Solver getSolver() {
		return solver;
	}

	protected void post(Constraint c) {
		solver.post(c);
	}

	public VariablesManager(Solver s) {
		this.solver = s;
	}

	public IntVar createIntegerConstant(int val) {
		return VariableFactory.fixed(val, getSolver());
	}

	public BoolVar createBoolVar(String name) {
		return VariableFactory.bool(name, getSolver());
	}

	public IntVar createEnumIntVar(String name, int... sortedValues) {
		return VariableFactory.enumerated(name, sortedValues, getSolver());
	}

	public IntVar createEnumIntVar(String name, int min, int max) {
		return VariableFactory.enumerated(name, min, max, getSolver());
	}

	/** creates an int variable whom range goes from min to max */
	public IntVar createBoundIntVar(String name, int min, int max) {
		return VariableFactory.bounded(name, min, max, getSolver());
	}

	public SetVar createEnumSetVar(String name, int... values) {
		return VariableFactory.set(name, values, getSolver());
	}

	/**
	 * 
	 * @param name
	 *          the name of the variable
	 * @param min
	 *          the minimum value of the set
	 * @param max
	 *          the max value of the set
	 * @return a new SetVar ranging from min to max
	 */
	public SetVar createRangeSetVar(String name, int min, int max) {
		return VariableFactory.set(name, min, max, getSolver());
	}

	public SetVar toSet(IntVar... vars) {
		Solver s = getSolver();
		if (vars == null || vars.length == 0) {
			return VF.set("empty set", new int[] {}, s);
		}
		int min = vars[0].getLB();
		int max = vars[0].getUB();
		for (int i = 1; i < vars.length; i++) {
			IntVar v = vars[i];
			min = Math.min(min, v.getLB());
			max = Math.max(max, v.getUB());
		}
		SetVar ret = VF.set("setof" + Arrays.asList(vars), min, max, s);
		s.post(SCF.int_values_union(vars, ret));
		return ret;
	}

  /**
   * creates an int variables whom ranges goes from minimum value to maximum
   * value
   */
	public IntVar createBoundIntVar(String name) {
    return createBoundIntVar(name, VF.MIN_INT_BOUND, VF.MAX_INT_BOUND);
  }

	////////////////////////////////////////////////
	// operations on variables
	///////////////////////////////////////////////

	/**
	 * @param left
	 * @param right
	 * @return a new variable constrained to ret=left+right
	 */
	public IntVar plus(IntVar left, IntVar right) {
		IntVar ret = createBoundIntVar("(" + left + ")+(" + right + ')', left.getLB() + right.getLB(),
				left.getUB() + right.getUB());
		getSolver().post(ICF.sum(new IntVar[] { left, right }, ret));
		return ret;
	}

	/**
	 * @param vars
	 * @return a new variable constrained to the sum of the elements of vars
	 */
	public IntVar sum(IntVar... vars) {
		if (vars == null || vars.length == 0) {
			return createIntegerConstant(0);
		}
		if (vars.length == 1) {
			return vars[0];
		}
		IntVar ret = createBoundIntVar("sum(" + Arrays.asList(vars) + ")");
		getSolver().post(ICF.sum(vars, ret));
		return ret;
	}

	public IntVar minus(IntVar X) {
		return VF.minus(X);
	}

	public IntVar plus(IntVar x, int y) {
		if (y == 0) {
			return x;
		}
		return VF.offset(x, y);
	}

	public IntVar mult(IntVar x, int multiplier) {
		return VF.scale(x, multiplier);
	}

	/**
	 * create a variable whom value depends on the boolean state of another
	 *
	 * @param x
	 *          the boolean value
	 * @param valFalse
	 *          the returning variable value if x ==false
	 * @param valTrue
	 *          the returning variable value if x==true
	 * @return a new variable (actually can be a view, reducing the number of
	 *         constraints to propagate)
	 */
	public IntVar bswitch(BoolVar x, int valFalse, int valTrue) {
		return linear(x, valTrue - valFalse, valFalse);
	}

	/**
	 * @param x
	 *          a variable
	 * @param a
	 *          the product
	 * @param b
	 *          the offset
	 * @return A variable set to value =a⋅x+b
	 */
	public IntVar linear(IntVar x, int a, int b) {
		if (a == 0) {
			return createIntegerConstant(b);
		}
		if (a == 1) {
			return plus(x, b);
		}
		if (a == -1) {
			return plus(VF.minus(x), b);
		}
		return plus(VF.scale(x, a), b);
	}

	/**
	 * @param left
	 * @param right
	 * @return a variable constrained to ret=left * right
	 */
	public IntVar mult(IntVar left, IntVar right) {
		if (left.isInstantiatedTo(0) || right.isInstantiatedTo(0)) {
			return createIntegerConstant(0);
		}
		if (left.isInstantiatedTo(1)) {
			return right;
		}
		if (right.isInstantiatedTo(1)) {
			return left;
		}
		IntVar ret = createBoundIntVar("(" + left.getName() + ")*(" + right.getName() + ")");
		mult(left, right, ret);
		return ret;
	}

	/** add a constraint, left*right==product */
	public void mult(IntVar left, IntVar right, IntVar product) {
		getSolver().post(ICF.times(left, right, product));
	}

	/**
	 * @param x
	 * @param y
	 * @return a variable constrained to ret * y = x
	 */
	public IntVar div(IntVar x, int y) {
		IntVar ret = createBoundIntVar("(" + x.getName() + ")/" + y);
		getSolver().post(ICF.times(ret, y, x));
		return ret;
	}

	/**
	 * make a variable constrained to the scalar product of the elements. The
	 * weights are multiplied by a common value to prevent granularity issues.
	 *
	 * @param pos
	 *          the pos in each dimension
	 * @param weigth
	 *          the weight of each dimensions
	 * @return the scalar product of the positions to the weights
	 */
	public IntVar scalar(IntVar[] pos, double[] weights) {
		assert pos.length == weights.length;
		double granularity = 1;
		for (double weight : weights) {
			granularity = Math.max(granularity, 1 / weight);
		}
		int[] mults = new int[weights.length];
		for (int i = 0; i < weights.length; i++) {
			mults[i] = (int) (weights[i] * granularity);
		}
		IntVar thescalar = scalar(pos, mults);
		return div(thescalar, (int) granularity);
	}

	public IntVar scalar(IntVar[] pos, int[] mults) {
		int min = 0, max = 0;
		StringBuilder sb = new StringBuilder("scalar[");
		for (int i = 0; i < pos.length; i++) {
			IntVar v = pos[i];
			int m = mults[i];
			if (m < 0) {
				min += v.getUB() * m;
				max += v.getLB() * m;
			} else {
				min += v.getLB() * m;
				max += v.getUB() * m;
			}
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(v.getName() + "⋅" + m);
		}
		IntVar ret = createBoundIntVar(sb.append("]").toString(), min, max);
		getSolver().post(ICF.scalar(pos, mults, ret));
		return ret;
	}

	/**
	 * creates a new var z constrained by z =(x==y)
	 *
	 * @param x
	 * @param y
	 * @return a new variable constrained to ret=1 if x and y are instancied to
	 *         the same value
	 */
	public BoolVar isSame(IntVar x, IntVar y) {
		return ICF.arithm(x, "=", y).reif();
	}

	/**
	 * creates a new var z constrained by z =(x==y)
	 *
	 * @param x
	 * @param y
	 * @return a new variable constrained to ret=1 if x and y are instancied to
	 *         different value
	 */
	public BoolVar isDifferent(IntVar x, IntVar y) {
		return ICF.arithm(x, "!=", y).reif();
	}

	/**
	 * @return a new variables constrained to ret == x?!=y
	 */
	public BoolVar isDifferent(IntVar x, int y) {
		return ICF.arithm(x, "!=", y).reif();
	}

	/** print an array of IntVar as {var0, var1, var2, var3} */
	public String foldSetNames(IntVar[] values) {
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
	 * @param values
	 * @return a variable constrained to the maximum value reached in values
	 */
	public IntVar max(IntVar... values) {
		if (values == null || values.length == 0) {
			logger.error("cannot make the maximum of an empty array of values");
		}
		if (values.length == 1) {
			return values[0];
		}
		int[] minmax = getMinMax(values);
		IntVar ret = createBoundIntVar("max(" + foldSetNames(values) + ")", minmax[0], minmax[1]);
		maxOfList(ret, values);
		return ret;
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
	 * get the min and max values of the inf and sup ranges of an array of IntVar
	 *
	 * @param array
	 *          the table of VarIntDomain
	 * @return [min(inf(array)), max(sup(array))]
	 */
	public int[] getMinMax(IntVar[] array) {
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (IntVar idv : array) {
			if (idv == null) {
				continue;
			}
			if (idv.getLB() < min) {
				min = idv.getLB();
			}
			if (idv.getUB() > max) {
				max = idv.getUB();
			}
		}
		return new int[] { min, max };
	}

	/**
	 * @param values
	 * @return a variable constrained to the min values reached in values
	 */
	public IntVar min(IntVar... values) {
		if (values == null || values.length == 0) {
			logger.error("cannot make the minimum of an empty array of values");
		}
		if (values.length == 1) {
			return values[0];
		}
		int[] minmax = getMinMax(values);
		IntVar ret = createBoundIntVar("min(" + foldSetNames(values) + ")", minmax[0], minmax[1]);
		minOfList(ret, values);

		return ret;
	}

	/**
	 * @param index
	 * @param array
	 * @return a new variable constrained by ret=array[index]
	 */
	public IntVar nth(IntVar index, IntVar[] array) {
		int[] minmax = getMinMax(array);
		IntVar ret = createBoundIntVar(foldSetNames(array), minmax[0], minmax[1]);
		nth(index, array, ret);
		return ret;
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

	/**
	 * get a boolean value of an int value
	 *
	 * @param x
	 *          the int value to booleanize
	 * @param name
	 *          the name of the variable to return, or null to let it create the
	 *          name
	 * @return a new variable constrained to 1 if x>0, 0 either way.
	 */
	public BoolVar boolenize(IntVar x, String name) {
		if (name == null) {
			name = x.getName() + ">0";
		}
		return ICF.arithm(x, ">", 0).reif();
	}

}
