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

import fr.emn.optiplace.configuration.resources.MappedResourceSpecification;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/**
 * Default implementation of Configuration. The elements are stored in
 * linkedHashMap and LinkedHashSet to ensure the same order when iterating
 *
 * @author Guillaume Le Louët
 */
public class SimpleConfiguration implements Configuration {

	private final Set<Node> offlines = new LinkedHashSet<>();

	private final Map<Node, Set<VM>> hosted = new LinkedHashMap<>();

	private final Set<VM> waitings = new LinkedHashSet<>();

	private final Map<VM, Node> vmLocs = new LinkedHashMap<>();

	public SimpleConfiguration(String... resources) {
		if (resources == null || resources.length == 0) {
		} else {
			for (String r : resources) {
				this.resources.put(r, new MappedResourceSpecification(r));
			}
		}
	}

	protected LinkedHashMap<String, ResourceSpecification> resources = new LinkedHashMap<String, ResourceSpecification>();

	@Override
	public LinkedHashMap<String, ResourceSpecification> resources() {
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
	public Stream<VM> getRunnings() {
		return vmLocs.keySet().stream();
	}

	@Override
	public Stream<VM> getWaitings() {
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
	public int nbHosted(Node host) {
		Set<VM> vms = hosted.get(host);
		return vms == null ? 0 : vms.size();
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
	public boolean isRunning(VM vm) {
		return vmLocs.containsKey(vm);
	}

	@Override
	public boolean isWaiting(VM vm) {
		return waitings.contains(vm);
	}

	@Override
	public boolean setHost(VM vm, Node node2) {
		if (vm == null || node2 == null || node2.equals(vmLocs.get(vm))) {
			return false;
		}
		setOnline(node2);
		waitings.remove(vm);
		vmLocs.put(vm, node2);
		hosted.get(node2).add(vm);
		return true;
	}

	@Override
	public boolean setWaiting(VM vm) {
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
	public VM addVM(String vmName, Node host, int... resources) {
		VM vm = new VM(vmName);
		if (host == null) {
			// we requested VM to be waiting.
			setWaiting(vm);
		} else {
			setHost(vm, host);
		}
		if (resources != null && resources.length > 0) {
			ResourceSpecification[] specs = this.resources.values().toArray(
					new ResourceSpecification[this.resources.size()]);
			for (int i = 0; i < specs.length && i < resources.length; i++) {
				specs[i].toUses().put(vm, resources[i]);
			}
		}
		return vm;
	}

	@Override
	public boolean remove(VM vm) {
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
	public boolean setOnline(Node node2) {
		if (isOnline(node2)) {
			return false;
		}
		offlines.remove(node2.getName());
		hosted.put(node2, new LinkedHashSet<>());
		return true;
	}

	@Override
	public Node addOnline(String name, int... resources) {
		Node n = new Node(name);
		setOnline(n);
		if (resources != null && resources.length > 0) {
			ResourceSpecification[] specs = this.resources.values().toArray(
					new ResourceSpecification[this.resources.size()]);
			for (int i = 0; i < specs.length && i < resources.length; i++) {
				specs[i].toCapacities().put(n, resources[i]);
			}
		}
		return n;
	}

	@Override
	public boolean setOffline(Node node2) {
		if (isOffline(node2)) {
			return false;
		}
		Set<VM> vms = hosted.remove(node2);
		if (vms != null) {
			for (VM vm : vms) {
				vmLocs.remove(vm);
				waitings.add(vm);
			}
		}
		offlines.add(node2);
		return true;
	}

	@Override
	public boolean remove(Node n) {
		// if node offline : remove it from offlines, then it's ok
		if (offlines.remove(n)) {
			return true;
		}
		Set<VM> vms = hosted.remove(n);
		if (vms != null) {
			for (VM vm : vms) {
				vmLocs.remove(vm);
				waitings.add(vm);
			}
			return true;
		}
		return false;
	}

	@Override
	public Stream<VM> getHosted(Node n) {
		return hosted.get(n).stream();
	}

	@Override
	public Node getLocation(VM vm) {
		return vmLocs.get(vm);
	}

	@Override
	public void replace(VM vm, VM newVM) {
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
			Set<VM> set = hosted.get(hoster);
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
		Set<VM> vms = hosted.remove(oldNode);
		if (vms == null) {
			if (offlines.remove(oldNode)) {
				offlines.add(newNode);
			} else {
				// the node had a null set of VMs, and was not offline : was not
				// present.
			}
		} else {
			hosted.put(newNode, vms);
			for (VM vm : vms) {
				vmLocs.put(vm, newNode);
			}
		}
	}

	@Override
	public Stream<Node> getOnlines(Predicate<Set<VM>> pred) {
		return hosted.entrySet().stream()
				.filter(e -> pred.test(Collections.unmodifiableSet(e.getValue())))
				.map(Entry<Node, Set<VM>>::getKey);
	}

	@Override
	public String toString() {
		return "onlines : "
				+ hosted
				+ "\nofflines : "
				+ offlines
				+ "\nwaitings : "
				+ waitings
				+ "\nresources : "
				+ resources.entrySet().stream().map(e -> " " + e.getValue())
						.reduce("", (s, t) -> s + "\n" + t);
	}
}
