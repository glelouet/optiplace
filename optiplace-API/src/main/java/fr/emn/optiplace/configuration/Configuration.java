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

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/**
 * Node and VirtualMachine in a datacenter.<br />
 * Vms are hosted by an online Node or waiting, and Nodes are either online or
 * offline<br />
 * Most methods do NOT handle the case where a Node or VM is null, although some
 * methods may return null values.
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

  /** Get the list of nodes that are online.
   * @return a Stream of all the nodes online in this configuration */
  Stream<Node> getOnlines();

  /** Get the nodes that are offline.
   * @return a Stream of all the nodes offline in this configuration */
  Stream<Node> getOfflines();

  /** Get all the nodes involved in the configuration.
   * @return a Stream, may be empty */
  default Stream<Node> getNodes() {
    return Stream.concat(getOnlines(), getOfflines());
  }

  /** Get the virtual machines that are running.
   * @return a Stream of VirtualMachines, may be empty */
  Stream<VirtualMachine> getRunnings();

  /** Get the virtual machines that are waiting.
   * @return a Stream, may be empty */
  Stream<VirtualMachine> getWaitings();

  /** Get all the virtual machines involved in the configuration.
   * @return a Stream, may be empty */
  default Stream<VirtualMachine> getVMs() {
    return Stream.concat(getRunnings(), getWaitings());
  }

  /** @param n a Node
   * @return the state of the Node in the configuration, or null if the Node is
   * not known */
  default NODESTATES getState(Node n) {
    if (isOnline(n)) {
      return NODESTATES.ONLINE;
    }
    if (isOffline(n)) {
      return NODESTATES.OFFLINE;
    }
    return null;
  }

  /** Test if a node is online.
   * @param n the node
   * @return true if the node is online */
  boolean isOnline(Node n);

  /** Test if a node is offline.
   * @param n the node
   * @return true if the node is offline */
  boolean isOffline(Node n);

  /** @param n a VM
   * @return the state of the VM in the configuration, or null if the VM is not
   * known */
  default VMSTATES getState(VirtualMachine n) {
    if (isRunning(n)) {
      return VMSTATES.RUNNING;
    }
    if (isWaiting(n)) {
      return VMSTATES.WAITING;
    }
    return null;
  }

  /** Test if a virtual machine is running.
   * @param vm the virtual machine
   * @return true if the virtual machine is running */
  boolean isRunning(VirtualMachine vm);

  /** Test if a virtual machine is waiting.
   * @param vm the virtual machine
   * @return true if the virtual machine is waiting */
  boolean isWaiting(VirtualMachine vm);

	/**
	 * check if a node is already present in this
	 *
	 * @param n
	 * a Node
	 * @return true if a node equal to this one already exist
	 */
	default boolean hasNode(Node n) {
		return isOnline(n) || isOffline(n);
	}

	/**
	 * check if a VM is already present in this
	 *
	 * @param vm
	 * a VirtualMachine
	 * @return true if a VM equal to this one already exist
	 */
	default boolean hasVM(VirtualMachine vm) {
		return isRunning(vm) || isWaiting(vm);
	}

	/**
	 * replace an existing VM by another one, keeping it on the same Node
	 *
	 * @param vm
	 * a vm to rename
	 * @param newVM
	 * the new VM reference
	 */
	void replace(VirtualMachine vm, VirtualMachine newVM);

	/**
	 * replaces references to a node by another node
	 *
	 * @param oldNode
	 * the old node reference
	 * @param newNode
	 * the new node to reference
	 */
	void replace(Node oldNode, Node newNode);

  /**
	 * Set a virtual machine running on a node. The node is set online whatever
	 * his previous state was. If the virtual machine is already in a other
	 * location or state in the configuration, it is updated
	 *
	 * @param vm
	 * the virtual machine
	 * @param node
	 * the node that will host the virtual machine.
	 * @return true if the vm is assigned to the node and was not before
	 */
  boolean setHost(VirtualMachine vm, Node node);

  /**
	 * Set a virtual machine waiting. If the virtual machine is already in a other
	 * location or state in the configuration, it is updated
	 *
	 * @param vm
	 * the virtual machine
	 * @return true if the VM state changed
	 */
	boolean setWaiting(VirtualMachine vm);

  /**
	 * Remove a virtual machine.
	 *
	 * @param vm
	 * the virtual machine to remove
	 * @return true if this VM was present
	 */
	boolean remove(VirtualMachine vm);

  /**
	 * Set a node online. If the node is already in the configuration but in an
	 * another state, it is updated.
	 *
	 * @param node
	 * the node to add
	 * @return true if the node state changed
	 */
	boolean setOnline(Node node);

  /**
	 * Set a node offline. If the node is already in the configuration but in an
	 * another state, it is updated. Any hosted VM state will be set to waiting.
	 *
	 * @param node
	 * the node
	 * @return true if the node state changed
	 */
  boolean setOffline(Node node);

	/**
	 * Remove a node and set all its vms to waiting
	 *
	 * @param n
	 * the node to remove
	 * @return true if the Node was present
	 */
  boolean remove(Node n);

  /**
	 * Get the virtual machines that are running on a node.
	 *
	 * @param n
	 * the node
	 * @return a set of virtual machines, may be empty, eg if the Node is not
	 * present or is offline
	 */
  Stream<VirtualMachine> getHosted(Node n);

  /** Get all the virtual machines running on a set of nodes.
   * @param ns the set of nodes
   * @return a set of virtual machines, may be empty */
  default Stream<VirtualMachine> getHosted(Set<Node> ns) {
    return ns.stream().map(this::getHosted).reduce(Stream::concat).get();
  }

  /**
	 * Get the location of a virtual machine.
	 *
	 * @param vm
	 * the virtual machine
	 * @return the node hosting the virtual machine or {@code null} is the virtual
	 * machine is waiting
	 */
  Node getLocation(VirtualMachine vm);

  /** get the known list of resources specifications */
  Map<String, ResourceSpecification> resources();

	static enum BasicChecks {

		OFFLINE_OR_ONLINE {

			@Override
			boolean check(Configuration c) {
				return !c.getOfflines().filter(c::isOnline).findFirst().isPresent();
			}

		},
		RUNNING_OR_WAITING {

			@Override
			boolean check(Configuration c) {
				return !c.getWaitings().filter(c::isRunning).findFirst().isPresent();
			}

		},
		HOSTER_HOSTS{

			@Override
			boolean check(Configuration c) {
				return !c.getRunnings()
						.filter(
								v -> !c.getHosted(c.getLocation(v)).filter(v::equals)
										.findFirst().isPresent()).findFirst()
						.isPresent();
			}

		};

		abstract boolean check(Configuration c);
	}

	default boolean checkBasics(){
		for( BasicChecks b : BasicChecks.values()){
			if(!b.check(this)) {
				return false;
			}
		}
		return true;
	}

}
