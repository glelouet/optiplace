/**
 * 
 */
package fr.emn.optiplace.view.linearpower;

import java.util.Map;

import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public interface MigrationCostMaker {

	/**
	 * @param vm
	 *            the vm to migrate
	 * @param resources
	 *            the resources to know what vm consumes
	 * @return
	 */
	public int migrationCost(VM vm,
			Map<String, ResourceSpecification> resources);

	/**
	 * compute the energy cost to migrate the vms
	 * 
	 * @param vms
	 * @param resources
	 * @return a new array of each vm's migration cost
	 */
	public default int[] migrationCosts(VM[] vms, Map<String, ResourceSpecification> resources) {
		int[] ret = new int[vms.length];
		for (int i = 0; i < vms.length; i++)
			ret[i] = migrationCost(vms[i], resources);
		return ret;
	}

}