/* Copyright (c) 2010 Ecole des Mines de Nantes. This file is part of Entropy.
 * Entropy is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Entropy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with Entropy. If not, see
 * <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.solver.choco;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceUse;
import fr.emn.optiplace.solver.SolutionStatistics;
import fr.emn.optiplace.solver.SolvingStatistics;
import fr.emn.optiplace.view.access.CoreView;
import fr.emn.optiplace.view.access.VariablesManager;
import gnu.trove.TIntArrayList;

/**
 * Specification of a reconfiguration problem.
 *
 * @author Fabien Hermenier
 * @author Guillaume Le Louët
 */
public interface ReconfigurationProblem
extends
Solver,
CoreView,
VariablesManager {

  /** The maximum number of group of nodes. */
  Integer MAX_NB_GRP = 100;

  /**
   * Get the current location of a running or a sleeping VM.
   *
   * @param vmIdx
   *            the index of the virtual machine
   * @return the node index if exists or -1 if the VM is not already placed
   */
  int getCurrentLocation(int vmIdx);

  /**
   * Get all the virtual machiens in the model. Indexed by their identifier.
   *
   * @return an array of virtual machines.
   */
  @Override
  VirtualMachine[] vms();

  /**
   * Get the source configuration, that is, the original configuration to
   * optimize.
   *
   * @return a configuration
   */
  Configuration getSourceConfiguration();

  /**
   * Get the formal value of the used CPU of a node.
   *
   * @param n
   *            the node
   * @return the free CPU capacity for this node
   */
  IntDomainVar getUsedCPU(Node n);

  /**
   * Get the free memory capacity of a node.
   *
   * @param n
   *            the node
   * @return the free memory capacity for this node
   */
  IntDomainVar getUsedMem(Node n);

  /**
   * Get the variable associated to a group of VMs. If the group was not
   * defined, it is created. All the VMs must only belong to one group
   *
   * @param vms
   *            the group of virtual machines.
   * @return the variable associated to the group or null if at least one VM
   *         of the proposed new group already belong to a group
   */
  IntDomainVar getVMGroup(Set<VirtualMachine> vms);

  /**
   * Make a group variable.
   *
   * @param vms
   *            the VMs involved in the group
   * @param nodes
   *            the possible hosting group
   * @return a variable denoting the assignment of the VMs group to one of the
   *         group of nodes
   */
  IntDomainVar makeGroup(Set<VirtualMachine> vms, Set<Set<Node>> nodes);

  /**
   * Get the group variable associated to a virtual machine.
   *
   * @param vm
   *            the virtual machine
   * @return the group variable if it exists, null otherwise
   */
  IntDomainVar getAssociatedGroup(VirtualMachine vm);

  /**
   * Get all the defined groups of virtual machines.
   *
   * @return a set of group of VMs, may be empty
   */
  Set<Set<VirtualMachine>> getVMGroups();

  /**
   * Get identifier associated to a group of nodes. If the group was not
   * defined, it is created.
   *
   * @param nodes
   *            the group to define
   * @return the value associated to the group. -1 if the maximum number of
   *         group of nodes has been reached.
   */
  int getGroup(Set<Node> nodes);

  /**
   * Get all the defined groups of nodes.
   *
   * @return a set of group of nodes, may be empty
   */
  Set<Set<Node>> getNodesGroups();

  /**
   * Get the different groups associated to a node.
   *
   * @param n
   *            the node
   * @return a list of groups, may be empty
   */
  TIntArrayList getAssociatedGroups(Node n);

  /**
   * Get the group of nodes associated to an identifier.
   *
   * @param idx
   *            the identifier
   * @return the group of nodes if it exists, null otherwise
   */
  Set<Node> getNodeGroup(int idx);

  int[] getNodesGroupId();

  /**
   * Get the set model of the nodes. One set per nodes
   *
   * @return an array of set
   */
  SetVar[] getSetModels();

  /**
   * Get the set associated to a node.
   *
   * @param n
   *            the node
   * @return the associated set if exists, {@code null} otherwise
   */
  SetVar getSetModel(Node n);

  /**
   * get the variables of the hoster of the VM in the end configuration.
   *
   * @param vm
   *            the VirtualMAchine to consider
   * @return a variable specifying on which Node the VM will be hosted
   */
  @Override
  IntDomainVar host(VirtualMachine vm);

  /**
   * get the array of VMs hosters. faster to iterate over it than using
   * {@link #host(VirtualMachine)}
   *
   * @params vms the vms to filter the hosters on if specified.
   * @return the array of VM hosters, indexed by the vms indexes or the
   *         position of each vm in vms if not null and not empty.
   */
  IntDomainVar[] getHosters(VirtualMachine... vms);

  /**
   * get the variable corresponding to the number of VMs associated to the
   * host in the final configuration
   *
   * @param n
   *            the node to get that number
   * @return the internal variable constrained to the number of VM executed on
   *         the node at the end of the reconfiguration plan.
   */
  @Override
  IntDomainVar nbVMs(Node n);

  /**
   * get the table {@link #nbVMs(Node)} , indexed by the nodes index (
   * {@link #getNode(int)} )
   *
   * @return
   */
  IntDomainVar[] getNbHosted();

  /**
   * get the boolean variable set to 1 IFF at least one VM is assigned to this
   * Node in the resulting configuration
   *
   * @param n
   *            the Node to get the
   * @return
   */
  @Override
  IntDomainVar isHoster(Node n);

  /**
   * get the max CPU associated to the host executing a virtual machine in the
   * destination configuration
   *
   * @param vm
   *            the vm.
   * @return a internal variable constrained to the CPU capacity of the Host
   *         executing vm.
   */
  IntDomainVar getHostMaxCPU(VirtualMachine vm);

  /**
   * get the real CPU used associated to the host executing a virtual machine
   * in the destination configuration
   *
   * @param vm
   *            the vm.
   * @return a internal variable constrained to the CPU used on the Host
   *         executing vm.
   */
  IntDomainVar getHostUsedCPU(VirtualMachine vm);

  /**
   * get the table of {@link #getUsedCPU(Node)} , indexed with
   * {@link #getNode(int)}
   *
   * @return the table of the nodes' used CPUs.
   */
  IntDomainVar[] getUsedCPUs();

  /**
   * get the boolean variable constrained to the migrated state of the VM. A
   * VM is live migrated if it was running or suspend and need to be run or
   * suspended on a different host.
   *
   * @param vm
   *            a VM to be run or not
   * @return a variable constrained to the need to live migrate the VM from
   *         {@link #getSourceConfiguration()} to the final configurations.
   */
  @Override
  IntDomainVar isMigrated(VirtualMachine vm);

  /**
   * get the table of boolean for the VMs.
   *
   * @see #isMigrated(VirtualMachine)
   * @return the table of IntDomainVar, so that
   *         ret[i]==isLiveMigrate(getVirtualMachine(i))
   */
  IntDomainVar[] getIsMigrateds();

  /**
   *
   * @return a variable constrained to the number of live migrations to
   *         realize
   */
  @Override
  IntDomainVar nbMigrations();

  /**
   * Get statistics about computed solutions.
   *
   * @return a list of statistics that may me empty.
   */
  List<SolutionStatistics> getSolutionsStatistics();

  /**
   * Get statistics about the solving process
   *
   * @return some statistics
   */
  SolvingStatistics getSolvingStatistics();

  /** get the internal list of cost constraints */
  @SuppressWarnings("rawtypes")
  public List<SConstraint> getCostConstraints();

  /**
   * @param left
   * @param right
   * @return a new variable constrained to ret=left+right
   */
  IntDomainVar plus(IntDomainVar left, IntDomainVar right);

  /**
   * add a constraint : sum=left+right
   *
   * @param left
   * @param right
   * @param sum
   */
  public void plus(IntDomainVar left, IntDomainVar right, IntDomainVar sum);

  /**
   * @param vars
   * @return a new variable constrained to the sum of the elements of vars
   */
  public IntDomainVar sum(IntDomainVar... vars);

  /**
   * @param left
   * @param right
   * @return a variable constrained to ret=left*min. if left is a boolean ( =
   *         [0..1] ) var, the variable domain is an enum containing {0,min}
   */
  IntDomainVar mult(IntDomainVar left, int right);

  /**
   * @param left
   * @param right
   * @return a variable constrained to ret=left * right
   */
  IntDomainVar mult(IntDomainVar left, IntDomainVar right);

  /**
   * @param var
   * @param i
   * @return a variable constrained to ret=var/i
   */
  IntDomainVar div(IntDomainVar var, int i);

  /**
   * make a variable constrained to the scalar product of the elements. The
   * weights are multiplied by a common value to prevent granularity issues.
   *
   * @param pos
   *            the pos in each dimension
   * @param weigth
   *            the weight of each dimensions
   * @return the scalar product of the positions to the weights
   */
  public IntDomainVar scalar(IntDomainVar[] pos, double[] weights);

  /**
   * creates a new var z constrained by z =(x==y)
   *
   * @param x
   * @param y
   * @return a new variable constrained to ret=1 if x and y are instancied to
   *         the same value
   */
  IntDomainVar isSame(IntDomainVar x, IntDomainVar y);

  /**
   * creates a new var z constrained by z =(x==y)
   *
   * @param x
   * @param y
   * @return a new variable constrained to ret=1 if x and y are instancied to
   *         different value
   */
  IntDomainVar isDifferent(IntDomainVar x, IntDomainVar y);

  /**
   * @return a new variables constrained to ret == x?!=y
   */
  IntDomainVar isDifferent(IntDomainVar x, int y);

  /**
   * @param values
   * @return a variable constrained to the maximum value reached in values
   */
  IntDomainVar max(IntDomainVar... values);

  /**
   * @param values
   * @return a variable constrained to the min values reached in values
   */
  IntDomainVar min(IntDomainVar... values);

  /**
   * Legacy code
   *
   * @param vars
   *            table of data to make the sum of.
   * @param step
   *            step to skip data in vars
   * @param post
   *            should the returned constrained be added in this or added in
   *            the {@link #getCostConstraints()} to be added later ?
   * @return a variable constrained to the sum of the variables in vars
   */
  // IntExp explodedSum(IntDomainVar[] vars, int step, boolean post);

  /**
   * @param index
   * @param array
   * @return a new variable constrained by ret=array[index]
   */
  IntDomainVar nth(IntDomainVar index, IntDomainVar[] array);

  /**
   * ensures var belongs to array[index], ie var.inf>=array[index].min and
   * var.sup<=array[index].inf
   *
   * @param index
   *            variable to index
   * @param array
   *            array of variables
   * @param var
   *            variable
   */
  void nth(IntDomainVar index, IntDomainVar[] array, IntDomainVar var);
  /**
   * ensures var belongs to array[index], ie var.inf>=array[index].min and
   * var.sup<=array[index].inf
   *
   * @param index
   *            variable to index
   * @param array
   *            array of int
   * @param var
   *            variable
   */
  void nth(IntDomainVar index, int[] array, IntDomainVar var);

  /**
   * get a boolean value of an int value
   *
   * @param x
   *            the int value to booleanize
   * @param name
   *            the name of the variable to return, or null to let it create
   *            the name
   * @return a new variable constrained to 1 if x>0, 0 either way.
   */
  IntDomainVar boolenize(IntDomainVar x, String name);

  /**
   * Extract the result destination configuration.
   *
   * @return a configuration corresponding to the result computed by the
   *         {@link #solve()}
   */
  Configuration extractConfiguration();

  /**
   * add an {@link ResourceHandler} to manage the consumption variables of a
   * resource
   *
   * @param handler
   *            the handler, already containing
   */
  void addResourceHandler(ResourceHandler handler);

  /**
   * @param res
   *            the name of the resource to get the usage, should be present
   *            in {@link #getResourceSpecifications()} keys
   * @return the variable of the uses of the resource
   */
  ResourceUse getUse(String res);

  /**
   * create a new table of the different {@link ResourceUse}
   *
   * @return
   */
  ResourceUse[] getUses();

  /**
   *
   * @return the map of types to the associated resource handlers
   */
  @Override
  HashMap<String, ResourceHandler> getResourcesHandlers();

  /**
   * creates a constant or get it in the cache. the name of the created
   * constant is val
   */
  @Override
  IntDomainVar createIntegerConstant(int val);
}
