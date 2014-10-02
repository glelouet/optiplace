package fr.emn.optiplace.solver.choco;

import memory.IEnvironment;
import solver.constraints.IntConstraint;
import solver.variables.IntVar;
import fr.emn.optiplace.configuration.resources.ResourceUse;

public interface ChocoResourcePacker {

	/**
	 * create a constraint to add to the solver
	 *
	 * @param environment
	 *            the solver's environment
	 * @param binAssign
	 *            the affectation of the elements in their bins
	 * @param resourceUse
	 *            specifications of the usage of the elements and capacities of
	 *            the bins
	 * @return
	 */
	public IntConstraint<IntVar>[] pack(IEnvironment environment,
			IntVar[] binAssign, ResourceUse... resourceUse);

}
