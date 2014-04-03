package fr.emn.optiplace.configuration.resources;

import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * A {@link ResourceSpecification} + a {@link ReconfigurationProblem} = a
 * {@link ResourceUse} . <br />
 * This class represents the effective use in a Choco problem, with IntDomainVar
 * corresponding to the Vms static uses and the nodes dynamic uses. The uses are
 * not constrained and need to be packed using the problem's packer.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class ResourceUse {

	/**
	 * effective constant resource usage; ie the item i uses use[i] on the
	 * resource
	 */
	IntDomainVar[] itemsConsumptions = null;

	/** effective resource use of a bin. */
	IntDomainVar[] binsUse = null;

	public ResourceUse() {
	}

	public ResourceUse(IntDomainVar[] vmsUse, IntDomainVar[] binsUse) {
		itemsConsumptions = vmsUse;
		this.binsUse = binsUse;
	}

	/** @return the constant consumption of the vms */
	public IntDomainVar[] getVMsUses() {
		return itemsConsumptions;
	}

	/** @return the uses of the nodes */
	public IntDomainVar[] getNodesUse() {
		return binsUse;
	}
}