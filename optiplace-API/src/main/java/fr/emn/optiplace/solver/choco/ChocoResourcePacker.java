package fr.emn.optiplace.solver.choco;

import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
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
	public SConstraint<IntDomainVar>[] pack(IEnvironment environment,
			IntDomainVar[] binAssign, ResourceUse... resourceUse);

}
