package entropy.configuration.resources;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.solver.choco.ReconfigurationProblem;

/**
 * Use of a resource in a {@link ReconfigurationProblem}. The uses are not
 * constraints and need to be packed using the problem's packer.
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