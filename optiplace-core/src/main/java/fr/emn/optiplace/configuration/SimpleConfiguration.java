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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/**
 * Default implementation of Configuration.
 *
 * @author Guillaume Le Louët
 */
public class SimpleConfiguration implements Configuration {

	private final Set<Node> offlines = new HashSet<>();

	private final Map<Node, Set<VirtualMachine>> hosted = new HashMap<>();

	private final Set<VirtualMachine> waitings = new HashSet<>();

  private final Map<VirtualMachine, Node> vmLocs = new HashMap<>();



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
		return hosted.keySet().stream();
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
		return hosted.containsKey(n);
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
  public boolean setHost(VirtualMachine vm, Node node) {
		if (vm == null || node == null || node.equals(vmLocs.get(vm))) {
			return false;
		}
		setOnline(node);
		waitings.remove(vm);
		vmLocs.put(vm, node);
		hosted.get(node).add(vm);
		return true;
  }

  @Override
	public boolean setWaiting(VirtualMachine vm) {
		if (isWaiting(vm)) {
			return false;
		}
		Node hoster = vmLocs.remove(vm);
		if (hoster != null) {
			hosted.get(hoster).remove(vm);
		}
		waitings.add(vm);
		return true;
  }

  @Override
	public boolean remove(VirtualMachine vm) {
		if (waitings.remove(vm)) {
			return true;
		}
		Node hoster = vmLocs.remove(vm);
		if (hoster != null) {
			hosted.get(hoster).remove(vm);
			return true;
		}
		return false;
  }

  @Override
	public boolean setOnline(Node node) {
		if (isOnline(node)) {
			return false;
		}
		offlines.remove(node.getName());
		hosted.put(node, new HashSet<>());
		return true;
  }

  @Override
  public boolean setOffline(Node node) {
		if (isOffline(node)) {
			return false;
		}
		Set<VirtualMachine> vms = hosted.remove(node);
		if (vms != null) {
			for (VirtualMachine vm : vms) {
				vmLocs.remove(vm);
				waitings.add(vm);
			}
		}
		offlines.add(node);
		return true;
  }

  @Override
  public boolean remove(Node n) {
		// if node offline : remove it from offlines, then it's ok
		if (offlines.remove(n)) {
			return true;
		}
		Set<VirtualMachine> vms = hosted.remove(n);
		if (vms != null) {
			for (VirtualMachine vm : vms) {
				vmLocs.remove(vm);
				waitings.add(vm);
			}
			return true;
		}
		return false;
  }

  @Override
  public Stream<VirtualMachine> getHosted(Node n) {
		return hosted.get(n).stream();
  }

  @Override
  public Node getLocation(VirtualMachine vm) {
		return vmLocs.get(vm);
  }

	@Override
	public void replace(VirtualMachine vm, VirtualMachine newVM) {
		Node hoster = vmLocs.remove(vm);
		if (hoster == null) {
			waitings.remove(vm);
			waitings.add(newVM);
		} else {
			Set<VirtualMachine> set = hosted.get(hoster);
			set.remove(vm);
			set.add(newVM);
			vmLocs.put(newVM, hoster);
		}
	}

	@Override
	public void replace(Node oldNode, Node newNode) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
