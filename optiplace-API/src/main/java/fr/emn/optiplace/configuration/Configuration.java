/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.configuration;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/** @author Fabien Hermenier */
public interface Configuration extends CenterStates {

	/**
	 * Set a virtual machine running on a node. The node must already be online.
	 * If the virtual machine is already in a other location or state in the
	 * configuration, it is updated
	 * 
	 * @param vm
	 *            the virtual machine
	 * @param node
	 *            the node that will host the virtual machine. Must be
	 *            considered as online.
	 * @return true if the vm is assigned on the node. False otherwise
	 */
	boolean setRunOn(VirtualMachine vm, Node node);

	/**
	 * Set a virtual machine sleeping on a node. If the virtual machine is
	 * already in a other location or state in the configuration, it is updated
	 * 
	 * @param vm
	 *            the virtual machine
	 * @param node
	 *            the node that will host the virtual machine. Must be
	 *            considered as online.
	 * @return false if the hosting node is offline or unknown
	 */
	boolean setSleepOn(VirtualMachine vm, Node node);

	/**
	 * Set a virtual machine waiting. If the virtual machine is already in a
	 * other location or state in the configuration, it is updated
	 * 
	 * @param vm
	 *            the virtual machine
	 */
	void addWaiting(VirtualMachine vm);

	/**
	 * Remove a virtual machine.
	 * 
	 * @param vm
	 *            the virtual machine to remove
	 */
	void remove(VirtualMachine vm);

	/**
	 * Remove a node. The node must not host any virtual machines
	 * 
	 * @param n
	 *            the node to remove
	 * @return {@code true} if the node was removed. {@code false} otherwise
	 */
	boolean remove(Node n);

	/**
	 * Set a node online. If the node is already in the configuration but in an
	 * another state, it is updated.
	 * 
	 * @param node
	 *            the node to add
	 */
	void addOnline(Node node);

	/**
	 * Set a node offline. If the node is already in the configuration but in an
	 * another state, it is updated. The node must not host any virtual machines
	 * 
	 * @param node
	 *            the node
	 * @return true if the node is offline. False otherwise
	 */
	boolean addOffline(Node node);

	/**
	 * Get the virtual machines that are sleeping on a node.
	 * 
	 * @param n
	 *            the node
	 * @return a set of virtual machines, may be empty
	 */
	ManagedElementSet<VirtualMachine> getSleepings(Node n);

	/**
	 * Get the virtual machines that are running on a node.
	 * 
	 * @param n
	 *            the node
	 * @return a set of virtual machines, may be empty
	 */
	ManagedElementSet<VirtualMachine> getRunnings(Node n);

	/**
	 * Get the location of a sleeping virtual machine.
	 * 
	 * @param vm
	 *            the virtual machine
	 * @return its host, or null if the virtual machine is not defined as
	 *         sleeping
	 * @deprecated use {@link Configuration#getLocation(VirtualMachine vm)}
	 *             instead
	 */
	@Deprecated
	Node getSleepingLocation(VirtualMachine vm);

	/**
	 * Return the node that host a running virtual machine.
	 * 
	 * @param vm
	 *            The VirtualMachine
	 * @return The node that host the VirtualMachine or null if the virtual
	 *         machine is not defined as running
	 * @deprecated use {@link Configuration#getLocation(VirtualMachine vm)}
	 *             instead
	 */
	@Deprecated
	Node getRunningLocation(VirtualMachine vm);

	/**
	 * Get the location of a running or a sleeping virtual machine.
	 * 
	 * @param vm
	 *            the virtual machine
	 * @return the node hosting the virtual machine or {@code null} is the
	 *         virtual machine is not in the sleeping state nor the running
	 *         state
	 */
	Node getLocation(VirtualMachine vm);

	/**
	 * Get all the virtual machines running on a set of nodes.
	 * 
	 * @param ns
	 *            the set of nodes
	 * @return a set of virtual machines, may be empty
	 */
	ManagedElementSet<VirtualMachine> getRunnings(ManagedElementSet<Node> ns);

	/**
	 * Shallow copy of the configuration. The state and the assignment of the
	 * element are copied but elements are not duplicated.
	 * 
	 * @return a copy of the configuration
	 */
	Configuration clone();

	/**
	 * Indicates if a node is included in the configuration.
	 * 
	 * @param n
	 *            the node
	 * @return {@code true} if the node is in the configuration. {code false}
	 *         otherwise
	 */
	@Override
	boolean contains(Node n);

	/**
	 * @param n
	 *            the node
	 * @return the total consumption of the VMs on the node
	 */
	int getTotalCPUConsumption(Node n);

	/**
	 * get the resource use of a node
	 * 
	 * @param n
	 *            the node to consider
	 * @param res
	 *            the specification of the resources of the vms
	 * @return an int equal to the sum of the uses performed by the hosted vms
	 */
	int getTotalConsumption(Node n, ResourceSpecification res);

	/**
	 * @param name
	 *            the name of the element to return
	 * @return the first element with given name, or null.
	 */
	ManagedElement getElementByName(String name);

	/**
	 * add a set of Nodes as a cluster. If a cluster with given name already
	 * exists, the nodes will be added to it.
	 * 
	 * @param name
	 *            the name of the cluster
	 * @param nodes
	 *            the nodes that belong to this cluster. If null, no node will
	 *            be added to the cluster, but it will be created anyhow
	 * @return true if a corresponding Cluster was created ; false if the
	 *         cluster already existed.
	 */
	boolean addCluster(String name, Node... nodes);

	/**
	 * shortcut for <code>addCluster(name, nodes.toArray(new Node[]{})</code>
	 * 
	 * @param name
	 *            the name of the cluster
	 * @param nodes
	 *            the nodes to add to the cluster, or null to add nothing.
	 * @see #addCluster(String, Node...)
	 */
	boolean addCluster(String name, Collection<Node> nodes);

	/**
	 * retrieve a cluster using its name
	 * 
	 * @param name
	 *            the name of the cluster to retrieve
	 * @return null if no cluster with this name si specified, or the
	 *         corresponding modifiable cluster.
	 */
	ManagedElementSet<Node> getCluster(String name);

	/**
	 * list the names of the clusters
	 * 
	 * @return the set of names of clusters declared
	 */
	Set<String> getClustersNames();

	/**
	 * delete a cluster
	 * 
	 * @param name
	 *            the name of the cluster to delete
	 * @return true if a corresponding cluster has been deleted
	 */
	boolean delCluster(String name);

	/**
	 * get the clusters a Node belongs to.
	 * 
	 * @param n
	 *            the node to get the clusters
	 * @return the map of name-&gt;clusters in which clusters the node appears.
	 */
	Map<String, ManagedElementSet<Node>> getBelongingClusters(Node n);

}
