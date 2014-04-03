package fr.emn.optiplace.view.access;

import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Grants acces to the management of variables in a view.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public interface VariablesManager {

	/** create a int constant. */
	public IntDomainVar createIntegerConstant(int val);

	/**
	 * creates an int variables whom ranges goes from minimum value to maximum
	 * value
	 */
	public IntDomainVar createBoundIntVar(String name);

	/** creates an int variable whom range goes from min to max */
	public IntDomainVar createBoundIntVar(String name, int min, int max);

	/** creates a new enumerated int variables, range goes from min to max */
	public IntDomainVar createEnumIntVar(String name, int min, int max);

	/**
	 * creates a new enumerated variable with an array of possible values,
	 * sorted by the values
	 */
	public IntDomainVar createEnumIntVar(String name, int[] sortedValues);

}
