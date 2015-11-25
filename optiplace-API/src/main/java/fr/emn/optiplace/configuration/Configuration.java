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
 * Node, Extern and VirtualMachine in a datacenter.<br />
 * VMs are hosted by an online Node, an extern or waiting, and Nodes are either
 * online or offline<
 * </p>
 * <p>
 * The VM and Node are stored in their add/state change order, meaning two
 * iterations on the streams will return the same exact result, leading to
 * determinism in the exploration.<br />
 * If the list of Nodes or VMs is the same for two configuration created in the
 * same way, the position in the list does not mean anything about the age of
 * the Element. Actually, the full list of Nodes and VMs is a concatenation of
 * the different state-specific lists of elements.
 * </p>
 *
 * @author Fabien Hermenier
 * @author guillaume Le Louët
 */
public interface Configuration {

	static enum VMSTATES {
		RUNNING, WAITING, EXTERN
	}

	static enum NODESTATES {
		ONLINE, OFFLINE
	}

	/**
	 * tooling to compare if two configurations have same vms, nodes, sites and
	 * externs
	 *
	 * @param first
	 * @param second
	 * @return
	 */
	public static boolean sameElements(Configuration first, Configuration second) {
		return !first.getNodes().parallel().filter(n -> !second.hasNode(n)).findAny().isPresent()
		    && !first.getVMs().parallel().filter(v -> !second.hasVM(v)).findAny().isPresent()
		    && !second.getNodes().parallel().filter(n -> !first.hasNode(n)).findAny().isPresent()
		    && !second.getVMs().parallel().filter(v -> !first.hasVM(v)).findAny().isPresent()
		    && !second.getExterns().parallel().filter(v -> !first.hasExtern(v)).findAny().isPresent()
		    && !first.getExterns().parallel().filter(v -> !second.hasExtern(v)).findAny().isPresent()
		    && !second.getSites().parallel().filter(v -> !first.hasSite(v)).findAny().isPresent()
		    && !first.getSites().parallel().filter(v -> !second.hasSite(v)).findAny().isPresent();
	}

	/**
	 * get an element with given name if it exists
	 *
	 * @param name
	 *          a name to check for an element
	 * @return the element with corresponding name if exists, or null.
	 */
	ManagedElement getElementByName(String name);

	/**
	 * Get the list of nodes that are online.
	 *
	 * @return a Stream of all the nodes online in this configuration
	 */
	Stream<Node> getOnlines();

	/**
	 * get a Stream of the nodes which are online and whom set of hosted VMs
	 * follow one predicate. The predicate is applied to unmodifiable set, and the
	 * VMs are unmutable so this call can not modify this
	 *
	 * @param pred
	 *          a predicate over the hosted VMs of a Node with no side-effect on
	 *          the set.
	 * @return The stream of node following the predicate over their hosted VMs
	 */
	Stream<Node> getOnlines(Predicate<Set<VM>> pred);

	/**
	 * Get the nodes that are offline.
	 *
	 * @return a Stream of all the nodes offline in this configuration
	 */
	Stream<Node> getOfflines();

	/**
	 * Get all the nodes involved in the configuration.
	 *
	 * @return a Stream, may be empty
	 */
	default Stream<Node> getNodes() {
		return Stream.concat(getOnlines(), getOfflines());
	}

	/**
	 * get the number of Node with given state
	 *
	 * @param state
	 *          the state of the nodes to consider, or null for all nodes
	 * @return the number of nodes with given state if not null, or the number of
	 *         nodes if null
	 */
	int nbNodes(NODESTATES state);

	default int nbNodes() {
		return nbNodes(null);
	}

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
		return Stream.concat(getRunnings(), Stream.concat(getWaitings(), getExterned()));
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
	 * get the number of VMs running on given node
	 *
	 * @param host
	 *          the node to consider
	 * @return the number of vms which are specified running on the node ; null if
	 *         the node is not known
	 */
	default long nbHosted(VMHoster host) {
		return getHosted(host).count();
	}

	/**
	 * @param n
	 *          a Node
	 * @return the state of the Node in the configuration, or null if the Node is
	 *         not known
	 */
	default NODESTATES getState(Node n) {
		if (isOnline(n)) {
			return NODESTATES.ONLINE;
		}
		if (isOffline(n)) {
			return NODESTATES.OFFLINE;
		}
		return null;
	}

	/**
	 * Test if a node is online.
	 *
	 * @param n
	 *          the node
	 * @return true if the node is online
	 */
	boolean isOnline(Node n);

	/**
	 * Test if a node is offline.
	 *
	 * @param n
	 *          the node
	 * @return true if the node is offline
	 */
	boolean isOffline(Node n);

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
	 * check if a node is already present in this
	 *
	 * @param n
	 *          a Node
	 * @return true if a node equal to this one already exist
	 */
	default boolean hasNode(Node n) {
		return isOnline(n) || isOffline(n);
	}

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
	 * Set a virtual machine running on a node. The node is set online whatever
	 * his previous state was. If the virtual machine is already in a other
	 * location or state in the configuration, it is updated
	 *
	 * @param vm
	 *          the virtual machine
	 * @param hoster
	 *          the managedelement that will host the virtual machine.
	 * @return true if the vm is assigned to the node and was not before
	 */
	boolean setHost(VM vm, VMHoster hoster);

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
	 *          null to set no migration, a Node to specify where the VM is
	 *          migrating
	 */
	void setMigTarget(VM vm, VMHoster n);

	/**
	 * @param vm
	 *          a VM of the center
	 * @return a Node if that vm is migrating to this node, or null.
	 */
	VMHoster getMigTarget(VM vm);

	default Node getNodeMig(VM vm) {
		VMHoster h = getMigTarget(vm);
		if (h instanceof Node) {
			return (Node) h;
		} else {
			return null;
		}
	}

	default Extern getExternMig(VM vm) {
		VMHoster h = getMigTarget(vm);
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
	 * @return true if the VM is migrating to another node
	 */
	default boolean isMigrating(VM vm) {
		VMHoster target = getMigTarget(vm);
		return target != null && !target.equals(getLocation(vm));
	}

	/**
	 * Add a VM in this; putting it on a Node or waiting. It also map the
	 * specified resource uses in the linkedHashMap of resources.
	 *
	 * @param vmName
	 *          the name of the VM
	 * @param host
	 *          the host or null if it is waiting
	 * @param resources
	 *          the resource use of the VM. Their must be less than
	 *          {@link #resources()} size, and any missing will be set to 0.
	 * @return a new VM with given specifications
	 */
	VM addVM(String vmName, VMHoster host, int... resources);

	/**
	 * Remove a virtual machine.
	 *
	 * @param vm
	 *          the virtual machine to remove
	 * @return true if this VM was present
	 */
	boolean remove(VM vm);

	/**
	 * Set a node online. If the node is already in the configuration but in an
	 * another state, it is updated.
	 *
	 * @param node
	 *          the node to add
	 * @return true if the node state changed
	 */
	boolean setOnline(Node node);

	Node addOnline(String name, int... resources);

	Node addOffline(String name, int... resources);

	/**
	 * Set a node offline. If the node is already in the configuration but in an
	 * another state, it is updated. Any hosted VM state will be set to waiting.
	 *
	 * @param node
	 *          the node
	 * @return true if the node state changed
	 */
	boolean setOffline(Node node);

	/**
	 * Remove a node and set all its vms to waiting
	 *
	 * @param n
	 *          the node to remove
	 * @return true if the Node was present
	 */
	boolean remove(Node n);

	/**
	 *
	 * @param hosted
	 *          a vm of the problem
	 * @return the node hosting the VM or null
	 */
	default Node getNodeHost(VM hosted) {
		return getState(hosted) == VMSTATES.RUNNING ? (Node) getLocation(hosted) : null;
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
	 *          the node
	 * @return a set of virtual machines, may be empty, eg if the Node is not
	 *         present or is offline
	 */
	Stream<VM> getHosted(VMHoster n);

	/**
	 * get the number of VM executed on given hoster
	 *
	 * @param host
	 *          an hoster of the configuration
	 * @return the number of VM that are specified running on given hoster
	 */
	default Stream<VM> getFutureHosted(VMHoster host) {
		return getVMs().filter(v -> host.equals(getFutureLocation(v)));
	}

	/**
	 * Get all the virtual machines running on a set of nodes.
	 *
	 * @param ns
	 *          the set of nodes
	 * @return a set of virtual machines, may be empty
	 */
	default Stream<VM> getHosted(Set<VMHoster> ns) {
		return ns.stream().map(this::getHosted).reduce(Stream::concat).get();
	}

	/**
	 * Get the location of a virtual machine. This is the Hoster that is executing
	 * it right now.
	 *
	 * @param vm
	 *          the virtual machine
	 * @return the node or extern hosting the virtual machine or {@code null} is
	 *         the virtual machine is waiting
	 */
	VMHoster getLocation(VM vm);

	/**
	 * get the future location of a VM once the migrations are done.
	 *
	 * @param v
	 *          a VM of the configuration
	 * @return the migration target of the VM or the location if no migration
	 *         present
	 */
	default VMHoster getFutureLocation(VM vm) {
		VMHoster ret = getMigTarget(vm);
		return ret == null ? getLocation(vm) : ret;
	}

	/**
	 * add an external site to host some VMs
	 *
	 * @param e
	 *          the name of the exter. must be unique, as no
	 *          {@link ManagedElement} uses it
	 * @return the extern with this name if already present, a new Extern with
	 *         this name if no ManagedElement with this name, or null if anohter
	 *         ManagedElement has this name
	 */
	Extern addExtern(String name, int... resources);

	Stream<Extern> getExterns();

	boolean hasExtern(Extern e);

	int nbExterns();

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
	ResourceSpecification addResource(String name);

	/**
	 * some basic checks to perform on a configuration
	 */
	static enum BasicChecks {

		OFFLINE_OR_ONLINE {

			@Override
			public boolean check(Configuration c) {
				return !c.getOfflines().filter(c::isOnline).findFirst().isPresent();
			}

		},
		RUNNING_OR_EXTERN_OR_WAITING {

			@Override
			public boolean check(Configuration c) {
				return !c.getWaitings().filter(c::isRunning).findFirst().isPresent();
			}

		},
		HOSTER_HOSTS {

			@Override
			public boolean check(Configuration c) {
				return !c.getRunnings().filter(v -> !c.getHosted(c.getLocation(v)).filter(v::equals).findFirst().isPresent())
		        .findFirst().isPresent();
			}

		};

		public abstract boolean check(Configuration c);
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
	 * compute the max number of VM of a kind a Node can host, based on the
	 * resource capacities of the node and the use of the VM.
	 *
	 * @param n
	 *          a Node
	 * @param vm
	 *          a VM to use the resource specifications
	 * @param specs
	 *          the resource specifications from which we can get the capacities
	 *          of the node and the use of the VM
	 * @return the minimum of capa(n)/use(VM) for each resource of specs
	 */
	public static double maxNBVms(Node n, VM vm, Stream<ResourceSpecification> specs) {
		return specs.mapToDouble(s -> 1.0 * s.getCapacity(n) / s.getLoad(vm)).min().getAsDouble();
	}

	/********************
	 * site management. A site , represented by an index, is a partition of the
	 * nodes.<br />
	 * By default one site, the index 0, exists. adding nodes to a new site remove
	 * them from the old one.<br />
	 * However, since the index is the order of creation of the sites, deleting
	 * all the nodes from a site doesn't remove it.
	 */

	/**
	 * add nodes to a site at given index
	 *
	 * @param siteName
	 *          the requested site name, or null to add the node to no site.
	 * @param nodes
	 *          the nodes to add to the site, or none to create an empty site or
	 *          retrieve an existing site
	 * @return the site with given name
	 */
	public Site addSite(String siteName, VMHoster... hosters);

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
	public Site getSite(VMHoster n);

	public Stream<Site> getSites();

	public default boolean hasSite(Site s) {
		if (s == null) {
			return false;
		}
		ManagedElement me = getElementByName(s.getName());
		return me != null && me instanceof Site;
	}

	/**
	 *
	 * @param Site
	 *          a site
	 * @return a stream over the nodes contained in this site. if this site is not
	 *         present, return an empty stream ; if this site is null, return the
	 *         stream of the nodes with no site.
	 */
	public Stream<VMHoster> getNodes(Site site);

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
		if (element instanceof Node) {
			tagNode((Node) element, tag);
		} else
		  if (element instanceof Extern) {
			tagExtern((Extern) element, tag);
		} else
		    if (element instanceof VM) {
			tagVM((VM) element, tag);
		} else
		      if (element instanceof Site) {
			tagSite((Site) element, tag);
		} else {
			throw new UnsupportedOperationException(
			    "can't tag element " + element + " with unsupported type in switch : " + element.getClass());
		}
	}

	public void tagNode(Node n, String tag);

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
		if (element instanceof Node) {
			delTagNode((Node) element, tag);
		} else
		  if (element instanceof Extern) {
			delTagExtern((Extern) element, tag);
		} else
		    if (element instanceof VM) {
			delTagVM((VM) element, tag);
		} else
		      if (element instanceof Site) {
			delTagSite((Site) element, tag);
		} else {
			throw new UnsupportedOperationException(
			    "can't deltag element " + element + " with unsupported type in switch : " + element.getClass());
		}
	}

	public void delTagNode(Node n, String tag);

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
	public default boolean isHosterTagged(VMHoster h, String tag) {
		return isTagged(h, tag) || isTagged(getSite(h), tag);
	}

	public Stream<VM> getVmsTagged(String tag);

	public Stream<Node> getNodesTagged(String tag);

	public Stream<Extern> getExternsTagged(String tag);

	public Stream<Site> getSitesTagged(String tag);

	public Stream<String> getTags(ManagedElement e);

	/**
	 *
	 * @return the stream of all tags that are applied to at least one element of
	 *         the center.
	 */
	public default Stream<String> getAllTags() {
		return concat(concat(getVmsTags(), getNodesTags()), concat(getSitesTags(), getSitesTags())).distinct();
	}

	public Stream<String> getVmsTags();

	public Stream<String> getNodesTags();

	public Stream<String> getExternsTags();

	public Stream<String> getSitesTags();

}
