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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class Configuration implements IConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

	private final LinkedHashMap<Computer, Set<VM>> computersVM = new LinkedHashMap<>();

	private final LinkedHashMap<Extern, Set<VM>> externVM = new LinkedHashMap<>();

	private final Set<VM> waitings = new LinkedHashSet<>();

	/**
	 * elements present in the configuration. The element names are non case
	 * sensitive.
	 */
	private final HashMap<String, ManagedElement> nameToElement = new HashMap<>();

	/** VM to the host/extern it is hosted on. */
	private final Map<VM, VMLocation> vmHoster = new LinkedHashMap<>();

	/** VM to the target is is migrating to. */
	private final Map<VM, VMLocation> vmMigration = new LinkedHashMap<>();

	protected LinkedHashMap<String, ResourceSpecification> resources = new LinkedHashMap<>();

	public Configuration(String... resources) {
		if (resources == null || resources.length == 0) {
		} else {
			for (String r : resources) {
				this.resources.put(r, new MappedResourceSpecification(r));
			}
		}
	}

	@Override
	public Configuration clone() {
		Configuration other = new Configuration();
		externsTags.forEach((e, v) -> other.externsTags.put(e, new LinkedHashSet<>(v)));
		externVM.forEach((e, v) -> other.externVM.put(e, new LinkedHashSet<>(v)));
		nameToElement.forEach((e, v) -> other.nameToElement.put(e, v));
		computersTags.forEach((e, v) -> other.computersTags.put(e, new LinkedHashSet<>(v)));
		computersVM.forEach((e, v) -> other.computersVM.put(e, new LinkedHashSet<>(v)));
		resources.forEach((e, v) -> other.resources.put(e, v.clone()));

		sitesTags.forEach((e, v) -> other.sitesTags.put(e, new LinkedHashSet<>(v)));
		sitesToHosters.forEach((e, v) -> other.sitesToHosters.put(e, new LinkedHashSet<>(v)));
		vmHoster.forEach((e, v) -> other.vmHoster.put(e, v));
		vmMigration.forEach((e, v) -> other.vmMigration.put(e, v));
		vmsTags.forEach((e, v) -> other.vmsTags.put(e, new LinkedHashSet<>(v)));
		other.waitings.addAll(waitings);
		return other;
	}

	@Override
	public LinkedHashMap<String, ResourceSpecification> resources() {
		return resources;
	}

	@Override
	public ResourceSpecification resource(String name) {
		ResourceSpecification ret = resources.get(name);
		if (ret == null) {
			ret = new MappedResourceSpecification(name);
			resources.put(name, ret);
		}
		return ret;
	}

	@Override
	public Stream<Computer> getComputers() {
		return computersVM.keySet().stream();
	}

	@Override
	public int nbComputers() {
		return computersVM.size();
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
			int ret = vmHoster.size() + waitings.size();
			return ret;
		}
		switch (state) {
		case RUNNING:
			return computersVM.values().parallelStream().mapToInt(Set::size).sum();
		case WAITING:
			return waitings.size();
		case EXTERN:
			return externVM.values().parallelStream().mapToInt(Set::size).sum();
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public boolean hasComputer(Computer n) {
		return computersVM.containsKey(n);
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
		return computersVM.containsKey(getLocation(vm));
	}

	@Override
	public boolean isExterned(VM vm) {
		return externVM.containsKey(getLocation(vm));
	}

	@Override
	public boolean setHost(VM vm, VMLocation hoster) {
		if (vm == null || hoster != null && hoster.equals(vmHoster.get(vm))) {
			return false;
		}
		if (hoster == null) {
			return setWaiting(vm);
		}
		waitings.remove(vm);
		vmHoster.put(vm, hoster);
		if (hoster.equals(vmMigration.get(vm))) {
			vmMigration.remove(vm);
		}

		if (hoster instanceof Computer) {
			computersVM.get(hoster).add(vm);
		} else if (hoster instanceof Extern) {
			externVM.get(hoster).add(vm);
		} else {
			logger.warn("can't handle hoster " + hoster + " of class " + hoster.getClass(), new Exception());
		}
		return true;
	}

	@Override
	public boolean setWaiting(VM vm) {
		if (isWaiting(vm)) {
			return false;
		}
		VMLocation hoster = vmHoster.remove(vm);
		if (hoster != null) {
			if (hoster instanceof Computer) {
				computersVM.get(hoster).remove(vm);
			} else if (hoster instanceof Extern) {
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
	public VMLocation getMigTarget(VM v) {
		VMLocation ret = vmMigration.get(v);
		if (ret == getLocation(v)) {
			ret = null;
		}
		return ret;
	}

	@Override
	public Set<VM> getMigratingVMs() {
		return vmMigration.keySet();
	}

	@Override
	public void setMigTarget(VM vm, VMLocation h) {
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
	public VM addVM(String vmName, VMLocation host, int... resources) {
		if (vmName == null) {
			return null;
		}
		VM ret;
		try {
			ret = getElementByName(vmName, VM.class);
			if (ret == null) {
				ret = new VM(vmName);
				nameToElement.put(vmName.toLowerCase(), ret);
			}
		} catch (ClassCastException e) {
			return null;
		}
		if (host == null) {
			// we requested VM to be waiting.
			setWaiting(ret);
		} else {
			setHost(ret, host);
		}
		if (resources != null) {
			ResourceSpecification[] specs = this.resources.values().toArray(new ResourceSpecification[this.resources.size()]);
			for (int i = 0; i < specs.length; i++) {
				specs[i].use(ret, i < resources.length ? resources[i] : 0);
			}
		}
		return ret;
	}

	@Override
	public boolean remove(VM vm) {
		if (vm == null) {
			return false;
		}
		// we ensure we have a VM of same name.
		VM vm2 = null;
		try {
			vm2 = getElementByName(vm.getName(), VM.class);
		} catch (ClassCastException cce) {
			logger.trace("while removing VM " + vm.getName(), cce);
			return false;
		}
		if (vm2 == null) {
			return false;
		}
		setWaiting(vm2);
		waitings.remove(vm2);
		VM rem = vm2;
		vmsTags.values().stream().forEach(s -> s.remove(rem));
		forgetElement(vm2);
		return true;
	}

	@Override
	public Computer addComputer(String name, int... resources) {
		if (name == null) {
			return null;
		}
		Computer ret;
		try {
			ret = getElementByName(name, Computer.class);
			if (ret == null) {
				ret = new Computer(name);
				nameToElement.put(name.toLowerCase(), ret);
				computersVM.put(ret, new LinkedHashSet<>());
			}
		} catch (ClassCastException cce) {
			return null;
		}
		if (resources != null) {
			ResourceSpecification[] specs = this.resources.values().toArray(new ResourceSpecification[this.resources.size()]);
			for (int i = 0; i < specs.length && i < resources.length; i++) {
				specs[i].capacity(ret, i < resources.length ? resources[i] : 0);
			}
		}
		return ret;
	}

	@Override
	public boolean removeVMs(Computer n) {
		Set<VM> vms = computersVM.remove(n);
		if (vms != null) {
			for (VM vm : vms) {
				vmHoster.remove(vm);
				waitings.add(vm);
				vmMigration.remove(vm);
			}
		}
		return true;
	}

	@Override
	public boolean remove(Computer n) {
		if (n == null) {
			return false;
		}
		// we ensure we have a Computer of same name.
		try {
			n = getElementByName(n.getName(), Computer.class);
			if (n == null) {
				return false;
			}
		} catch (ClassCastException cce) {
			return false;
		}
		removeVMs(n);
		computersVM.remove(n);
		Computer rem = n;
		computersTags.values().stream().forEach(s -> s.remove(rem));
		forgetElement(n);
		return true;
	}

	@Override
	public Stream<VM> getHosted(VMLocation n) {
		Set<VM> s = computersVM.get(n);
		if (s == null) {
			s = externVM.get(n);
		}
		return s != null ? s.stream() : Stream.empty();
	}

	@Override
	public VMLocation getLocation(VM vm) {
		return vmHoster.get(vm);
	}

	@Override
	public Stream<Computer> getComputers(Predicate<Set<VM>> pred) {
		return computersVM.entrySet().stream().filter(e -> pred.test(Collections.unmodifiableSet(e.getValue())))
				.map(Entry<Computer, Set<VM>>::getKey);
	}

	LinkedHashMap<Site, Set<VMLocation>> sitesToHosters = new LinkedHashMap<>();

	protected void removeHostersFromSites(Collection<VMLocation> c) {
		for (Set<VMLocation> set : sitesToHosters.values()) {
			set.removeAll(c);
		}
	}

	@Override
	public Site addSite(String siteName, VMLocation... hosters) {
		List<VMLocation> l = hosters == null ? Collections.emptyList() : Arrays.asList(hosters);
		removeHostersFromSites(l);
		if (siteName == null) {
			return null;
		}
		Site ret;
		Set<VMLocation> set = null;
		try {
			ret = getElementByName(siteName, Site.class);
			if (ret == null) {
				ret = new Site(siteName);
				nameToElement.put(siteName.toLowerCase(), ret);
				set = new LinkedHashSet<>();
				sitesToHosters.put(ret, set);
			} else {
				set = sitesToHosters.get(ret);
			}
		} catch (ClassCastException e) {
			return null;
		}
		set.addAll(l);
		return ret;
	}

	@Override
	public int nbSites() {
		return sitesToHosters.size();
	}

	@Override
	public Site getSite(VMLocation h) {
		if (h == null) {
			return null;
		}
		for (Entry<Site, Set<VMLocation>> e : sitesToHosters.entrySet()) {
			if (e.getValue().contains(h)) {
				return e.getKey();
			}
		}
		return null;
	}

	@Override
	public Stream<Site> getSites() {
		return sitesToHosters.keySet().stream();
	}

	@Override
	public boolean remove(Site site) {
		if (site == null) {
			return false;
		}
		// we ensure we have a site of same name.
		try {
			site = getElementByName(site.getName(), Site.class);
			if (site == null) {
				return false;
			}
		} catch (ClassCastException cce) {
			return false;
		}
		sitesToHosters.remove(site);
		Site rem = site;
		sitesTags.values().stream().forEach(s -> s.remove(rem));
		forgetElement(site);
		return true;
	}

	@Override
	public Stream<VMLocation> getSiteLocations(Site site) {
		if (site == null) {
			return Stream.concat(getComputers().filter(n -> getSite(n) == null),
					getExterns().filter(n -> getSite(n) == null));
		}
		Set<VMLocation> set = sitesToHosters.get(site);
		if (set == null) {
			return Stream.empty();
		}
		return set.stream();
	}

	public String toString(String fieldSeparator) {
		StringBuilder sb = new StringBuilder();
		sb.append("computers : ").append(computersVM);
		if (!externVM.isEmpty()) {
			sb.append(fieldSeparator).append("externs : ").append(externVM);
		}
		if (!waitings.isEmpty()) {
			sb.append(fieldSeparator).append("waitings : ").append(waitings);
		}
		if (!vmMigration.isEmpty()) {
			sb.append(fieldSeparator).append("migrations : ").append(vmMigration);
		}
		if (!sitesToHosters.isEmpty()) {
			sb.append(fieldSeparator).append("sites : ").append(sitesToHosters);
		}
		if (!resources.isEmpty()) {
			sb.append(fieldSeparator).append("resources : ").append(
					resources.entrySet().stream().map(e -> " " + e.getValue()).reduce("", (s, t) -> s + fieldSeparator + t));
		}
		if (!computersTags.isEmpty()) {
			sb.append(fieldSeparator).append("computersTags : ").append(computersTags);
		}
		if (!vmsTags.isEmpty()) {
			sb.append(fieldSeparator).append("vmsTags : ").append(vmsTags);
		}
		if (!sitesTags.isEmpty()) {
			sb.append(fieldSeparator).append("sitesTags : ").append(sitesTags);
		}
		if (!externsTags.isEmpty()) {
			sb.append(fieldSeparator).append("externsTags : ").append(externsTags);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return toString("\n");
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj == null || obj.getClass() != Configuration.class) {
			return false;
		}
		Configuration o = (Configuration) obj;
		if (!vmHoster.equals(o.vmHoster)) {
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
		if (!sitesToHosters.equals(o.sitesToHosters)) {
			return false;
		}
		if (!computersTags.equals(o.computersTags) || !vmsTags.equals(o.vmsTags) || !externsTags.equals(o.externsTags)
				|| !sitesTags.equals(o.sitesTags)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return vmHoster.hashCode() + waitings.hashCode() + resources.hashCode()
				+ vmMigration.hashCode() + sitesToHosters.hashCode() + computersTags.hashCode() + vmsTags.hashCode()
		+ externsTags.hashCode() + sitesTags.hashCode();
	}

	@Override
	public ManagedElement getElementByName(String name) {
		if (name == null) {
			return null;
		}
		return nameToElement.get(name.toLowerCase());
	}

	/**
	 * forget the name to element reference, as well as the name to resources.
	 *
	 * @param me
	 *          an element.
	 */
	public void forgetElement(ManagedElement me) {
		nameToElement.remove(me.getName().toLowerCase());
		for (ResourceSpecification rs : resources.values()) {
			rs.remove(me);
		}
	}

	@Override
	public Extern addExtern(String name, int... resources) {
		if (name == null) {
			return null;
		}
		Extern ret;
		try {
			ret = getElementByName(name, Extern.class);
			if (ret == null) {
				ret = new Extern(name);
				externVM.put(ret, new LinkedHashSet<>());
				nameToElement.put(name.toLowerCase(), ret);
			}
		} catch (ClassCastException e) {
			return null;
		}
		if (resources != null && resources.length > 0) {
			ResourceSpecification[] specs = this.resources.values().toArray(new ResourceSpecification[this.resources.size()]);
			for (int i = 0; i < specs.length && i < resources.length; i++) {
				specs[i].capacity(ret, resources[i]);
			}
		}
		return ret;
	}

	@Override
	public int nbExterns() {
		return externVM.size();
	}

	@Override
	public Stream<Extern> getExterns() {
		return externVM.keySet().stream();
	}

	@Override
	public boolean remove(Extern e) {
		if (e == null) {
			return false;
		}
		// we ensure we have an extern of same name.
		try {
			e = getElementByName(e.getName(), Extern.class);
			if (e == null) {
				return false;
			}
		} catch (ClassCastException cce) {
			return false;
		}
		externVM.remove(e).forEach(v -> {
			vmHoster.remove(v);
			waitings.add(v);
			vmMigration.remove(v);
		});
		Extern rem = e;
		externsTags.values().stream().forEach(s -> s.remove(rem));
		forgetElement(e);
		return true;
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

	//////////////////////////////////////////
	// host tags

	protected HashMap<String, Set<VM>> vmsTags = new LinkedHashMap<>();
	protected HashMap<String, Set<Site>> sitesTags = new LinkedHashMap<>();
	protected HashMap<String, Set<Computer>> computersTags = new LinkedHashMap<>();
	protected HashMap<String, Set<Extern>> externsTags = new LinkedHashMap<>();

	@Override
	public void tagComputer(Computer n, String tag) {
		if (n == null || tag == null) {
			return;
		}
		Set<Computer> s = computersTags.get(tag);
		if (s == null) {
			s = new LinkedHashSet<>();
			computersTags.put(tag, s);
		}
		s.add(n);
	}

	@Override
	public void tagExtern(Extern e, String tag) {
		if (e == null || tag == null) {
			return;
		}
		Set<Extern> s = externsTags.get(tag);
		if (s == null) {
			s = new LinkedHashSet<>();
			externsTags.put(tag, s);
		}
		s.add(e);
	}

	@Override
	public void tagVM(VM v, String tag) {
		if (v == null || tag == null) {
			return;
		}
		Set<VM> s = vmsTags.get(tag);
		if (s == null) {
			s = new LinkedHashSet<>();
			vmsTags.put(tag, s);
		}
		s.add(v);
	}

	@Override
	public void tagSite(Site s, String tag) {
		if (s == null || tag == null) {
			return;
		}
		Set<Site> set = sitesTags.get(tag);
		if (set == null) {
			set = new LinkedHashSet<>();
			sitesTags.put(tag, set);
		}
		set.add(s);
	}

	@Override
	public void delTagComputer(Computer n, String tag) {
		Set<Computer> set = computersTags.get(tag);
		if (set == null) {
			return;
		}
		set.remove(n);
		if (set.isEmpty()) {
			computersTags.remove(tag);
		}
	}

	@Override
	public void delTagExtern(Extern e, String tag) {
		Set<Extern> set = externsTags.get(tag);
		if (set == null) {
			return;
		}
		set.remove(e);
		if (set.isEmpty()) {
			externsTags.remove(tag);
		}
	}

	@Override
	public void delTagVM(VM v, String tag) {
		Set<VM> set = vmsTags.get(tag);
		if (set == null) {
			return;
		}
		set.remove(v);
		if (set.isEmpty()) {
			vmsTags.remove(tag);
		}
	}

	@Override
	public void delTagSite(Site s, String tag) {
		Set<Site> set = sitesTags.get(tag);
		if (set == null) {
			return;
		}
		set.remove(s);
		if (set.isEmpty()) {
			sitesTags.remove(tag);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isTagged(ManagedElement e, String tag) {
		if (e == null || tag == null) {
			return false;
		}
		for (Map<String, Set<? extends ManagedElement>> m : new Map[] { vmsTags, computersTags, externsTags, sitesTags }) {
			Set<? extends ManagedElement> set = m.get(tag);
			if (set != null && set.contains(e)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Stream<VM> getVmsTagged(String tag) {
		Set<VM> set = vmsTags.get(tag);
		return set == null ? Stream.empty() : set.stream();
	}

	@Override
	public Stream<Computer> getComputersTagged(String tag) {
		Set<Computer> set = computersTags.get(tag);
		return set == null ? Stream.empty() : set.stream();
	}

	@Override
	public Stream<Extern> getExternsTagged(String tag) {
		Set<Extern> set = externsTags.get(tag);
		return set == null ? Stream.empty() : set.stream();
	}

	@Override
	public Stream<Site> getSitesTagged(String tag) {
		Set<Site> set = sitesTags.get(tag);
		return set == null ? Stream.empty() : set.stream();
	}

	@Override
	public Stream<String> getTags(ManagedElement me) {
		if (me == null) {
			return Stream.empty();
		}
		if (me instanceof Computer) {
			return computersTags.entrySet().stream().filter(e -> e.getValue().contains(me)).map(e -> e.getKey());
		}
		if (me instanceof VM) {
			return vmsTags.entrySet().stream().filter(e -> e.getValue().contains(me)).map(e -> e.getKey());
		}
		if (me instanceof Extern) {
			return externsTags.entrySet().stream().filter(e -> e.getValue().contains(me)).map(e -> e.getKey());
		}
		if (me instanceof Site) {
			return sitesTags.entrySet().stream().filter(e -> e.getValue().contains(me)).map(e -> e.getKey());
		}
		throw new UnsupportedOperationException(
				"can not stream the tags of the managedelement " + me + " with unsupported class " + me.getClass());
	}

	@Override
	public Stream<String> getVmsTags() {
		return vmsTags.keySet().stream();
	}

	@Override
	public Stream<String> getExternsTags() {
		return externsTags.keySet().stream();
	}

	@Override
	public Stream<String> getComputersTags() {
		return computersTags.keySet().stream();
	}

	@Override
	public Stream<String> getSitesTags() {
		return sitesTags.keySet().stream();
	}

}
