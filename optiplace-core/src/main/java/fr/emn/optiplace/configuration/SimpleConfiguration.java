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

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
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

	private final LinkedHashMap<Node, Set<VM>> nodesVM = new LinkedHashMap<>();

	private final LinkedHashMap<Extern, Set<VM>> externVM = new LinkedHashMap<>();

	private final Set<VM> waitings = new LinkedHashSet<>();

	private final HashMap<String, ManagedElement> nameToElement = new HashMap<>();

	/** VM to the host/extern it is hosted on. */
	private final Map<VM, VMHoster> vmHoster = new LinkedHashMap<>();

	/** VM to the target is is migrating to. */
	private final Map<VM, VMHoster> vmMigration = new LinkedHashMap<>();

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
		return nodesVM.keySet().stream();
	}

	@Override
	public Stream<Node> getOfflines() {
		return offlines.stream();
	}

	@Override
	public int nbNodes(NODESTATES state) {
		if (state == null) {
			return offlines.size() + nodesVM.size();
		}
		switch (state) {
			case OFFLINE:
				return offlines.size();
			case ONLINE:
				return nodesVM.size();
			default:
				throw new UnsupportedOperationException();
		}
	}

	@Override
	public Stream<VM> getRunnings() {
		return vmHoster.keySet().stream();
	}

	@Override
	public Stream<VM> getWaitings() {
		return waitings.stream();
	}

	@Override
	public int nbVMs(VMSTATES state) {
		if (state == null) {
			return vmHoster.size() + waitings.size();
		}
		switch (state) {
			case RUNNING:
				return nodesVM.values().parallelStream().mapToInt(Set::size).sum();
			case WAITING:
				return waitings.size();
			case EXTERN:
				return externVM.values().parallelStream().mapToInt(Set::size).sum();
			default:
				throw new UnsupportedOperationException();
		}
	}

	@Override
	public boolean isOnline(Node n) {
		return nodesVM.containsKey(n);
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
	public boolean hasExtern(Extern e) {
		return externVM.containsKey(e);
	}

	@Override
	public boolean isRunning(VM vm) {
		return nodesVM.containsKey(getLocation(vm));
	}

	@Override
	public boolean isExterned(VM vm) {
		return externVM.containsKey(getLocation(vm));
	}

	@Override
	public boolean setHost(VM vm, VMHoster hoster) {
		if (vm == null || hoster == null || hoster.equals(vmHoster.get(vm))) {
			return false;
		}
		waitings.remove(vm);
		vmHoster.put(vm, hoster);
		if (hoster.equals(vmMigration.get(vm))) {
			vmMigration.remove(vm);
		}

		if (hoster instanceof Node) {
			setOnline((Node) hoster);
			nodesVM.get(hoster).add(vm);
		} else
		  if (hoster instanceof Extern) {
			externVM.get(hoster).add(vm);
		}
		return true;
	}

	@Override
	public boolean setWaiting(VM vm) {
		if (isWaiting(vm)) {
			return false;
		}
		VMHoster hoster = vmHoster.remove(vm);
		if (hoster != null) {
			if (hoster instanceof Node) {
				nodesVM.get(hoster).remove(vm);
			} else
			  if (hoster instanceof Extern) {
				externVM.get(hoster).remove(vm);
			}
		}
		waitings.add(vm);
		vmMigration.remove(vm);
		return true;
	}

	/**
	 *
	 */
	@Override
	public VMHoster getMigTarget(VM v) {
		VMHoster ret = vmMigration.get(v);
		if (ret == getLocation(v)) {
			ret = null;
		}
		return ret;
	}

	@Override
	public void setMigTarget(VM vm, VMHoster h) {
		if (vm == null || !vmHoster.containsKey(vm)) {
			return;
		}
		if (h == null || h.equals(vmHoster.get(vm))) {
			vmMigration.remove(vm);
		} else {
			vmMigration.put(vm, h);
		}
	}

	@Override
	public VM addVM(String vmName, VMHoster host, int... resources) {
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
		return setWaiting(vm) && waitings.remove(vm) || waitings.remove(vm);
	}

	@Override
	public boolean setOnline(Node node2) {
		if (isOnline(node2)) {
			return false;
		}
		offlines.remove(node2.getName());
		nodesVM.put(node2, new LinkedHashSet<>());
		return true;
	}

	@Override
	public Node addOnline(String name, int... resources) {
		ManagedElement contained = getElementByName(name);
		if (contained != null) {
			if (contained instanceof Node) {
				return (Node) contained;
			} else {
				return null;
			}
		} else {
			Node n = new Node(name);
			nameToElement.put(name, n);
			setOnline(n);
			if (resources != null && resources.length > 0) {
				ResourceSpecification[] specs = this.resources.values()
				    .toArray(new ResourceSpecification[this.resources.size()]);
				for (int i = 0; i < specs.length && i < resources.length; i++) {
					specs[i].toCapacities().put(n, resources[i]);
				}
			}
			return n;
		}
	}

	@Override
	public Node addOffline(String name, int... resources) {
		Node ret = addOnline(name, resources);
		if (ret == null) {
			return null;
		}
		setOffline(ret);
		return ret;
	}

	@Override
	public boolean setOffline(Node node2) {
		if (isOffline(node2)) {
			return false;
		}
		Set<VM> vms = nodesVM.remove(node2);
		if (vms != null) {
			for (VM vm : vms) {
				vmHoster.remove(vm);
				waitings.add(vm);
				vmMigration.remove(vm);
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
		Set<VM> vms = nodesVM.remove(n);
		if (vms != null) {
			for (VM vm : vms) {
				vmHoster.remove(vm);
				waitings.add(vm);
				vmMigration.remove(vm);
			}
			return true;
		}
		return false;
	}

	@Override
	public Stream<VM> getHosted(VMHoster n) {
		Set<VM> s = nodesVM.get(n);
		return s != null ? s.stream() : Stream.empty();
	}

	@Override
	public VMHoster getLocation(VM vm) {
		return vmHoster.get(vm);
	}

	@Override
	public Stream<Node> getOnlines(Predicate<Set<VM>> pred) {
		return nodesVM.entrySet().stream().filter(e -> pred.test(Collections.unmodifiableSet(e.getValue())))
		    .map(Entry<Node, Set<VM>>::getKey);
	}

	// the site i is at pos i-1
	LinkedHashMap<Site, Set<Node>> sitesToNodes = new LinkedHashMap<>();

	protected void removeNodesFromSites(Collection<Node> c) {
		for (Set<Node> set : sitesToNodes.values()) {
			set.removeAll(c);
		}
	}

	@Override
	public Site addSite(String siteName, Node... nodes) {
		if (siteName == null) {
			removeNodesFromSites(Arrays.asList(nodes));
			return null;
		}
		Site site = null;
		ManagedElement contained = getElementByName(siteName);
		if (contained != null) {
			if (contained instanceof Site) {
				site = (Site) contained;
			} else {
				return null;
			}
		} else {
			site = new Site(siteName);
			nameToElement.put(siteName, site);
			sitesToNodes.put(site, new HashSet<>());
		}
		List<Node> l = Arrays.asList(nodes);
		removeNodesFromSites(l);
		Set<Node> s = sitesToNodes.get(site);
		s.addAll(l);
		return site;
	}

	@Override
	public int nbSites() {
		return sitesToNodes.size();
	}

	@Override
	public Site getSite(Node n) {
		if (n == null) {
			return null;
		}
		for (Entry<Site, Set<Node>> e : sitesToNodes.entrySet()) {
			if (e.getValue().contains(n)) {
				return e.getKey();
			}
		}
		return null;
	}

	@Override
	public Stream<Site> getSites() {
		return sitesToNodes.keySet().stream();
	}

	@Override
	public Stream<Node> getNodes(Site site) {
		if (site == null) {
			return getNodes().filter(n -> getSite(n) == null);
		}
		Set<Node> set = sitesToNodes.get(site);
		if (set != null) {
			return set.stream();
		}
		return Stream.empty();
	}

	@Override
	public String toString() {
		return "onlines : " + nodesVM + "\nofflines : " + offlines + "\nwaitings : " + waitings + "\nmigrations : "
		    + vmMigration + "\nsites : " + sitesToNodes + "\nresources : "
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
		if (!vmHoster.equals(o.vmHoster)) {
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
		if (!vmMigration.equals(o.vmMigration)) {
			return false;
		}
		if (!sitesToNodes.equals(o.sitesToNodes)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return vmHoster.hashCode() + offlines.hashCode() + waitings.hashCode() + resources.hashCode()
		    + vmMigration.hashCode() + sitesToNodes.hashCode();
	}

	@Override
	public ManagedElement getElementByName(String name) {
		return nameToElement.get(name);
	}

	@Override
	public Extern addExtern(String name) {
		ManagedElement contained = getElementByName(name);
		if (contained != null) {
			if (contained instanceof Extern) {
				return (Extern) contained;
			} else {
				return null;
			}
		} else {
			Extern ret = new Extern(name);
			externVM.put(ret, new LinkedHashSet<>());
			nameToElement.put(name, ret);
			return ret;
		}
	}

	@Override
	public Stream<Extern> getExterns() {
		return externVM.keySet().stream();
	}

	@Override
	public Stream<VM> getExterned() {
		return externVM.values().stream().flatMap(Set::stream);
	}

	@Override
	public Extern getExternHost(VM vm) {
		for (Entry<Extern, Set<VM>> e : externVM.entrySet()) {
			if (e.getValue().contains(vm)) {
				return e.getKey();
			}
		}
		return null;
	}
}
