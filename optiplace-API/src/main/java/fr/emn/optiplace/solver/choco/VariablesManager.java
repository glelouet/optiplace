package fr.emn.optiplace.solver.choco;

import java.util.Arrays;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.set.SCF;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Grants acces to the management of variables in a view.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class VariablesManager {

	Solver solver;

	public Solver getSolver() {
		return solver;
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

}
