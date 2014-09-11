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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/**
 * Default implementation of Configuration. The elements are stored in
 * linkedHashMap and LinkedHashSet to ensure the same order when iterating
 *
 * @author Guillaume Le Louët
 */
public class SimpleConfiguration implements Configuration {

	private final Set<Node> offlines = new LinkedHashSet<>();

	private final Map<Node, Set<VirtualMachine>> hosted = new LinkedHashMap<>();

	private final Set<VirtualMachine> waitings = new LinkedHashSet<>();

	private final Map<VirtualMachine, Node> vmLocs = new LinkedHashMap<>();

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
	public int nbNodes(NODESTATES state) {
		if (state == null) {
			return offlines.size() + hosted.size();
		}
		switch (state) {
		case OFFLINE:
			return offlines.size();
		case ONLINE:
			return hosted.size();
		default:
			throw new UnsupportedOperationException();
		}
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
	public int nbVMs(VMSTATES state) {
		if (state == null) {
			return vmLocs.size() + waitings.size();
		}
		switch (state) {
		case RUNNING:
			return vmLocs.size();
		case WAITING:
			return hosted.size();
		default:
			throw new UnsupportedOperationException();
		}
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
		hosted.put(node, new LinkedHashSet<>());
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
		if (vm == newVM || vm == null || newVM == null) {
			return;
		}
		Node hoster = vmLocs.remove(vm);
		if (hoster == null) {
			if (waitings.remove(vm)) {
				waitings.add(newVM);
			} else {
				// the VM had no hoster and was not waiting : it was not present.
			}
		} else {
			Set<VirtualMachine> set = hosted.get(hoster);
			set.remove(vm);
			set.add(newVM);
			vmLocs.put(newVM, hoster);
		}
	}

	@Override
	public void replace(Node oldNode, Node newNode) {
		if (oldNode == newNode || oldNode == null || newNode == null) {
			return;
		}
		Set<VirtualMachine> vms = hosted.remove(oldNode);
		if (vms == null) {
			if (offlines.remove(oldNode)) {
				offlines.add(newNode);
			} else {
				// the node had a null set of VMs, and was not offline : was not
				// present.
			}
		} else {
			hosted.put(newNode, vms);
			for (VirtualMachine vm : vms) {
				vmLocs.put(vm, newNode);
			}
		}
	}

	@Override
	public Stream<Node> getOnlines(Predicate<Set<VirtualMachine>> pred) {
		return hosted.entrySet().stream()
				.filter(e -> pred.test(Collections.unmodifiableSet(e.getValue())))
				.map(Entry<Node, Set<VirtualMachine>>::getKey);
	}
}
