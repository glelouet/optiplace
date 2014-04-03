package entropy.configuration;

/**
 * specify running states of {@link Node}s and {@link VirtualMachine}s
 * 
 * @author Guillaume Le Louët
 */
public interface StateDefinition {

	/**
	 * Get the list of nodes that are online.
	 * 
	 * @return a list, may be empty
	 */
	ManagedElementSet<Node> getOnlines();

	/**
	 * Get the nodes that are offline.
	 * 
	 * @return a list of nodes, may be empty
	 */
	ManagedElementSet<Node> getOfflines();

	/**
	 * Get the virtual machines that are running.
	 * 
	 * @return a set of VirtualMachines, may be empty
	 */
	ManagedElementSet<VirtualMachine> getRunnings();

	/**
	 * Get the virtual machines that are sleeping.
	 * 
	 * @return a set of virtual machines, may be empty
	 */
	ManagedElementSet<VirtualMachine> getSleepings();

	/**
	 * Get the virtual machines that are waiting.
	 * 
	 * @return a list, may be empty
	 */
	ManagedElementSet<VirtualMachine> getWaitings();

	/**
	 * Get all the virtual machines involved in the configuration.
	 * 
	 * @return a set, may be empty
	 */
	ManagedElementSet<VirtualMachine> getAllVirtualMachines();

	/**
	 * Get all the nodes involved in the configuration.
	 * 
	 * @return a set, may be empty
	 */
	ManagedElementSet<Node> getAllNodes();

	/**
	 * Test if a node is online.
	 * 
	 * @param n
	 *            the node
	 * @return true if the node is online
	 */
	boolean isOnline(Node n);

	/**
	 * Test if a node is offline.
	 * 
	 * @param n
	 *            the node
	 * @return true if the node is offline
	 */
	boolean isOffline(Node n);

	/**
	 * Test if a virtual machine is running.
	 * 
	 * @param vm
	 *            the virtual machine
	 * @return true if the virtual machine is running
	 */
	boolean isRunning(VirtualMachine vm);

	/**
	 * Test if a virtual machine is waiting.
	 * 
	 * @param vm
	 *            the virtual machine
	 * @return true if the virtual machine is waiting
	 */
	boolean isWaiting(VirtualMachine vm);

	/**
	 * Test if a virtual machine is sleeping.
	 * 
	 * @param vm
	 *            the virtual machine
	 * @return true if the virtual machine is sleeping
	 */
	boolean isSleeping(VirtualMachine vm);

	/**
	 * Indicates if a virtual machine is included in this.
	 * 
	 * @param vm
	 *            the virtual machine
	 * @return {@code true} if the virtual machine is in the configuration.
	 *         {code false} otherwise
	 */
	boolean contains(VirtualMachine vm);

}
