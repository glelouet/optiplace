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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.emn.optiplace.configuration.parser.ConfigurationFiler;
import fr.emn.optiplace.configuration.resources.MappedResourceSpecification;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;


/**
 * Default implementation of Configuration. The elements are stored in
 * linkedHashMap and LinkedHashSet to ensure the same order when iterating
 * <p>
 * see {@link ConfigurationFiler} to read or write it on file
 * </p>
 *
 * @author Guillaume Le Louët
 */
public class SimpleConfiguration implements Configuration {

	private final Set<Node> offlines = new LinkedHashSet<>();

	private final Map<Node, Set<VM>> hosted = new LinkedHashMap<>();

	private final LinkedHashMap<Extern, Set<VM>> externs = new LinkedHashMap<>();

	private final Set<VM> waitings = new LinkedHashSet<>();

	private final Map<VM, Node> vmLocs = new LinkedHashMap<>();

	private final Map<VM, Node> migrations = new LinkedHashMap<>();

	public SimpleConfiguration(String... resources) {
		if (resources == null || resources.length == 0) {} else {
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
				return waitings.size();
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
		if (node2.equals(migrations.get(vm))) {
			migrations.remove(vm);
		}
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
		migrations.remove(vm);
		return true;
	}

	/**
	 *
	 */
	@Override
	public Node getMigrationTarget(VM v) {
		Node ret = migrations.get(v);
		if (ret == getLocation(v)) {
			ret = null;
		}
		return ret;
	}

	@Override
	public void setMigrationTarget(VM vm, Node n) {
		if (vm == null || !vmLocs.containsKey(vm)) {
			return;
		}
		if (n == null || n.equals(vmLocs.get(vm))) {
			migrations.remove(vm);
		} else {
			migrations.put(vm, n);
		}
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
			ResourceSpecification[] specs = this.resources.values().toArray(new ResourceSpecification[this.resources.size()]);
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
		migrations.remove(vm);
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
			ResourceSpecification[] specs = this.resources.values().toArray(new ResourceSpecification[this.resources.size()]);
			for (int i = 0; i < specs.length && i < resources.length; i++) {
				specs[i].toCapacities().put(n, resources[i]);
			}
		}
		return n;
	}

	@Override
	public Node addOffline(String name, int... resources) {
		Node ret = addOnline(name, resources);
		setOffline(ret);
		return ret;
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
				migrations.remove(vm);
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
				migrations.remove(vm);
			}
			return true;
		}
		return false;
	}

	@Override
	public Stream<VM> getHosted(Node n) {
		Set<VM> s = hosted.get(n);
		return s != null ? s.stream() : Stream.empty();
	}

	@Override
	public Node getLocation(VM vm) {
		return vmLocs.get(vm);
	}

	@Override
	public Stream<Node> getOnlines(Predicate<Set<VM>> pred) {
		return hosted.entrySet().stream().filter(e -> pred.test(Collections.unmodifiableSet(e.getValue())))
		    .map(Entry<Node, Set<VM>>::getKey);
	}

	// the site i is at pos i-1
	ArrayList<Set<Node>> sites = new ArrayList<>();

	protected void removeNodesFromSites(Collection<Node> c) {
		for (Set<Node> set : sites) {
			set.removeAll(c);
		}
	}

	@Override
	public int addSite(Node... nodes) {
		Set<Node> site = new HashSet<Node>(Arrays.asList(nodes));
		removeNodesFromSites(site);
		sites.add(site);
		return sites.size();
	}

	@Override
	public int addSite(int siteIdx, Node... nodes) {
		if (siteIdx == 0) {
			removeNodesFromSites(Arrays.asList(nodes));
			return siteIdx;
		}
		if (siteIdx > sites.size()) {
			return addSite(nodes);
		}
		List<Node> l = Arrays.asList(nodes);
		removeNodesFromSites(l);
		Set<Node> s = sites.get(siteIdx - 1);
		s.addAll(l);
		return siteIdx;
	}

	@Override
	public int nbSites() {
		return sites.size() + 1;
	}

	@Override
	public int getSite(Node n) {
		for (int i = 0; i < sites.size(); i++) {
			if (sites.get(i).contains(n)) {
				return i + 1;
			}
		}
		return 0;
	}

	@Override
	public Stream<Node> getSite(int idx) {
		if (idx == 0) {
			return Configuration.super.getSite(idx);
		}
		if (idx > sites.size()) {
			return Stream.empty();
		} else {
			return sites.get(idx - 1).stream();
		}
	}

	protected HashMap<String, Integer> aliasesToIndex = new HashMap<>();

	@Override
	public Set<String> area(int siteIdx, String... aliases) {
		if (aliases != null) {
			for (String s : aliases) {
				if (!aliasesToIndex.containsKey(s)) {
					aliasesToIndex.put(s, siteIdx);
				}
			}
		}
		return aliasesToIndex.entrySet().parallelStream().filter(e -> e.getValue() == siteIdx).map(e -> e.getKey())
		    .collect(Collectors.toSet());
	}

	@Override
	public int area(String alias) {
		return aliasesToIndex.getOrDefault(alias, -1);
	}

	@Override
	public String toString() {
		return "onlines : " + hosted + "\nofflines : " + offlines + "\nwaitings : " + waitings + "\nmigrations : "
		    + migrations + "\nsites : " + sites + "\nresources : "
		    + resources.entrySet().stream().map(e -> " " + e.getValue()).reduce("", (s, t) -> s + "\n" + t);
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj == null || obj.getClass() != SimpleConfiguration.class) {
			return false;
		}
		SimpleConfiguration o = (SimpleConfiguration) obj;
		if (!vmLocs.equals(o.vmLocs)) {
			return false;
		}
		if (!offlines.equals(o.offlines)) {
			return false;
		}
		if (!waitings.equals(o.waitings)) {
			return false;
		}
		if (!resources.equals(o.resources)) {
			return false;
		}
		if (!migrations.equals(o.migrations)) {
			return false;
		}
		if (!sites.equals(o.sites)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return vmLocs.hashCode() + offlines.hashCode() + waitings.hashCode() + resources.hashCode() + migrations.hashCode()
		    + sites.hashCode();
	}

	@Override
	public void addExtern(Extern e) {
		if (!externs.containsKey(e)) {
			externs.put(e, new LinkedHashSet<>());
		}
	}

	@Override
	public Stream<Extern> getExterns() {
		return externs.keySet().stream();
	}

	@Override
	public Stream<VM> getExterned() {
		return externs.values().stream().flatMap(Set::stream);
	}

	@Override
	public Extern getExtern(VM vm) {
		for (Entry<Extern, Set<VM>> e : externs.entrySet()) {
			if (e.getValue().contains(vm)) {
				return e.getKey();
			}
		}
		return null;
	}

	@Override
	public boolean hasExtern(Extern e) {
		return externs.containsKey(e);
	}
}
