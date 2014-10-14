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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.set.SCF;
import solver.variables.*;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.solver.SolvingStatistics;
import fr.emn.optiplace.view.access.CoreView;
import fr.emn.optiplace.view.access.VariablesManager;
import gnu.trove.list.array.TIntArrayList;

/**
 * Specification of a reconfiguration problem.
 *
 * @author Fabien Hermenier
 * @author Guillaume Le Louët
 */
public interface ReconfigurationProblem extends CoreView, VariablesManager {

  /** The maximum number of group of nodes. */
  Integer MAX_NB_GRP = 100;

  public Solver getSolver();

  @Override
  default IntVar createIntegerConstant(int val) {
    return VariableFactory.fixed(val, getSolver());
  }

  @Override
  default BoolVar createBoolVar(String name) {
    return VariableFactory.bool(name, getSolver());
  }

  @Override
  default IntVar createEnumIntVar(String name, int... sortedValues) {
    return VariableFactory.enumerated(name, sortedValues, getSolver());
  }

  @Override
  default IntVar createEnumIntVar(String name, int min, int max) {
    return VariableFactory.enumerated(name, min, max, getSolver());
  }

  /** creates an int variable whom range goes from min to max */
  @Override
  default IntVar createBoundIntVar(String name, int min, int max) {
    return VariableFactory.bounded(name, min, max, getSolver());
  }

  @Override
  default SetVar createEnumSetVar(String name, int... values) {
    return VariableFactory.set(name, values, getSolver());
  }

  @Override
  default SetVar createRangeSetVar(String name, int min, int max) {
    return VariableFactory.set(name, min, max, getSolver());
  }

  default SetVar toSet(IntVar... vars) {
    Solver s = getSolver();
    if (vars == null || vars.length == 0) {
      return VF.set("empty set", new int[] {}, s);
    }
    SetVar ret = VF.set("setof" + Arrays.asList(vars), Integer.MIN_VALUE + 1,
        Integer.MAX_VALUE - 1, s);
    s.post(SCF.int_values_union(vars, ret));
    return ret;
  }

  /**
   * Get the current location of a running or a sleeping VM.
   *
   * @param vmIdx
   * the index of the virtual machine
   * @return the node index if exists or -1 if the VM is not already placed
   */
  int getCurrentLocation(int vmIdx);

  /**
   * Get the source configuration, that is, the original configuration to
   * optimize.
   *
   * @return a configuration
   */
  Configuration getSourceConfiguration();

  default IntVar getNodeUse(String resource, Node n) {
    return getResourcesHandlers().get(resource).getNodeUses()[node(n)];
  }

  default int getNodeCap(String resource, Node n) {
    return getResourcesHandlers().get(resource).getCapacities()[node(n)];
  }

  default IntVar getUsedCPU(Node n) {
    return getNodeUse("CPU", n);
  }

  default IntVar getUsedMem(Node n) {
    return getNodeUse("MEM", n);
  }

  public IntVar getHostUse(String resource, int vmIndex);

  public IntVar getHostCapa(String resource, int vmIndex);

  /**
   * Get the variable associated to a group of VMs. If the group was not
   * defined, it is created. All the VMs must only belong to one group
   *
   * @param vms
   * the group of virtual machines.
   * @return the variable associated to the group or null if at least one VM of
   * the proposed new group already belong to a group
   */
  IntVar getVMGroup(Set<VM> vms);

  /**
   * Make a group variable.
   *
   * @param vms
   * the VMs involved in the group
   * @param node2s
   * the possible hosting group
   * @return a variable denoting the assignment of the VMs group to one of the
   * group of nodes
   */
  IntVar makeGroup(Set<VM> vms, Set<Set<Node>> node2s);

  /**
   * Get the group variable associated to a virtual machine.
   *
   * @param vm
   * the virtual machine
   * @return the group variable if it exists, null otherwise
   */
  IntVar getAssociatedGroup(VM vm);

  /**
   * Get all the defined groups of virtual machines.
   *
   * @return a set of group of VMs, may be empty
   */
  Set<Set<VM>> getVMGroups();

  /** Get identifier associated to a group of nodes. If the group was not
   * defined, it is created.
   * @param nodes the group to define
   * @return the value associated to the group. -1 if the maximum number of
   * group of nodes has been reached. */
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
   * the node
   * @return a list of groups, may be empty
   */
  TIntArrayList getAssociatedGroups(Node n);

  /**
   * Get the group of nodes associated to an identifier.
   *
   * @param idx
   * the identifier
   * @return the group of nodes if it exists, null otherwise
   */
  Set<Node> getNodeGroup(int idx);

  int[] getNodesGroupId();

  /**
   * Get statistics about the solving process
   *
   * @return some statistics
   */
  SolvingStatistics getSolvingStatistics();

  /** get the internal list of cost constraints */
  public List<Constraint> getCostConstraints();

  /**
   * @param left
   * @param right
   * @return a new variable constrained to ret=left+right
   */
  IntVar plus(IntVar left, IntVar right);

  /**
   * @param vars
   * @return a new variable constrained to the sum of the elements of vars
   */
  public IntVar sum(IntVar... vars);

  /**
   * @param left
   * @param right
   * @return a variable constrained to ret=left*min. if left is a boolean ( =
   * [0..1] ) var, the variable domain is an enum containing {0,min}
   */
  IntVar mult(IntVar left, int right);

  /**
   * @param left
   * @param right
   * @return a variable constrained to ret=left * right
   */
  IntVar mult(IntVar left, IntVar right);

  /**
   * @param var
   * @param i
   * @return a variable constrained to ret=var/i
   */
  IntVar div(IntVar var, int i);

  /**
   * make a variable constrained to the scalar product of the elements. The
   * weights are multiplied by a common value to prevent granularity issues.
   *
   * @param pos
   * the pos in each dimension
   * @param weigth
   * the weight of each dimensions
   * @return the scalar product of the positions to the weights
   */
  public IntVar scalar(IntVar[] pos, double[] weights);

  public IntVar scalar(IntVar[] pos, int[] mults);

  /**
   * creates a new var z constrained by z =(x==y)
   *
   * @param x
   * @param y
   * @return a new variable constrained to ret=1 if x and y are instancied to
   * the same value
   */
  BoolVar isSame(IntVar x, IntVar y);

  /**
   * creates a new var z constrained by z =(x==y)
   *
   * @param x
   * @param y
   * @return a new variable constrained to ret=1 if x and y are instancied to
   * different value
   */
  BoolVar isDifferent(IntVar x, IntVar y);

  /**
   * @return a new variables constrained to ret == x?!=y
   */
  BoolVar isDifferent(IntVar x, int y);

  /**
   * @param values
   * @return a variable constrained to the maximum value reached in values
   */
  IntVar max(IntVar... values);

  /**
   * @param values
   * @return a variable constrained to the min values reached in values
   */
  IntVar min(IntVar... values);

  /**
   * Legacy code
   *
   * @param vars
   * table of data to make the sum of.
   * @param step
   * step to skip data in vars
   * @param post
   * should the returned constrained be added in this or added in the
   * {@link #getCostConstraints()} to be added later ?
   * @return a variable constrained to the sum of the variables in vars
   */
  // IntExp explodedSum(IntVar[] vars, int step, boolean post);

  /**
   * @param index
   * @param array
   * @return a new variable constrained by ret=array[index]
   */
  IntVar nth(IntVar index, IntVar[] array);

  /**
   * ensures var belongs to array[index], ie var.inf>=array[index].min and
   * var.sup<=array[index].inf
   *
   * @param index
   * variable to index
   * @param array
   * array of variables
   * @param var
   * variable
   */
  void nth(IntVar index, IntVar[] array, IntVar var);

  /**
   * ensures var belongs to array[index], ie var.inf>=array[index].min and
   * var.sup<=array[index].inf
   *
   * @param index
   * variable to index
   * @param array
   * array of int
   * @param var
   * variable
   */
  void nth(IntVar index, int[] array, IntVar var);

  /**
   * get a boolean value of an int value
   *
   * @param x
   * the int value to booleanize
   * @param name
   * the name of the variable to return, or null to let it create the name
   * @return a new variable constrained to 1 if x>0, 0 either way.
   */
  BoolVar boolenize(IntVar x, String name);

  /**
   * Extract the result destination configuration.
   *
   * @return a configuration corresponding to the result computed by the
   * {@link #solve()}
   */
  Configuration extractConfiguration();

  /**
   * add an {@link ResourceHandler} to manage the consumption variables of a
   * resource
   *
   * @param handler
   * the handler, already containing
   */
  void addResourceHandler(ResourceHandler handler);
}
