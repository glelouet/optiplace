/**
 *
 */
package fr.emn.optiplace.view.access;

import java.util.HashMap;

import solver.variables.IntVar;
import solver.variables.SetVar;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
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
	 * a node of the problem
	 * @return the index of the node in the problem, or -1
	 */
	public int node(Node n);

	/**
	 * @param idx
	 * @return the node at given position, or null
	 */
	public Node node(int idx);

	/**
	 * @return the array of vm of this problem, each vm being on its index,
	 * meaning vm(i)= vms[i]
	 */
	public VM[] vms();

	/**
	 * @param vm
	 * a virtual machine of this problem
	 * @return the internal index for this vm, or -1 if not known
	 */
	public int vm(VM vm);

	/**
	 * @param idx
	 * @return the vm at given pos, or null
	 */
	public VM vm(int idx);

	/**
	 * @param vm
	 * a virtual machine of the problem
	 * @return the index of the node hosting this vm
	 */
	public IntVar host(VM vm);

	/**
	 * @param n
	 * a node of the problem
	 * @return the number of vms hosted on this node
	 */
	public IntVar nbVMs(Node n);

	/**
	 * @param n
	 * a node of the problem
	 * @return the set of all VMs hosted on this node in the dest configuration
	 */
	public SetVar vms(Node n);

	/**
	 * @return the array of setVar, each setvar at index i corresponding to the
	 * set of VMs hosted by the Node i in the dest configuration.
	 */
	public SetVar[] hosteds();

	/**
	 * @param n
	 * a node of the problem
	 * @return the boolean presence of a vm to host on the node
	 */
	public IntVar isHoster(Node n);

	/**
	 * @param vm
	 * a vm of the problem
	 * @return true if the vm change host from source to target
	 */
	public IntVar isMigrated(VM vm);

	/**
	 * @return the number of migrations performed to pass from source to target
	 */
	public IntVar nbMigrations();

	/**
	 * @return the map of types to the associated resource handlers
	 */
	public HashMap<String, ResourceHandler> getResourcesHandlers();

}
