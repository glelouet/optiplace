package entropy.view.access;

import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Grants acces to the management of variables in a view.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public interface VariablesManager {

	public IntDomainVar newIntVar(int val);

	public IntDomainVar newIntVar(String name);

	public IntDomainVar newIntVar(String name, int min, int max);

	public IntDomainVar newEnumVar(String name, int min, int max);

	public IntDomainVar newEnumVar(String name, int[] sortedValues);

}
