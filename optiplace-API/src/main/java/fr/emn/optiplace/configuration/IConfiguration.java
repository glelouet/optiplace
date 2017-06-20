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

import static java.util.stream.Stream.concat;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/**
 * <p>
 * Computer, Extern and VirtualMachine in a datacenter.<br />
 * VMs are hosted by an online Computer, an extern or waiting, and Computers are
 * either online or offline<
 * </p>
 * <p>
 * The VM and Computer are stored in their add/state change order, meaning two
 * iterations on the streams will return the same exact result, leading to
 * determinism in the exploration.<br />
 * If the list of Computers or VMs is the same for two configuration created in
 * the same way, the position in the list does not mean anything about the age
 * of the Element. Actually, the full list of Computers and VMs is a
 * concatenation of the different state-specific lists of elements.
 * </p>
 *
 * @author Fabien Hermenier
 * @author guillaume Le Louët
 */
public interface IConfiguration extends Cloneable {

	static enum VMSTATES {
		RUNNING, WAITING, EXTERN
	}

	/**
	 * tooling to compare if two configurations have same vms, computers, sites
	 * and externs
	 *
	 * @param first
	 * @param second
	 * @return
	 */
	public static boolean sameElements(IConfiguration first, IConfiguration second) {
		return !first.getComputers().parallel().filter(n -> !second.hasComputer(n)).findAny().isPresent()
				&& !second.getComputers().parallel().filter(n -> !first.hasComputer(n)).findAny().isPresent()
				&& !first.getVMs().parallel().filter(v -> !second.hasVM(v)).findAny().isPresent()
				&& !second.getVMs().parallel().filter(v -> !first.hasVM(v)).findAny().isPresent()
				&& !second.getExterns().parallel().filter(v -> !first.hasExtern(v)).findAny().isPresent()
				&& !first.getExterns().parallel().filter(v -> !second.hasExtern(v)).findAny().isPresent()
				&& !second.getSites().parallel().filter(v -> !first.hasSite(v)).findAny().isPresent()
				&& !first.getSites().parallel().filter(v -> !second.hasSite(v)).findAny().isPresent();
	}

	public IConfiguration clone();

	/**
	 * get an element with given name if it exists
	 *
	 * @param name
	 *          a name to check for an element
	 * @return the element with corresponding name if exists, or null.
	 */
	ManagedElement getElementByName(String name);

	/**
	 * get an element with a name and a given type in the configuration
	 *
	 * @param name
	 *          the name of the element
	 * @param clazz
	 *          the type of the element
	 * @return a corresponding element if exists, null if no element with given
	 *         name, throws an exception if incorrect type
	 * @throws ClassCastException
	 *           if an element with given name is present but does not have a
	 *           subclass of clazz
	 */
	@SuppressWarnings("unchecked")
	default <T extends ManagedElement> T getElementByName(String name, Class<T> clazz) throws ClassCastException {
		if (name == null) {
			return null;
		}
		ManagedElement ret = getElementByName(name);
		if (ret == null) {
			return null;
		}
		if (clazz.isAssignableFrom(ret.getClass())) {
			return (T) ret;
		}
		throw new ClassCastException("cannot cast " + ret + " to " + clazz);
	}

	/**
	 * Get the list of computers that are online.
	 *
	 * @return a Stream of all the computers online in this configuration
	 */
	Stream<Computer> getComputers();

	/**
	 * get a Stream of the computers which are online and whom set of hosted VMs
	 * follow one predicate. The predicate is applied to unmodifiable set, and the
	 * VMs are unmutable so this call can not modify this
	 *
	 * @param pred
	 *          a predicate over the hosted VMs of a Computer with no side-effect
	 *          on the set.
	 * @return The stream of computer following the predicate over their hosted
	 *         VMs
	 */
	Stream<Computer> getComputers(Predicate<Set<VM>> pred);


	/**
	 * get the number of Computer with given state
	 *
	 * @param state
	 *          the state of the computers to consider, or null for all computers
	 * @return the number of computers with given state if not null, or the number
	 *         of computers if null
	 */
	int nbComputers();

	/**
	 * Get the virtual machines that are running.
	 *
	 * @return a Stream of VirtualMachines, may be empty
	 */
	Stream<VM> getRunnings();

	/**
	 * Get the virtual machines that are waiting.
	 *
	 * @return a Stream, may be empty
	 */
	Stream<VM> getWaitings();

	Stream<VM> getExterned();

	/**
	 * Get all the virtual machines involved in the configuration.
	 *
	 * @return a Stream, may be empty
	 */
	default Stream<VM> getVMs() {
		return Stream.concat(getRunnings(), getWaitings());
	}

	/**
	 * get the number of known VMs with given state
	 *
	 * @param state
	 *          the state of the VMs to count, nor null for all VMs
	 * @return the number of VMs with the given state, or of all VMs if state is
	 *         null
	 */
	int nbVMs(VMSTATES state);

	default int nbVMs() {
		return nbVMs(null);
	}

	/**
	 * get the number of VMs running on given computer
	 *
	 * @param host
	 *          the computer to consider
	 * @return the number of vms which are specified running on the computer ;
	 *         null if the computer is not known
	 */
	default long nbHosted(VMLocation host) {
		return getHosted(host).count();
	}

	/**
	 * Test if a computer is online.
	 *
	 * @param n
	 *          the computer
	 * @return true if the computer is online
	 */
	boolean hasComputer(Computer n);

	/**
	 * @param vm
	 *          a VM
	 * @return the state of the VM in the configuration, or null if the VM is not
	 *         known
	 */
	default VMSTATES getState(VM vm) {
		if (isRunning(vm)) {
			return VMSTATES.RUNNING;
		}
		if (isWaiting(vm)) {
			return VMSTATES.WAITING;
		}
		if (isExterned(vm)) {
			return VMSTATES.EXTERN;
		}
		return null;
	}

	/**
	 * Test if a virtual machine is running.
	 *
	 * @param vm
	 *          the virtual machine
	 * @return true if the virtual machine is running
	 */
	boolean isRunning(VM vm);

	/**
	 * Test if a virtual machine is waiting.
	 *
	 * @param vm
	 *          the virtual machine
	 * @return true if the virtual machine is waiting
	 */
	boolean isWaiting(VM vm);

	/**
	 * test is a VM is hosted on an external site
	 *
	 * @param v
	 *          a VM of the configuration
	 * @return true if the VM is hosted on an external site
	 */
	boolean isExterned(VM v);


	/**
	 * check if a VM is already present in this
	 *
	 * @param vm
	 *          a VirtualMachine
	 * @return true if a VM equal to this one already exist
	 */
	default boolean hasVM(VM vm) {
		return isRunning(vm) || isWaiting(vm) || isExterned(vm);
	}

	/**
	 * Set a virtual machine running on a computer. The computer is set online
	 * whatever his previous state was. If the virtual machine is already in a
	 * other location or state in the configuration, it is updated
	 *
	 * @param vm
	 *          the virtual machine
	 * @param hoster
	 *          the managedelement that will host the virtual machine.
	 * @return true if the vm is assigned to the computer and was not before
	 */
	boolean setHost(VM vm, VMLocation hoster);

	/**
	 * Set a virtual machine waiting. If the virtual machine is already in a other
	 * location or state in the configuration, it is updated
	 *
	 * @param vm
	 *          the virtual machine
	 * @return true if the VM state changed
	 */
	boolean setWaiting(VM vm);

	/**
	 * set a migration target for a VM
	 *
	 * @param vm
	 *          the vm migrating
	 * @param n
	 *          null to set no migration, a Computer to specify where the VM is
	 *          migrating
	 */
	void setMigTarget(VM vm, VMLocation n);

	/**
	 * @param vm
	 *          a VM of the center
	 * @return a Computer if that vm is migrating to this computer, or null.
	 */
	VMLocation getMigTarget(VM vm);

	Set<VM> getMigratingVMs();

	default Computer getComputerMig(VM vm) {
		VMLocation h = getMigTarget(vm);
		if (h instanceof Computer) {
			return (Computer) h;
		} else {
			return null;
		}
	}

	default Extern getExternMig(VM vm) {
		VMLocation h = getMigTarget(vm);
		if (h instanceof Extern) {
			return (Extern) h;
		} else {
			return null;
		}
	}

	/**
	 *
	 * @param vm
	 *          a VM of the center
	 * @return true if the VM is migrating to another computer
	 */
	default boolean isMigrating(VM vm) {
		VMLocation target = getMigTarget(vm);
		return target != null && !target.equals(getLocation(vm));
	}

	/**
	 * Add a VM in this; putting it on an Host or waiting. It also set the use of
	 * the VM if specification is given
	 * <p>
	 * the resources array is truncated to meet {@link #resources()} size, and
	 * missing resource specifications are set to 0
	 * </p>
	 *
	 * @param vmName
	 *          the name of the VM
	 * @param host
	 *          the host of the VM or null if it is waiting
	 * @param resources
	 *          the resource use of the VM, corresponding to the resources in
	 *          {@link #resources()}. If null and a VM with given name already
	 *          exists this VM resources are not modified.
	 * @return depending on whether an element with given name exists or not : if
	 *         exists and is a VM the existing VM is modified and then returned ;
	 *         if exist and not a VM returns null ; if not exist then a new VM is
	 *         creatd and returned
	 */
	VM addVM(String vmName, VMLocation host, int... resources);

	/**
	 * Ensure we don't have a VM with given name
	 *
	 * @param vm
	 *          the virtual machine to remove
	 * @return true if this VM was present and is removed
	 */
	boolean remove(VM vm);


	/**
	 * add an online Computer
	 *
	 * @param name
	 *          the name of the computer
	 * @param resources
	 * @return null if a on-computer with give name exists, a new noe if no
	 *         computer with given name exists, or previous computer modified if
	 *         already exists
	 */
	Computer addComputer(String name, int... resources);

	/**
	 * Set a computer offline. If the computer is already in the configuration but
	 * in an another state, it is updated. Any hosted VM state will be set to
	 * waiting.
	 *
	 * @param computer
	 *          the computer
	 * @return true if the computer state changed
	 */
	boolean removeVMs(Computer computer);

	/**
	 * Ensure we don't have a Computer with given name.
	 *
	 * @param n
	 *          the computer to remove
	 * @return true if the Computer was present
	 */
	boolean remove(Computer n);

	/**
	 *
	 * @param hosted
	 *          a vm of the problem
	 * @return the computer hosting the VM or null
	 */
	default Computer getComputerHost(VM hosted) {
		return getState(hosted) == VMSTATES.RUNNING ? (Computer) getLocation(hosted) : null;
	}

	/**
	 *
	 * @param hosted
	 * @return
	 */
	default Extern getExternHost(VM hosted) {
		return getState(hosted) == VMSTATES.EXTERN ? (Extern) getLocation(hosted) : null;
	}

	/**
	 * Get the virtual machines that are running on an hoster.
	 *
	 * @param n
	 *          the computer
	 * @return a set of virtual machines, may be empty, eg if the Computer is not
	 *         present or is offline
	 */
	Stream<VM> getHosted(VMLocation n);

	/**
	 * get the number of VM executed on given hoster
	 *
	 * @param host
	 *          an hoster of the configuration
	 * @return the number of VM that are specified running on given hoster
	 */
	default Stream<VM> getFutureHosted(VMLocation host) {
		return getVMs().filter(v -> host.equals(getFutureLocation(v)));
	}

	/**
	 * Get all the virtual machines running on a set of computers.
	 *
	 * @param ns
	 *          the set of computers
	 * @return a set of virtual machines, may be empty
	 */
	default Stream<VM> getHosted(Set<VMLocation> ns) {
		return ns.stream().map(this::getHosted).reduce(Stream::concat).get();
	}

	/**
	 * Get the location of a virtual machine. This is the Hoster that is executing
	 * it right now.
	 *
	 * @param vm
	 *          the virtual machine
	 * @return the computer or extern hosting the virtual machine or {@code null}
	 *         is the virtual machine is waiting
	 */
	VMLocation getLocation(VM vm);

	/**
	 * get the future location of a VM once the migrations are done.
	 *
	 * @param v
	 *          a VM of the configuration
	 * @return the migration target of the VM or the location if no migration
	 *         present
	 */
	default VMLocation getFutureLocation(VM vm) {
		VMLocation ret = getMigTarget(vm);
		return ret == null ? getLocation(vm) : ret;
	}

	/**
	 * Ensure we have an Extern with given name and given resources
	 *
	 * @param name
	 *          the name of the extern.
	 * @param resources
	 *          optional resources capacity of the extern. can be null, or an
	 *          array of any size.
	 * @return the extern with this name if already present, a new Extern with
	 *         this name if no ManagedElement with this name, or null if another
	 *         non-extern ManagedElement has this name
	 */
	Extern addExtern(String name, int... resources);

	Stream<Extern> getExterns();

	boolean hasExtern(Extern e);

	int nbExterns();

	boolean remove(Extern e);

	default Stream<VMLocation> getLocations() {
		return Stream.concat(getComputers(), getExterns());
	}

	default int nbHosts() {
		return nbExterns() + nbComputers();
	}

	/**
	 *
	 * @return the total number of computers, externs and VMs.
	 */
	default int nbElems() {
		return nbHosts() + nbVMs();
	}

	/**
	 *
	 * @return the total placement solution, as nb(hosts)^nb(vms)
	 */
	default long nbPlacement() {
		return (long) Math.pow(nbHosts(), nbVMs());
	}

	/** get the known list of resources specifications. It can be modified */
	LinkedHashMap<String, ResourceSpecification> resources();

	/**
	 * ensure a resource is present
	 *
	 * @param name
	 *          the name of the resource
	 * @return the present resource specification, or a new one if not present
	 *         yet.
	 */
	ResourceSpecification resource(String name);

	/**
	 * some basic checks to perform on a configuration
	 */
	static enum BasicChecks {

		RUNNING_OR_EXTERN_OR_WAITING {

			@Override
			public boolean check(IConfiguration c) {
				return !c.getWaitings().filter(c::isRunning).findFirst().isPresent();
			}

		},
		HOSTER_HOSTS {

			@Override
			public boolean check(IConfiguration c) {
				return !c.getRunnings().filter(v -> !c.getHosted(c.getLocation(v)).filter(v::equals).findFirst().isPresent())
						.findFirst().isPresent();
			}

		};

		public abstract boolean check(IConfiguration c);
	}

	default boolean checkBasics() {
		for (BasicChecks b : BasicChecks.values()) {
			if (!b.check(this)) {
				LoggerFactory.getLogger(BasicChecks.class).debug("check failed " + b);
				return false;
			}
		}
		return true;
	}

	/**
	 * compute the max number of VM of a kind a Computer can host, based on the
	 * resource capacities of the computer and the use of the VM.
	 *
	 * @param n
	 *          a Computer
	 * @param vm
	 *          a VM to use the resource specifications
	 * @param specs
	 *          the resource specifications from which we can get the capacities
	 *          of the computer and the use of the VM
	 * @return the minimum of capa(n)/use(VM) for each resource of specs
	 */
	public static double maxNBVms(Computer n, VM vm, Stream<ResourceSpecification> specs) {
		return specs.mapToDouble(s -> 1.0 * s.getCapacity(n) / s.getUse(vm)).min().getAsDouble();
	}

	/********************
	 * site management. A site , represented by an index, is a partition of the
	 * computers.<br />
	 * By default one site, the index 0, exists. adding computers to a new site
	 * remove them from the old one.<br />
	 * However, since the index is the order of creation of the sites, deleting
	 * all the computers from a site doesn't remove it.
	 */

	/**
	 * Ensure a list of hosters is added to a Site with given name.
	 *
	 * @param siteName
	 *          the requested site name, or null to add the hosters to no site.
	 * @param hosters
	 *          the hosters to add to the site, or none to create an empty site or
	 *          retrieve an existing site
	 * @return The corresponding site, or null if sitename was null or already in
	 *         use by a non-site element.
	 */
	public Site addSite(String siteName, VMLocation... hosters);

	/**
	 *
	 * @return the number of sites declared in this configuration
	 */
	public int nbSites();

	/**
	 *
	 * @param h
	 *          a Hoster of the configuration
	 * @return the index of the site this hoster belongs to
	 */
	public Site getSite(VMLocation n);

	public Stream<Site> getSites();

	public default boolean hasSite(Site s) {
		if (s == null) {
			return false;
		}
		ManagedElement me = getElementByName(s.getName());
		return me != null && me instanceof Site;
	}

	boolean remove(Site site);

	/**
	 *
	 * @param Site
	 *          a site
	 * @return a stream over the hosters contained in this site. if this site is
	 *         not present, return an empty stream ; if this site is null, return
	 *         the stream of the hosters with no site.
	 */
	public Stream<VMLocation> getSiteLocations(Site site);

	/**
	 * stream over all the managed elements
	 *
	 * @return
	 */
	public default Stream<ManagedElement> getManagedElements() {
		return Stream.concat(Stream.concat(getComputers(), getExterns()), Stream.concat(getVMs(), getSites()));
	}

	///////////////////////////////////
	// host tags

	/**
	 * tag an element of the configuration with a tag
	 *
	 * @param element
	 * @param tag
	 */
	public default void tag(ManagedElement element, String tag) {
		if (element == null) {
			return;
		}
		if (element instanceof Computer) {
			tagComputer((Computer) element, tag);
		} else if (element instanceof Extern) {
			tagExtern((Extern) element, tag);
		} else if (element instanceof VM) {
			tagVM((VM) element, tag);
		} else if (element instanceof Site) {
			tagSite((Site) element, tag);
		} else {
			throw new UnsupportedOperationException(
					"can't tag element " + element + " with unsupported type in switch : " + element.getClass());
		}
	}

	public void tagComputer(Computer n, String tag);

	public void tagExtern(Extern e, String tag);

	public void tagVM(VM v, String tag);

	public void tagSite(Site s, String tag);

	public default void tag(String elementName, String tag) {
		tag(getElementByName(elementName), tag);
	}

	public default void delTag(ManagedElement element, String tag) {
		if (element == null) {
			return;
		}
		if (element instanceof Computer) {
			delTagComputer((Computer) element, tag);
		} else if (element instanceof Extern) {
			delTagExtern((Extern) element, tag);
		} else if (element instanceof VM) {
			delTagVM((VM) element, tag);
		} else if (element instanceof Site) {
			delTagSite((Site) element, tag);
		} else {
			throw new UnsupportedOperationException(
					"can't deltag element " + element + " with unsupported type in switch : " + element.getClass());
		}
	}

	public void delTagComputer(Computer n, String tag);

	public void delTagExtern(Extern e, String tag);

	public void delTagVM(VM v, String tag);

	public void delTagSite(Site s, String tag);

	public default void delTag(String elementName, String tag) {
		delTag(getElementByName(elementName), tag);
	}

	/**
	 * check if an element has given tag
	 *
	 * @param e
	 * @param tag
	 * @return
	 */
	public boolean isTagged(ManagedElement e, String tag);

	/**
	 * check if a hoster or its site is tagged with given tag
	 *
	 * @param h
	 *          a hoster
	 * @param tag
	 *          the tag to check
	 * @return true if h or its site is tagged with tag
	 */
	public default boolean isLocationTagged(VMLocation h, String tag) {
		return isTagged(h, tag) || isTagged(getSite(h), tag);
	}

	public Stream<VM> getVmsTagged(String tag);

	public Stream<Computer> getComputersTagged(String tag);

	public Stream<Extern> getExternsTagged(String tag);

	public Stream<Site> getSitesTagged(String tag);

	public Stream<String> getTags(ManagedElement e);

	/**
	 *
	 * @return the stream of all tags that are applied to at least one element of
	 *         the center.
	 */
	public default Stream<String> getAllTags() {
		return concat(concat(getVmsTags(), getComputersTags()), concat(getSitesTags(), getSitesTags())).distinct();
	}

	public Stream<String> getVmsTags();

	public Stream<String> getComputersTags();

	public Stream<String> getExternsTags();

	public Stream<String> getSitesTags();

}
