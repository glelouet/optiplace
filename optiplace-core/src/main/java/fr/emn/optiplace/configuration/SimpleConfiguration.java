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

import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;

import java.util.*;
import java.util.stream.Stream;

/**
 * Default implementation of Configuration.
 *
 * @author Fabien Hermenier
 */
public class SimpleConfiguration implements Configuration, Cloneable {

  long nextId = Long.MIN_VALUE;

  protected final long id() {
    return ++nextId;
  }

  private final TIntObjectHashMap<Node> idToNodes = new TIntObjectHashMap<>();

  private final TIntObjectHashMap<VirtualMachine> idToVMs = new TIntObjectHashMap<>();

  private final TObjectIntHashMap<ManagedElement> elemToId = new TObjectIntHashMap<>();

  private final TIntObjectHashMap<String> idToName = new TIntObjectHashMap<>();

  private final TObjectIntHashMap<String> nameToId = new TObjectIntHashMap<>();

  private final Set<Node> onlines = new HashSet<>();

  private final Set<Node> offlines = new HashSet<>();

  private final Set<VirtualMachine> waitings = new HashSet<>();

  private final Map<VirtualMachine, Node> vmLocs = new HashMap<>();

  private final Map<Node, Set<VirtualMachine>> hosted = new HashMap<>();

  /** Build an empty configuration. */
  public SimpleConfiguration() {
  }

  protected LinkedHashMap<String, ResourceSpecification> resources = new LinkedHashMap<String, ResourceSpecification>();

  @Override
  public Map<String, ResourceSpecification> resources() {
    return resources;
  }

  @Override
  public Stream<Node> getOnlines() {
    return onlines.stream();
  }

  @Override
  public Stream<Node> getOfflines() {
    return offlines.stream();
  }

  @Override
  public Stream<VirtualMachine> getRunnings() {
    return vmLocs.keySet().stream();
  }

  @Override
  public Stream<VirtualMachine> getWaitings() {
    return waitings.stream();
  }

  @Override
  public boolean isOnline(Node n) {
    return onlines.contains(n);
  }

  @Override
  public boolean isOffline(Node n) {
    return offlines.contains(n);
  }

  @Override
  public boolean isRunning(VirtualMachine vm) {
    return vmLocs.containsKey(vm);
  }

  @Override
  public boolean isWaiting(VirtualMachine vm) {
    return waitings.contains(vm);
  }

  @Override
  public Node makeNode(String name) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public Node getNode(String name) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public Node getNode(long id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public VirtualMachine makeVM(String name) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public VirtualMachine getVM(String name) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public VirtualMachine getVM(long id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public <T extends ManagedElement> T rename(T elem, String newString) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public boolean setHost(VirtualMachine vm, Node node) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public void setWaiting(VirtualMachine vm) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public void remove(VirtualMachine vm) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public void setOnline(Node node) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public boolean setOffline(Node node) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public boolean remove(Node n) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public Stream<VirtualMachine> getHosted(Node n) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public Node getLocation(VirtualMachine vm) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public Configuration clone() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

  @Override
  public int getTotalConsumption(Node n, ResourceSpecification res) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }
}
