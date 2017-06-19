package fr.emn.optiplace.configuration.resources;

import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.view.ProvidedDataReader;

/**
 * specifies, for one resource known as its {@link #getType() type} the
 * capacities of the nodes and the consumptions of the VMs for that resource.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public interface ResourceSpecification extends ProvidedDataReader {

	/** @return the type of the resources, used as an ID. */
	String getType();

	/**
	 * set the use of a VM
	 *
	 * @param v
	 *          the VM
	 * @param use
	 *          the use value of this VM
	 */
	void use(VM v, int use);

	/**
	 * get the use value of a VM
	 *
	 * @param vm
	 *          the VM
	 * @return the use value of a VM if set, or 0 if not set.
	 */
	int getUse(VM vm);

	default int sumUses(VM... vms) {
		int ret = 0;
		if (vms != null) {
			for (VM v : vms) {
				ret += getUse(v);
			}
		}
		return ret;
	}

	/**
	 * set the use of a VM
	 *
	 * @param v
	 *          the VM
	 * @param use
	 *          the use value
	 * @return this.
	 */
	default ResourceSpecification with(VM v, int use) {
		use(v, use);
		return this;
	}
	/**
	 * @param vms
	 *          the vms to get the usages. Should not contain null values.
	 * @return the array of usages of this resource by the vms, such as
	 *         ret[i]:=usage(vms[i])
	 */
	default int[] getUses(VM... vms) {
		int[] ret = new int[vms.length];
		for (int i = 0; i < vms.length; i++) {
			ret[i] = getUse(vms[i]);
		}
		return ret;
	}

	/**
	 * get the capacity of an hoster
	 *
	 * @param h
	 *          an hoster
	 * @return the capacity is set, 0 if not set.
	 */
	int getCapacity(VMLocation h);

	/**
	 * @param nodes
	 *          the nodes to get the capacities. Should not contain null values.
	 * @return the array of capacities of this resource by the nodes, such as
	 *         ret[i]:=capacity(nodes[i])
	 */
	default int[] getCapacities(Node... nodes) {
		int[] ret = new int[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			ret[i] = getCapacity(nodes[i]);
		}
		return ret;
	}

	/**
	 * set the capacity of the hoster
	 *
	 * @param h
	 *          the hoster
	 * @param capacity
	 *          the capacity value of the hoster
	 */
	void capacity(VMLocation h, int capacity);

	/**
	 * set the capacity of an hoster
	 *
	 * @param h
	 *          the hoster
	 * @param capa
	 *          the hoster capacity value
	 * @return this
	 */
	default ResourceSpecification with(VMLocation h, int capa) {
		capacity(h, capa);
		return this;
	}

	/**
	 * get the use of a node in a given configuration
	 *
	 * @param cfg
	 *          the configuration
	 * @param n
	 *          the node
	 * @return the use of the node
	 */
	default int getUse(IConfiguration cfg, Node n) {
		return cfg.getHosted(n).collect(Collectors.summingInt(this::getUse));
	}

	/** get the total use of the VMs running in the center */
	default int getUse(IConfiguration cfg) {
		return cfg.getVMs().mapToInt(this::getUse).sum();
	}

	/** get the total capacity of the nodes which are online */
	default int getCapacity(IConfiguration cfg) {
		return cfg.getNodes().mapToInt(this::getCapacity).sum();
	}

	/**
	 * check wether a vm can be hosted on given node.
	 *
	 * @param n
	 *          the node
	 * @param vm
	 *          the vm
	 * @return true if there is enough resource on n to host vm.
	 */
	default boolean canHost(IConfiguration cfg, VMLocation n, VM vm) {
		if (n instanceof Node) {
			return getUse(cfg, (Node) n) + getUse(vm) <= getCapacity(n);
		} else if (n instanceof Extern) {
			return getUse(vm) <= getCapacity(n);
		}
		throw new UnsupportedOperationException("can't handle the class " + n.getClass());
	}

	/**
	 * make a comparator on VMs, based on this resource's uses. this comparator
	 * can then be used to sort collections according to the VMs' resource use
	 *
	 * @param increasing
	 *          true to make Collections.sort() sort with increasing order, false
	 *          to make decreasing order
	 * @return a new comparator, backing to this.
	 */
	default Comparator<VM> makeVMComparator(boolean increasing) {
		if (increasing) {
			return new Comparator<VM>() {
				@Override
				public int compare(VM o1, VM o2) {
					return getUse(o1) - getUse(o2);
				}

				@Override
				public String toString() {
					return "VMCOMP:" + getType() + "-" + (increasing ? "inc" : "dec");
				};
			};
		} else {
			return new Comparator<VM>() {
				@Override
				public int compare(VM o1, VM o2) {
					return getUse(o2) - getUse(o1);
				}

				@Override
				public String toString() {
					return "VMCOMP:" + getType() + "-" + (increasing ? "inc" : "dec");
				};
			};
		}
	}

	default Comparator<Node> makeNodeComparator(boolean increasing) {
		return new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return (increasing ? 1 : -1) * (getCapacity(o1) - getCapacity(o2));
			}

			@Override
			public String toString() {
				return "NodeCOMP:" + getType() + "-" + (increasing ? "inc" : "dec");
			}

			@Override
			public boolean equals(Object obj) {
				return obj != null && obj instanceof Comparator<?> && toString().equals(obj.toString());
			}

			@Override
			public int hashCode() {
				return toString().hashCode();
			}
		};
	}

	/**
	 * add resource specifications to a (potential null) map
	 *
	 * @param map
	 *          the map to add data inside, or null to create one
	 * @param res
	 *          the list of resource specifications. can be null or empty
	 * @return map if not null or a new map, into which specifications have been
	 *         added
	 */
	public static HashMap<String, ResourceSpecification> toMap(HashMap<String, ResourceSpecification> map,
			ResourceSpecification... res) {
		HashMap<String, ResourceSpecification> ret = map == null ? new HashMap<>() : map;
		if (res != null) {
			for (ResourceSpecification r : res) {
				ret.put(r.getType(), r);
			}
		}
		return ret;
	}

	/**
	 * find all the hosters which capacity respects given predicate
	 *
	 * @param filter
	 *          the predicate on the hosters capacities
	 * @return a new stream of the Hosters
	 */
	public default Stream<VMLocation> findHosters(IConfiguration c, Predicate<Integer> filter) {
		return c.getSiteLocations(null).filter(h -> filter.test(getCapacity(h)));
	}

	/**
	 * ensures an element has no reference. In most implementations that should be
	 * the same as settings its capacity/use to 0.
	 *
	 * @param e
	 *          the element
	 */
	public void remove(ManagedElement e);

	ResourceSpecification clone();

}
