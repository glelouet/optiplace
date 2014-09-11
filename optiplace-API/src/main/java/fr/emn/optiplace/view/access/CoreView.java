/**
 *
 */
package fr.emn.optiplace.view.access;

import java.util.HashMap;

import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.configuration.resources.ResourceHandler;

/**
 * View of the core problem. A core problem contains nodes, vms, and the hosting
 * of the vms on the nodes. It also has resource handlers specifying the
 * capacities of the hosts and the consumptions of the vms.<br />
 * It does NOT extend the view because it is used as a base view by the other
 * modules.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public interface CoreView {

	/**
	 * @return an array of the nodes of the problem, each at its index position
	 */
	public Node[] nodes();

	/**
	 * @param n
	 *            a node of the problem
	 * @return the index of the node in the problem, or -1
	 */
	public int node(Node n);

	/**
	 * @param idx
	 * @return the node at given position, or null
	 */
	public Node node(int idx);

	/**
	 * @return the array of vm of this problem, each vm being on its index
	 */
	public VirtualMachine[] vms();

	/**
	 * @param vm
	 *            a virtual machine of this problem
	 * @return the internal index for this vm, or -1
	 */
	public int vm(VirtualMachine vm);

	/**
	 * @param idx
	 * @return the vm at given pos, or null
	 */
	public VirtualMachine vm(int idx);

	/**
	 * @param vm
	 *            a virtual machine of the problem
	 * @return the index of the node hosting this vm
	 */
	public IntDomainVar host(VirtualMachine vm);

	/**
	 * @param n
	 *            a node of the problem
	 * @return the number of vms hosted on this node
	 */
	public IntDomainVar nbVMs(Node n);

	/**
	 * @param n
	 *            a node of the problem
	 * @return the boolean presence of a vm to host on the node
	 */
	public IntDomainVar isHoster(Node n);


	/**
	 * @param vm
	 *            a vm of the problem
	 * @return true if the vm change host from source to target
	 */
	public IntDomainVar isMigrated(VirtualMachine vm);

	/**
	 * @return the number of migrations performed to pass from source to target
	 */
	public IntDomainVar nbMigrations();

	/**
	 * @return the map of types to the associated resource handlers
	 */
	public HashMap<String, ResourceHandler> getResourcesHandlers();

}
