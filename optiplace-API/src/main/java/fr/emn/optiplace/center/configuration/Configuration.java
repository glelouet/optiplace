/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.center.configuration;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import fr.emn.optiplace.center.configuration.resources.ResourceSpecification;


/**
 * <p>
 * Node and VirtualMachine in a datacenter.<br />
 * VMs are hosted by an online Node or waiting, and Nodes are either online or
 * offline<
 * </p>
 * <p>
 * Most methods do NOT handle the case where a Node or VM is null, although some
 * methods may return null values.
 * </p>
 * <p>
 * The VM and Node are stored in their add/state change order, meaning two
 * iterations on the streams will return the same exact result, leading to
 * determinism in the exploration.<br />
 * If the list of Nodes or VMs is the same for two configuration created in the
 * same way, the position in the list does not mean anything about the age of
 * the Element. Actually, the full list of Nodes and VMs is a concatenation of
 * the different state elements.
 * </p>
 *
 * @author Fabien Hermenier
 * @author guillaume Le Louët
 */
public interface Configuration {

	static enum VMSTATES {
		RUNNING, WAITING
	}

	static enum NODESTATES {
		ONLINE, OFFLINE
	}

	/**
	 * tooling to compare if two configurations have same vms and same nodes
	 *
	 * @param first
	 * @param second
	 * @return
	 */
	public static boolean sameElements(Configuration first, Configuration second) {
		return !first.getNodes().parallel().filter(n -> !second.hasNode(n)).findAny().isPresent()
		    && !first.getVMs().parallel().filter(v -> !second.hasVM(v)).findAny().isPresent()
		    && !second.getNodes().parallel().filter(n -> !first.hasNode(n)).findAny().isPresent()
		    && !second.getVMs().parallel().filter(v -> !first.hasVM(v)).findAny().isPresent();
	}

	/**
	 * Get the list of nodes that are online.
	 *
	 * @return a Stream of all the nodes online in this configuration
	 */
	Stream<Node> getOnlines();

	/**
	 * get a Stream of the nodes which are online and whom set of hosted VMs
	 * follow one predicate. The predicate is applied to unmodifiable set, and the
	 * VMs are unmutable so this call can not modify this
	 *
	 * @param pred
	 *          a predicate over the hosted VMs of a Node with no side-effect on
	 *          the set.
	 * @return The stream of node following the predicate over their hosted VMs
	 */
	Stream<Node> getOnlines(Predicate<Set<VM>> pred);

	/**
	 * Get the nodes that are offline.
	 *
	 * @return a Stream of all the nodes offline in this configuration
	 */
	Stream<Node> getOfflines();

	/**
	 * Get all the nodes involved in the configuration.
	 *
	 * @return a Stream, may be empty
	 */
	default Stream<Node> getNodes() {
		return Stream.concat(getOnlines(), getOfflines());
	}

	/**
	 * get the number of Node with given state
	 *
	 * @param state
	 *          the state of the nodes to consider, or null for all nodes
	 * @return the number of nodes with given state if not null, or the number of
	 *         nodes if null
	 */
	int nbNodes(NODESTATES state);

	/**
	 * Get the virtual machines that are running.
	 *
	 * @return a Stream of VirtualMachines, may be empty
	 */
	Stream<VM> getRunnings();

	/**
	 * Get the virtual machines that are waiting.
	 *
	 * @return a Stream, may be empty
	 */
	Stream<VM> getWaitings();

	/**
	 * Get all the virtual machines involved in the configuration.
	 *
	 * @return a Stream, may be empty
	 */
	default Stream<VM> getVMs() {
		return Stream.concat(getRunnings(), getWaitings());
	}

	/**
	 * get the number of known VMs with given state
	 *
	 * @param state
	 *          the state of the VMs to count, nor null for all VMs
	 * @return the number of VMs with the given state, or of all VMs if state is
	 *         null
	 */
	int nbVMs(VMSTATES state);

	/**
	 * get the number of VMs running on given node
	 *
	 * @param host
	 *          the node to consider
	 * @return the number of vms which are specified running on the node ; null if
	 *         the node is not known
	 */
	int nbHosted(Node host);

	/**
	 * @param n
	 *          a Node
	 * @return the state of the Node in the configuration, or null if the Node is
	 *         not known
	 */
	default NODESTATES getState(Node n) {
		if (isOnline(n)) {
			return NODESTATES.ONLINE;
		}
		if (isOffline(n)) {
			return NODESTATES.OFFLINE;
		}
		return null;
	}

	/**
	 * Test if a node is online.
	 *
	 * @param n
	 *          the node
	 * @return true if the node is online
	 */
	boolean isOnline(Node n);

	/**
	 * Test if a node is offline.
	 *
	 * @param n
	 *          the node
	 * @return true if the node is offline
	 */
	boolean isOffline(Node n);

	/**
	 * @param n
	 *          a VM
	 * @return the state of the VM in the configuration, or null if the VM is not
	 *         known
	 */
	default VMSTATES getState(VM n) {
		if (isRunning(n)) {
			return VMSTATES.RUNNING;
		}
		if (isWaiting(n)) {
			return VMSTATES.WAITING;
		}
		return null;
	}

	/**
	 * Test if a virtual machine is running.
	 *
	 * @param vm
	 *          the virtual machine
	 * @return true if the virtual machine is running
	 */
	boolean isRunning(VM vm);

	/**
	 * Test if a virtual machine is waiting.
	 *
	 * @param vm
	 *          the virtual machine
	 * @return true if the virtual machine is waiting
	 */
	boolean isWaiting(VM vm);

	/**
	 * check if a node is already present in this
	 *
	 * @param n
	 *          a Node
	 * @return true if a node equal to this one already exist
	 */
	default boolean hasNode(Node n) {
		return isOnline(n) || isOffline(n);
	}

	/**
	 * check if a VM is already present in this
	 *
	 * @param vm
	 *          a VirtualMachine
	 * @return true if a VM equal to this one already exist
	 */
	default boolean hasVM(VM vm) {
		return isRunning(vm) || isWaiting(vm);
	}

	/**
	 * Set a virtual machine running on a node. The node is set online whatever
	 * his previous state was. If the virtual machine is already in a other
	 * location or state in the configuration, it is updated
	 *
	 * @param vm
	 *          the virtual machine
	 * @param node
	 *          the node that will host the virtual machine.
	 * @return true if the vm is assigned to the node and was not before
	 */
	boolean setHost(VM vm, Node node);

	/**
	 * Set a virtual machine waiting. If the virtual machine is already in a other
	 * location or state in the configuration, it is updated
	 *
	 * @param vm
	 *          the virtual machine
	 * @return true if the VM state changed
	 */
	boolean setWaiting(VM vm);

	/**
	 * set a migration target for a VM
	 *
	 * @param vm
	 *          the vm migrating
	 * @param n
	 *          null to set no migration, a Node to specify where the VM is
	 *          migrating
	 */
	void setMigrationTarget(VM vm, Node n);

	/**
	 * @param vm
	 *          a VM of the center
	 * @return a Node if that vm is migrating to this node, or null.
	 */
	Node getMigrationTarget(VM vm);

	/**
	 *
	 * @param vm
	 *          a VM of the center
	 * @return true if the VM is migrating to another node
	 */
	default boolean isMigrating(VM vm) {
		Node target = getMigrationTarget(vm);
		return target != null && !target.equals(getLocation(vm));
	}

	/**
	 * Add a VM in this; putting it on a Node or waiting. It also map the
	 * specified resource uses in the linkedHashMap of resources.
	 *
	 * @param vmName
	 *          the name of the VM
	 * @param host
	 *          the host or null if it is waiting
	 * @param resources
	 *          the resource use of the VM. Their must be less than
	 *          {@link #resources()} size, and any missing will be set to 0.
	 * @return a new VM with given specifications
	 */
	VM addVM(String vmName, Node host, int... resources);

	/**
	 * Remove a virtual machine.
	 *
	 * @param vm
	 *          the virtual machine to remove
	 * @return true if this VM was present
	 */
	boolean remove(VM vm);

	/**
	 * Set a node online. If the node is already in the configuration but in an
	 * another state, it is updated.
	 *
	 * @param node
	 *          the node to add
	 * @return true if the node state changed
	 */
	boolean setOnline(Node node);

	Node addOnline(String name, int... resources);

	Node addOffline(String name, int... resources);

	/**
	 * Set a node offline. If the node is already in the configuration but in an
	 * another state, it is updated. Any hosted VM state will be set to waiting.
	 *
	 * @param node
	 *          the node
	 * @return true if the node state changed
	 */
	boolean setOffline(Node node);

	/**
	 * Remove a node and set all its vms to waiting
	 *
	 * @param n
	 *          the node to remove
	 * @return true if the Node was present
	 */
	boolean remove(Node n);

	/**
	 * Get the virtual machines that are running on a node.
	 *
	 * @param n
	 *          the node
	 * @return a set of virtual machines, may be empty, eg if the Node is not
	 *         present or is offline
	 */
	Stream<VM> getHosted(Node n);

	/**
	 * Get all the virtual machines running on a set of nodes.
	 *
	 * @param ns
	 *          the set of nodes
	 * @return a set of virtual machines, may be empty
	 */
	default Stream<VM> getHosted(Set<Node> ns) {
		return ns.stream().map(this::getHosted).reduce(Stream::concat).get();
	}

	/**
	 * Get the location of a virtual machine.
	 *
	 * @param vm
	 *          the virtual machine
	 * @return the node hosting the virtual machine or {@code null} is the virtual
	 *         machine is waiting
	 */
	Node getLocation(VM vm);

	/** get the known list of resources specifications. It can be modified */
	LinkedHashMap<String, ResourceSpecification> resources();

	static enum BasicChecks {

		OFFLINE_OR_ONLINE {

			@Override
			public boolean check(Configuration c) {
				return !c.getOfflines().filter(c::isOnline).findFirst().isPresent();
			}

		},
		RUNNING_OR_WAITING {

			@Override
			public boolean check(Configuration c) {
				return !c.getWaitings().filter(c::isRunning).findFirst().isPresent();
			}

		},
		HOSTER_HOSTS {

			@Override
			public boolean check(Configuration c) {
				return !c.getRunnings().filter(v -> !c.getHosted(c.getLocation(v)).filter(v::equals).findFirst().isPresent())
				    .findFirst().isPresent();
			}

		};

		public abstract boolean check(Configuration c);
	}

	default boolean checkBasics() {
		for (BasicChecks b : BasicChecks.values()) {
			if (!b.check(this)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * compute the max number of VM of a kind a Node can host, based on the
	 * resource capacities of the node and the use of the VM.
	 *
	 * @param n
	 *          a Node
	 * @param vm
	 *          a VM to use the resource specifications
	 * @param specs
	 *          the resource specifications
	 * @return the minimum of capa(n)/use(VM) for each resource of specs
	 */
	public static double maxNBVms(Node n, VM vm, Stream<ResourceSpecification> specs) {
		return specs.mapToDouble(s -> 1.0 * s.getCapacity(n) / s.getUse(vm)).min().getAsDouble();
	}

}
