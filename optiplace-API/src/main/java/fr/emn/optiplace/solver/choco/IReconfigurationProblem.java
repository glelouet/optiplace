/* Copyright (c) 2010 Ecole des Mines de Nantes. This file is part of Entropy.
 * Entropy is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Entropy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with Entropy. If not, see
 * <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.solver.choco;

import java.util.Arrays;
import java.util.stream.Stream;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.ProblemStatistics;
import fr.emn.optiplace.solver.SolvingStatistics;
import fr.emn.optiplace.view.access.CoreView;


/**
 * Specification of a reconfiguration problem. A bridge between the VMs, the
 * Nodes, and a Choco problem
 *
 * @author Fabien Hermenier
 * @author Guillaume Le Louët
 */
public interface IReconfigurationProblem extends CoreView {

	public static final Logger logger = LoggerFactory.getLogger(IReconfigurationProblem.class);

	public Solver getSolver();

	/** shortcut for getSolver().post(c) */
	default void post(Constraint c) {
		getSolver().post(c);
	}

	/**
	 * Get statistics about the solving process
	 *
	 * @return some statistics
	 */
	SolvingStatistics getSolvingStatistics();

	/***** Variables linked to Nodes and VMs *************************/

	/**
	 * Get the source configuration, that is, the original configuration to
	 * optimize.
	 *
	 * @return a configuration
	 */
	Configuration getSourceConfiguration();

	/******************** variables linked to resources ***********************/

	default IntVar getNodeUse(String resource, Node n) {
		return getResourcesHandlers().get(resource).getNodeLoads()[b().node(n)];
	}

	default int getNodeCap(String resource, Node n) {
		return getResourcesHandlers().get(resource).getCapacities()[b().node(n)];
	}

	default IntVar getUsedCPU(Node n) {
		return getNodeUse("CPU", n);
	}

	default IntVar getUsedMem(Node n) {
		return getNodeUse("MEM", n);
	}

	public IntVar getHostUse(String resource, int vmIndex);

	public IntVar getHostCapa(String resource, int vmIndex);

	/**
	 * add an {@link ResourceHandler} to manage the consumption variables of a
	 * resource
	 *
	 * @param handler
	 *          the handler, already containing
	 */
	void addResourceHandler(ResourceHandler handler);

	default void addResource(ResourceSpecification rs) {
		addResourceHandler(new ResourceHandler(rs));
	}

	/********************* Operations on variables *****************/

	/**
	 * @param left
	 * @param right
	 * @return a new variable constrained to ret=left+right
	 */
	default IntVar plus(IntVar left, IntVar right) {
		IntVar ret = v().createBoundIntVar("(" + left + ")+(" + right + ')', left.getLB() + right.getLB(),
		    left.getUB() + right.getUB());
		getSolver().post(ICF.sum(new IntVar[] {
		    left, right
		}, ret));
		return ret;
	}

	/**
	 * @param vars
	 * @return a new variable constrained to the sum of the elements of vars
	 */
	default IntVar sum(IntVar... vars) {
		if (vars == null || vars.length == 0) {
			return v().createIntegerConstant(0);
		}
		if (vars.length == 1) {
			return vars[0];
		}
		IntVar ret = v().createBoundIntVar("sum(" + Arrays.asList(vars) + ")");
		getSolver().post(ICF.sum(vars, ret));
		return ret;
	}

	default IntVar minus(IntVar X) {
		return VF.minus(X);
	}

	default IntVar plus(IntVar x, int y) {
		if (y == 0) {
			return x;
		}
		return VF.offset(x, y);
	}

	default IntVar mult(IntVar x, int multiplier) {
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
	default IntVar bswitch(BoolVar x, int valFalse, int valTrue) {
		return linear(x, valTrue - valFalse, valFalse);
	}

	/**
	 * 
	 * @Override public int vm(VM vm) { int v = revVMs.get(vm); if (v == 0 &&
	 *           !vms[0].equals(vm)) { return -1; } return v; }
	 * 
	 *           /** converts an array of vms to an array of index of those vms in
	 *           the problem.
	 *
	 * @param vms
	 *          the vms to convert, all of them must belong to the problem
	 * @return
	 * @param x
	 *          a variable
	 * @param a
	 *          the product
	 * @param b
	 *          the offset
	 * @return A variable set to value =a⋅x+b
	 */
	default IntVar aff(IntVar x, int a, int b) {
		return linear(x, a, b);
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
	default IntVar linear(IntVar x, int a, int b) {
		if (a == 0) {
			return v().createIntegerConstant(b);
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
	default IntVar mult(IntVar left, IntVar right) {
		if (left.isInstantiatedTo(0) || right.isInstantiatedTo(0)) {
			return v().createIntegerConstant(0);
		}
		if (left.isInstantiatedTo(1)) {
			return right;
		}
		if (right.isInstantiatedTo(1)) {
			return left;
		}
		IntVar ret = v().createBoundIntVar("(" + left.getName() + ")*(" + right.getName() + ")");
		mult(left, right, ret);
		return ret;
	}

	/** add a constraint, left*right==product */
	default void mult(IntVar left, IntVar right, IntVar product) {
		getSolver().post(ICF.times(left, right, product));
	}

	/**
	 * @param x
	 * @param y
	 * @return a variable constrained to ret * y = x
	 */
	default IntVar div(IntVar x, int y) {
		IntVar ret = v().createBoundIntVar("(" + x.getName() + ")/" + y);
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
	default IntVar scalar(IntVar[] pos, double[] weights) {
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

	default IntVar scalar(IntVar[] pos, int[] mults) {
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
		IntVar ret = v().createBoundIntVar(sb.append("]").toString(), min, max);
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
	default BoolVar isSame(IntVar x, IntVar y) {
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
	default BoolVar isDifferent(IntVar x, IntVar y) {
		return ICF.arithm(x, "!=", y).reif();
	}

	/**
	 * @return a new variables constrained to ret == x?!=y
	 */
	default BoolVar isDifferent(IntVar x, int y) {
		return ICF.arithm(x, "!=", y).reif();
	}

	/** print an array of IntVar as {var0, var1, var2, var3} */
	static String foldSetNames(IntVar[] values) {
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
	default IntVar max(IntVar... values) {
		if (values == null || values.length == 0) {
			logger.error("cannot make the maximum of an empty array of values");
		}
		if (values.length == 1) {
			return values[0];
		}
		int[] minmax = getMinMax(values);
		IntVar ret = v().createBoundIntVar("max(" + foldSetNames(values) + ")", minmax[0], minmax[1]);
		maxOfList(ret, values);
		return ret;
	}

	/** add a constraint, such as max = max(values) */
	default void maxOfList(IntVar max, IntVar... values) {
		getSolver().post(ICF.maximum(max, values));
	}

	/** add a constraint, such as min = min(values) */
	default void minOfList(IntVar min, IntVar... values) {
		getSolver().post(ICF.minimum(min, values));
	}

	/**
	 * get the min and max values of the inf and sup ranges of an array of IntVar
	 *
	 * @param array
	 *          the table of VarIntDomain
	 * @return [min(inf(array)), max(sup(array))]
	 */
	static int[] getMinMax(IntVar[] array) {
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
		return new int[] {
		    min, max
		};
	}

	/**
	 * @param values
	 * @return a variable constrained to the min values reached in values
	 */
	default IntVar min(IntVar... values) {
		if (values == null || values.length == 0) {
			logger.error("cannot make the minimum of an empty array of values");
		}
		if (values.length == 1) {
			return values[0];
		}
		int[] minmax = getMinMax(values);
		IntVar ret = v().createBoundIntVar("min(" + foldSetNames(values) + ")", minmax[0], minmax[1]);
		minOfList(ret, values);

		return ret;
	}

	/**
	 * @param index
	 * @param array
	 * @return a new variable constrained by ret=array[index]
	 */
	default IntVar nth(IntVar index, IntVar[] array) {
		int[] minmax = getMinMax(array);
		IntVar ret = v().createBoundIntVar(foldSetNames(array), minmax[0], minmax[1]);
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
	default void nth(IntVar index, IntVar[] array, IntVar var) {
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
	default void nth(IntVar index, int[] array, IntVar var) {
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
	default BoolVar boolenize(IntVar x, String name) {
		if (name == null) {
			name = x.getName() + ">0";
		}
		return ICF.arithm(x, ">", 0).reif();
	}

	/**
	 * affect a variable according to the state of a VM.
	 * <p>
	 * if one of the potential IntVar (onX) is null, then the corresponding state
	 * is removed from the IntVar state of the vm.This reduces the need to check
	 * for VM state or removing the values manually
	 * </p>
	 *
	 * @param v
	 *          the VM to follow the state
	 * @param var
	 *          the variable to assign
	 * @param onRunning
	 *          the value of var if VM is running
	 * @param onExtern
	 *          the value of var if VM is externed
	 * @param onWaiting
	 *          the value of var ir VM is waiting
	 */
	default void switchState(VM v, IntVar var, IntVar onRunning, IntVar onExtern, IntVar onWaiting) {
		IntVar[] vars = {
		    onRunning, onExtern, onWaiting
		};
		IntVar state = getState(v);
		int nbNonNull = (int) Stream.of(vars).filter(a -> a != null).count();
		switch (nbNonNull) {
			case 0:
				throw new UnsupportedOperationException("can't assign an IntVar( " + var + ")to null value");
			case 1:
				post(ICF.arithm(var, "=", onRunning != null ? onRunning : onExtern != null ? onExtern : onWaiting));
				try {
					state.instantiateTo(onRunning != null ? VM_RUNNING : onExtern != null ? VM_EXTERNED : VM_WAITING, Cause.Null);
				}
				catch (ContradictionException e) {
					throw new UnsupportedOperationException(e);
				}
			break;
			case 2:
				try {
					if (onRunning == null) {
						state.removeValue(VM_RUNNING, Cause.Null);
					}
					if (onExtern == null) {
						state.removeValue(VM_EXTERNED, Cause.Null);
					}
					if (onWaiting == null) {
						state.removeValue(VM_WAITING, Cause.Null);
					}
				}
				catch (ContradictionException e) {
					throw new UnsupportedOperationException(e);
				}
			break;
			case 3:
				post(ICF.arithm(var, "=", nth(getState(v), vars)));
			break;
			default:
				throw new UnsupportedOperationException(
				    "unsupported nuber of non null variables " + nbNonNull + " in " + Arrays.asList(vars));
		}
	}

	public ProblemStatistics getStatistics();
}
