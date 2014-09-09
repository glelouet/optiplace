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

/** @author Fabien Hermenier
 * @author guillaume Le Louët */
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

  /** @param n a node
   * @return true if the node state is specified */
  default boolean contains(Node n) {
    return isOnline(n) || isOffline(n);
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

  /** Indicates if a virtual machine is included in this.
   * @param vm the virtual machine
   * @return {@code true} if the virtual machine is in the configuration. {code
   * false} otherwise */
  default boolean contains(VirtualMachine vm) {
    return isRunning(vm) || isWaiting(vm);
  }

  /** Test if a virtual machine is running.
   * @param vm the virtual machine
   * @return true if the virtual machine is running */
  boolean isRunning(VirtualMachine vm);

  /** Test if a virtual machine is waiting.
   * @param vm the virtual machine
   * @return true if the virtual machine is waiting */
  boolean isWaiting(VirtualMachine vm);

  /** create a new Node if possible, with state {@link NODESTATES.#OFFLINE}
   * @param name the name of the Node
   * @return a new Node with given name, or null if a Node with this name
   * already exists */
  Node makeNode(String name);

  Node getNode(String name);

  default Node enforceNode(String name) {
    Node ret = makeNode(name);
    return ret != null ? ret : getNode(name);
  }

  public Node getNode(long id);

  /** create a new VM if possible, with new state {@link VMSTATES.#WAITING}
   * @param name the name of the VM
   * @return a new VM with given name, or null if a VM with this name already
   * exists. */
  VirtualMachine makeVM(String name);

  VirtualMachine getVM(String name);

  default VirtualMachine enforceVM(String name) {
    VirtualMachine ret = makeVM(name);
    return ret != null ? ret : getVM(name);
  }

  VirtualMachine getVM(long id);

  default ManagedElement getElement(long id) {
    Node ret = getNode(id);
    return ret != null ? ret : getVM(id);
  }

  /** @param name the name of the element to return
   * @return the only element with given name, or null. */
  default ManagedElement getElement(String name) {
    Node ret = getNode(name);
    return ret != null ? ret : getVM(name);
  }

  /** Set a virtual machine running on a node. The node is set online whatever
   * his previous state was. If the virtual machine is already in a other
   * location or state in the configuration, it is updated
   * @param vm the virtual machine
   * @param node the node that will host the virtual machine. Must be considered
   * as online.
   * @return true if the vm is assigned on the node. False otherwise */
  boolean setHost(VirtualMachine vm, Node node);

  /**
   * Set a virtual machine waiting. If the virtual machine is already in a
   * other location or state in the configuration, it is updated
   *
   * @param vm
   *            the virtual machine
   */
  void setWaiting(VirtualMachine vm);

  /**
   * Remove a virtual machine.
   *
   * @param vm
   *            the virtual machine to remove
   */
  void remove(VirtualMachine vm);

  /**
   * Set a node online. If the node is already in the configuration but in an
   * another state, it is updated.
   *
   * @param node
   *            the node to add
   */
  void setOnline(Node node);

  /** Set a node offline. If the node is already in the configuration but in an
   * another state, it is updated. Any hosted VM state will be set to waiting.
   * @param node the node
   * @return true if the node is offline. False otherwise */
  boolean setOffline(Node node);

  /** Remove a node. The node must not host any virtual machines
   * @param n the node to remove
   * @return {@code true} if the node was removed. {@code false} otherwise */
  boolean remove(Node n);

  /**
   * Get the virtual machines that are running on a node.
   *
   * @param n
   *            the node
   * @return a set of virtual machines, may be empty
   */
  Stream<VirtualMachine> getHosted(Node n);

  /** Get all the virtual machines running on a set of nodes.
   * @param ns the set of nodes
   * @return a set of virtual machines, may be empty */
  default Stream<VirtualMachine> getHosted(Set<Node> ns) {
    return ns.stream().map(this::getHosted).reduce(Stream::concat).get();
  }

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
   * Shallow copy of the configuration. The state and the assignment of the
   * element are copied but elements are not duplicated.
   *
   * @return a copy of the configuration
   */
  Configuration clone();

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

  /** get the known list of resources specifications */
  Map<String, ResourceSpecification> resources();

}
