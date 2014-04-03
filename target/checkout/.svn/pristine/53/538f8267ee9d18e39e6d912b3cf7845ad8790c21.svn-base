/**
 *
 */
package entropy.view.access;

import java.util.HashMap;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.configuration.resources.ResourceHandler;

/**
 * view of the core problem.
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
	 * @return the index of the node in the problem
	 */
	public int node(Node n);

	/**
	 * @return the array of vm of this problem, each vm being on its index
	 */
	public VirtualMachine[] vms();

	/**
	 * @param vm
	 *            a virtual machine of this problem
	 * @return the internal index for this vm
	 */
	public int vm(VirtualMachine vm);

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
	 * @param n
	 *            a node of the problem
	 * @return true if the state of the node has changed - eg from online to
	 *         offline.
	 */
	public IntDomainVar isPowered(Node n);

	/**
	 * @param n
	 *            a node of the problem
	 * @return true if the power state of the node has changed
	 */
	public IntDomainVar isPowerChanged(Node n);

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
	public HashMap<String, ResourceHandler> getHandlers();

}
