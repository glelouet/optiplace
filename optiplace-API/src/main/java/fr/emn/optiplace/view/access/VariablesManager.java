package fr.emn.optiplace.view.access;

import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.SetVar;

/**
 * Grants acces to the management of variables in a view.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public interface VariablesManager {

	/** create a int constant. */
	public IntVar createIntegerConstant(int val);

	public BoolVar createBoolVar(String name);

	/**
	 * creates an int variables whom ranges goes from minimum value to maximum
	 * value
	 */
	default IntVar createBoundIntVar(String name) {
		return createBoundIntVar(name, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/** creates an int variable whom range goes from min to max */
	public IntVar createBoundIntVar(String name, int min, int max);


	/** creates a new enumerated int variables, range goes from min to max */
	public IntVar createEnumIntVar(String name, int min, int max);

	/**
	 * creates a new enumerated variable with an array of possible values,
	 * sorted by the values
	 */
	public IntVar createEnumIntVar(String name, int... sortedValues);


	public SetVar createEnumSetVar(String name, int... values);

	public SetVar createRangeSetVar(String name, int min, int max);

}
