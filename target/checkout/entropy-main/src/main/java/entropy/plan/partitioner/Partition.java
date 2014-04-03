/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.plan.partitioner;

import java.util.ArrayList;
import java.util.List;

import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.vjob.PlacementConstraint;

/** @author Fabien Hermenier */
public class Partition {

  private ManagedElementSet<VirtualMachine> vms;

  private ManagedElementSet<Node> nodes;

  private List<PlacementConstraint> constraints;

  private int nb;

  public Partition(int nb) {
    this.nb = nb;
    vms = new SimpleManagedElementSet<VirtualMachine>();
    nodes = new SimpleManagedElementSet<Node>();
    constraints = new ArrayList<PlacementConstraint>();
  }

  public ManagedElementSet<Node> getNodes() {
    return nodes;
  }

  public ManagedElementSet<VirtualMachine> getVirtualMachines() {
    return vms;
  }

  public List<PlacementConstraint> getConstraints() {
    return constraints;
  }

  @Override
  public String toString() {
    return new StringBuilder("partition ").append(nb).toString();
  }
}
